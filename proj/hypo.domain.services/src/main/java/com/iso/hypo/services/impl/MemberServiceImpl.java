package com.iso.hypo.services.impl;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.iso.hypo.common.context.RequestContext;
import com.iso.hypo.domain.Message;
import com.iso.hypo.domain.aggregate.Member;
import com.iso.hypo.domain.aggregate.User;
import com.iso.hypo.domain.dto.MemberDto;
import com.iso.hypo.domain.enumeration.MessageSeverityEnum;
import com.iso.hypo.domain.security.RoleEnum;
import com.iso.hypo.domain.security.Roles;
import com.iso.hypo.events.event.OperationEnum;
import com.iso.hypo.repositories.MemberRepository;
import com.iso.hypo.repositories.UserRepository;
import com.iso.hypo.services.BrandQueryService;
import com.iso.hypo.services.MemberService;
import com.iso.hypo.services.clients.AzureGraphClientService;
import com.iso.hypo.services.event.MemberEvent;
import com.iso.hypo.services.exception.BrandException;
import com.iso.hypo.services.exception.MemberException;
import com.iso.hypo.services.exception.UserException;
import com.iso.hypo.services.mappers.MemberMapper;
import com.microsoft.graph.models.PasswordProfile;

@Service
public class MemberServiceImpl implements MemberService {

	private final BrandQueryService brandQueryService;;

	private final MemberRepository memberRepository;

	private final MemberMapper memberMapper;

	private final UserRepository userRepository;

	private final AzureGraphClientService azureGraphClientService;

	private final ApplicationEventPublisher eventPublisher;

	@Value("${app.test.run:false}")
	private boolean testRun;

	private static final Logger logger = LoggerFactory.getLogger(MemberServiceImpl.class);

	private final RequestContext requestContext;

	public MemberServiceImpl(MemberMapper memberMapper, MemberRepository memberRepository,
			UserRepository userRepository, ApplicationEventPublisher eventPublisher,
			BrandQueryService brandQueryService, AzureGraphClientService azureGraphClientService,
			RequestContext requestContext) {
		this.memberMapper = memberMapper;
		this.memberRepository = memberRepository;
		this.userRepository = userRepository;
		this.azureGraphClientService = azureGraphClientService;
		this.eventPublisher = eventPublisher;
		this.brandQueryService = brandQueryService;
		this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
	}

	@Override
	@Transactional
	public MemberDto create(MemberDto memberDto, String password) throws MemberException {
		try {
			Assert.notNull(memberDto, "memberDto must not be null");

			Member member = memberMapper.toEntity(memberDto);

			brandQueryService.assertExists(member.getBrandUuid());

			// Find Member
			Optional<Member> existingMember = memberRepository.findByBrandUuidAndPersonEmailAndIsDeletedIsFalse(
					member.getBrandUuid(), member.getPerson().getEmail());

			if (existingMember.isPresent()) {
				Message message = new Message();
				message.setCode(MemberException.MEMBER_ALREADY_EXIST);
				message.setDescription("Duplicate member");
				message.setSeverity(MessageSeverityEnum.warning);
				existingMember.get().getMessages().add(message);

				throw new MemberException(requestContext.getTrackingNumber(), MemberException.MEMBER_ALREADY_EXIST,
						"Duplicate member", memberMapper.toDto(existingMember.get()));
			}

			// Create member
			member.setCreatedOn(Instant.now());
			member.setCreatedBy(requestContext.getUsername());
			member.setUuid(UUID.randomUUID().toString());
						
			// Skip external side-effect during JUnit runs
			if (!testRun) {
				Optional<com.microsoft.graph.models.User> idpUser = null;
				
				Optional<User> existingUser = userRepository.findByEmailAndIsDeletedIsFalse(memberDto.getPerson().getEmail());
				if (existingUser.isPresent()) {
					idpUser = azureGraphClientService.findUser(existingUser.get().getIdpId());
				} else {
					idpUser = Optional.empty();
				}
				
				if (!idpUser.isPresent()) {
					// Create user in identity provider
					com.microsoft.graph.models.User newUser = new com.microsoft.graph.models.User();
					
					newUser.setAccountEnabled(true);
					newUser.setDisplayName(member.getPerson().getFirstname() + " " + member.getPerson().getLastname());
					newUser.setGivenName(member.getPerson().getFirstname());
					newUser.setSurname(member.getPerson().getLastname());
					newUser.setMailNickname(member.getUuid());
					newUser.setMail(member.getPerson().getEmail());
					newUser.setUserPrincipalName(member.getUuid());
					newUser.setPasswordProfile(new PasswordProfile());
					newUser.getPasswordProfile().setForceChangePasswordNextSignIn(false);
					newUser.getPasswordProfile().setPassword(password);
					
					idpUser = Optional.of(azureGraphClientService.createUser(newUser));
				}

				// Assign role to user
				azureGraphClientService.assignRole(idpUser.get().getId(), Roles.Member);

				// Assign Group to user
				azureGraphClientService.addToGroup(idpUser.get().getId(), member.getBrandUuid());
				
				if (!existingUser.isPresent()) {
					// Persist user
					User user = new User();
					user.setIdpId(idpUser.get().getId());
					user.setUpn(idpUser.get().getUserPrincipalName());
					user.setEmail(idpUser.get().getMail());
					user.getRoles().add(RoleEnum.member);
					User userSaved = userRepository.save(user);
					
					member.setUser(userSaved);
				} else {
					member.setUser(existingUser.get());
				}
			} else {
				logger.debug("Skipping createUser() because app.test-run=true.");
			}

			Member saved = memberRepository.save(member);
			return memberMapper.toDto(saved);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}", memberDto != null ? memberDto.getBrandUuid() : null, e);

			if (e instanceof BrandException) {
				throw new MemberException(requestContext.getTrackingNumber(), MemberException.BRAND_NOT_FOUND,
						"Brand not found");
			}
			if (e instanceof MemberException) {
				throw (MemberException) e;
			}
			throw new MemberException(requestContext.getTrackingNumber(), MemberException.CREATION_FAILED, e);
		}
	}

	@Override
	@Transactional
	public MemberDto update(MemberDto memberDto) throws MemberException {
		try {
			return updateMember(memberDto, false);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, memberUuid={}", memberDto != null ? memberDto.getBrandUuid() : null,
					memberDto != null ? memberDto.getUuid() : null, e);

			if (e instanceof MemberException) {
				throw (MemberException) e;
			}
			throw new MemberException(requestContext.getTrackingNumber(), MemberException.UPDATE_FAILED, e);
		}
	}

	@Override
	@Transactional
	public MemberDto patch(MemberDto memberDto) throws MemberException {
		try {
			return updateMember(memberDto, true);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, memberUuid={}", memberDto != null ? memberDto.getBrandUuid() : null,
					memberDto != null ? memberDto.getUuid() : null, e);

			if (e instanceof MemberException) {
				throw (MemberException) e;
			}
			throw new MemberException(requestContext.getTrackingNumber(), MemberException.UPDATE_FAILED, e);
		}
	}

	@Override
	@Transactional
	public MemberDto activate(String brandUuid, String memberUuid) throws MemberException {
		try {
			Optional<Member> memberOpt = memberRepository.activate(brandUuid, memberUuid);
			if (memberOpt.isEmpty()) {
				throw new MemberException(requestContext.getTrackingNumber(), MemberException.MEMBER_NOT_FOUND,
						"Member not found");
			}

			return memberMapper.toDto(memberOpt.get());
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, memberUuid={}", brandUuid, memberUuid, e);

			if (e instanceof MemberException) {
				throw (MemberException) e;
			}
			throw new MemberException(requestContext.getTrackingNumber(), MemberException.ACTIVATION_FAILED, e);
		}
	}

	@Override
	@Transactional
	public MemberDto deactivate(String brandUuid, String memberUuid) throws MemberException {
		try {
			Optional<Member> memberOpt = memberRepository.deactivate(brandUuid, memberUuid);
			if (memberOpt.isEmpty()) {
				throw new MemberException(requestContext.getTrackingNumber(), MemberException.MEMBER_NOT_FOUND,
						"Member not found");
			}

			return memberMapper.toDto(memberOpt.get());
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, memberUuid={}", brandUuid, memberUuid, e);

			if (e instanceof MemberException) {
				throw (MemberException) e;
			}
			throw new MemberException(requestContext.getTrackingNumber(), MemberException.DEACTIVATION_FAILED, e);
		}
	}

	@Override
	@Transactional
	public void delete(String brandUuid, String memberUuid) throws MemberException {
		try {
			Member entity = this.readByMemberUuid(brandUuid, memberUuid);
			memberRepository.delete(entity.getBrandUuid(), entity.getUuid(), requestContext.getUsername());
			eventPublisher.publishEvent(new MemberEvent(this, entity, OperationEnum.delete));
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, memberUuid={}", brandUuid, memberUuid, e);

			if (e instanceof MemberException) {
				throw (MemberException) e;
			}
			throw new MemberException(requestContext.getTrackingNumber(), MemberException.DELETE_FAILED, e);
		}
	}

	@Override
	public void deleteAllByBrandUuid(String brandUuid) throws MemberException {
		try {
			long deletedCount = memberRepository.deleteAllByBrandUuid(brandUuid, requestContext.getUsername());

			logger.info("Member deleted for brand - brandUuid={} deletedCount={} ", brandUuid, deletedCount);
		} catch (Exception e) {
			logger.error("Error - brandId={}", brandUuid, e);

			throw new MemberException(requestContext.getTrackingNumber(), MemberException.DELETE_FAILED, e);
		}
	}

	private MemberDto updateMember(MemberDto memberDto, boolean skipNull) throws MemberException {
		try {
			Assert.notNull(memberDto, "memberDto must not be null");
			Member member = memberMapper.toEntity(memberDto);

			Member oldMember = this.readByMemberUuid(member.getBrandUuid(), member.getUuid());

			ModelMapper mapper = new ModelMapper();
			mapper.getConfiguration().setSkipNullEnabled(skipNull).setCollectionsMergeEnabled(false);

			mapper = memberMapper.initMemberMappings(mapper);
			mapper.map(member, oldMember);
			
			if (memberDto.getPerson().getEmail() != null && !memberDto.getPerson().getEmail().equals(oldMember.getPerson().getEmail())) {
				Optional<User> existingMember = userRepository.findByEmailAndIsDeletedIsFalse(memberDto.getPerson().getEmail());
				if (existingMember.isPresent()) {
					Message message = new Message();
					message.setCode(UserException.USER_ALREADY_EXIST);
					message.setDescription("Duplicate user");
					message.setSeverity(MessageSeverityEnum.warning);
					memberDto.getMessages().add(message);

					throw new MemberException(requestContext.getTrackingNumber(), MemberException.MEMBER_ALREADY_EXIST,
							"Duplicate member", memberMapper.toDto(oldMember));
				}
			}
			
			if (!testRun) {
				Optional<com.microsoft.graph.models.User> idpUser = azureGraphClientService.findUser(oldMember.getUser().getIdpId());
				if (idpUser.isPresent()) {
					idpUser.get().setDisplayName(oldMember.getPerson().getFirstname() + " " + oldMember.getPerson().getLastname());
					idpUser.get().setGivenName(oldMember.getPerson().getFirstname());
					idpUser.get().setSurname(oldMember.getPerson().getLastname());
					idpUser.get().setMail(oldMember.getPerson().getEmail());
					azureGraphClientService.updateUser(idpUser.get());
				}
			}

			oldMember.setModifiedOn(Instant.now());
			oldMember.setModifiedBy(requestContext.getUsername());

			Member saved = memberRepository.save(oldMember);
			return memberMapper.toDto(saved);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, memberUuid={}", memberDto != null ? memberDto.getBrandUuid() : null,
					memberDto != null ? memberDto.getUuid() : null, e);

			if (e instanceof MemberException) {
				throw (MemberException) e;
			}
			throw new MemberException(requestContext.getTrackingNumber(), MemberException.UPDATE_FAILED, e);
		}
	}
	
	private Member readByMemberUuid(String brandUuid, String memberUuid) throws MemberException {
		Optional<Member> entity = memberRepository.findByBrandUuidAndUuidAndIsDeletedIsFalse(brandUuid, memberUuid);
		if (entity.isEmpty()) {
			throw new MemberException(requestContext.getTrackingNumber(), MemberException.MEMBER_NOT_FOUND,
					"Member not found");
		}

		return entity.get();
	}

}
