package com.iso.hypo.membership.application.usecase.impl;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.iso.hypo.common.application.context.RequestContext;
import com.iso.hypo.common.application.event.enumeration.OperationEnum;
import com.iso.hypo.common.application.security.RoleEnum;
import com.iso.hypo.common.domain.model.Message;
import com.iso.hypo.common.domain.model.enumeration.MessageSeverityEnum;
import com.iso.hypo.common.domain.model.location.Address;
import com.iso.hypo.membership.application.dto.MemberDto;
import com.iso.hypo.membership.application.event.MemberEvent;
import com.iso.hypo.membership.application.exception.MemberException;
import com.iso.hypo.membership.application.mapper.MemberDtoMapper;
import com.iso.hypo.membership.application.port.BrandServicePort;
import com.iso.hypo.membership.application.port.UserServicePort;
import com.iso.hypo.membership.application.port.dto.BrandRef;
import com.iso.hypo.membership.application.port.dto.UserRef;
import com.iso.hypo.membership.application.usecase.MemberService;
import com.iso.hypo.membership.domain.model.Member;
import com.iso.hypo.membership.domain.repository.MemberRepository;
import com.iso.hypo.sale.application.exception.OrderException;

@Service
public class MemberServiceImpl implements MemberService {

	private final BrandServicePort brandServicePort;

	private final MemberRepository memberRepository;

	private final MemberDtoMapper memberMapper;

	private final UserServicePort userServicePort;

	private final ApplicationEventPublisher eventPublisher;

	private static final Logger logger = LoggerFactory.getLogger(MemberServiceImpl.class);

	private final RequestContext requestContext;

	public MemberServiceImpl(MemberDtoMapper memberMapper, MemberRepository memberRepository,
			UserServicePort userServicePort, ApplicationEventPublisher eventPublisher,
			BrandServicePort brandServicePort, RequestContext requestContext) {
		this.memberMapper = memberMapper;
		this.memberRepository = memberRepository;
		this.userServicePort = userServicePort;
		this.eventPublisher = eventPublisher;
		this.brandServicePort = brandServicePort;
		this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
	}

	@Override
	@Transactional
	public MemberDto create(MemberDto memberDto, String password) throws MemberException {
		try {
			Assert.notNull(memberDto, "memberDto must not be null");

			Member member = memberMapper.toEntity(memberDto);

			BrandRef brand = resolveBrand(memberDto.getBrandUuid());

			// Find Member
			Optional<Member> existingMember = memberRepository.findByBrandUuidAndPersonEmailAndDeletedIsFalse(
					member.getBrandUuid(), member.getPerson().getEmail());

			if (existingMember.isPresent()) {
				Message message = new Message();
				message.setCode(MemberException.MEMBER_ALREADY_EXIST);
				message.setDescription("Duplicate member");
				message.setSeverity(MessageSeverityEnum.error);
				existingMember.get().setMessages(List.of(message));

				throw new MemberException(requestContext.getTrackingNumber(), MemberException.MEMBER_ALREADY_EXIST,
						"Duplicate member", memberMapper.toDto(existingMember.get()));
			}

			Optional<UserRef> user = userServicePort.findByEmail(member.getPerson().getEmail());
			if (user.isEmpty()) {
				// Create user
				UserRef newUser = new UserRef();
				newUser.setEmail(member.getPerson().getEmail());
				newUser.setFirstname(member.getPerson().getFirstname());
				newUser.setLastname(member.getPerson().getLastname());
				newUser.setRoles(List.of(RoleEnum.member));

				user = Optional.of(userServicePort.create(newUser, password, member.getBrandUuid()));

				/*
				 * Message message = new Message();
				 * message.setCode(MemberException.USER_ALREADY_EXIST);
				 * message.setDescription("Duplicate user");
				 * message.setSeverity(MessageSeverityEnum.critical);
				 * member.setMessages(List.of(message));
				 * 
				 * throw new MemberException(requestContext.getTrackingNumber(),
				 * MemberException.USER_ALREADY_EXIST, "Duplicate user",
				 * memberMapper.toDto(member));
				 */
			}

			member = initializeAddress(member, brand);

			// Create member
			member.setUuid(UUID.randomUUID().toString());
			member.setCreatedOn(Instant.now());
			member.setCreatedBy(requestContext.getUsername());
			member.setUserUuid(user.get().getUuid());

			Member saved = memberRepository.save(member);
			return memberMapper.toDto(saved);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}", memberDto != null ? memberDto.getBrandUuid() : null, e);

			if (e instanceof MemberException) {
				throw (MemberException) e;
			}
			throw new MemberException(requestContext.getTrackingNumber(), MemberException.CREATION_FAILED, e);
		}
	}

	private Member initializeAddress(Member member, BrandRef brand) {
		// Set member address country and state same as brand if not provided in request
		String country = brand.getAddress() != null ? brand.getAddress().getCountry() : "CA";
		String state = brand.getAddress() != null ? brand.getAddress().getState() : "QC";

		if (member.getPerson().getAddress() == null) {
			member.getPerson().setAddress(new Address());
		}

		if (member.getPerson().getAddress().getCountry() == null) {
			member.getPerson().getAddress().setCountry(country);
		}

		if (member.getPerson().getAddress().getState() == null) {
			member.getPerson().getAddress().setState(state);
		}
		
		return member;
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
			BrandRef brand = resolveBrand(brandUuid);

			Member entity = this.readByMemberUuid(brand.getUuid(), memberUuid);
			entity.activate(requestContext.getUsername());
			memberRepository.save(entity);

			return memberMapper.toDto(entity);
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
			BrandRef brand = resolveBrand(brandUuid);
			
			Member entity = this.readByMemberUuid(brand.getUuid(), memberUuid);
			entity.deactivate(requestContext.getUsername());
			memberRepository.save(entity);

			return memberMapper.toDto(entity);
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
			BrandRef brand = resolveBrand(brandUuid);
			
			Member entity = this.readByMemberUuid(brand.getUuid(), memberUuid);
			entity.delete(requestContext.getUsername());
			memberRepository.save(entity);

			eventPublisher.publishEvent(new MemberEvent(this, memberMapper.toDto(entity), OperationEnum.delete));
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
			if (brandServicePort.brandDeleted(brandUuid)) {
				long deletedCount = memberRepository.deleteAllByBrandUuid(brandUuid, requestContext.getUsername());

				logger.info("Member deleted for brand - brandUuid={} deletedCount={} ", brandUuid, deletedCount);	
			}
		} catch (Exception e) {
			logger.error("Error - brandId={}", brandUuid, e);

			throw new MemberException(requestContext.getTrackingNumber(), MemberException.DELETE_FAILED, e);
		}
	}

	private MemberDto updateMember(MemberDto memberDto, boolean skipNull) throws MemberException {
		try {
			Assert.notNull(memberDto, "memberDto must not be null");

			BrandRef brand = resolveBrand(memberDto.getBrandUuid());
			
			Member member = memberMapper.toEntity(memberDto);

			Member oldMember = this.readByMemberUuid(brand.getUuid(), member.getUuid());

			ModelMapper mapper = new ModelMapper();
			mapper.getConfiguration().setSkipNullEnabled(skipNull).setCollectionsMergeEnabled(false);

			mapper = memberMapper.initMemberMappings(mapper);
			mapper.map(member, oldMember);

			if (memberDto.getPerson().getEmail() != null
					&& !memberDto.getPerson().getEmail().equals(oldMember.getPerson().getEmail())) {
				// Member email is updated, need to check duplicate email and update user email
				// in user service.
				Optional<Member> existingMember = memberRepository.findByBrandUuidAndPersonEmailAndDeletedIsFalse(
						oldMember.getBrandUuid(), memberDto.getPerson().getEmail());
				if (existingMember.isPresent()) {
					Message message = new Message();
					message.setCode(MemberException.MEMBER_ALREADY_EXIST);
					message.setDescription("Duplicate user");
					message.setSeverity(MessageSeverityEnum.warning);
					oldMember.setMessages(List.of(message));

					throw new MemberException(requestContext.getTrackingNumber(), MemberException.MEMBER_ALREADY_EXIST,
							"Duplicate member", memberMapper.toDto(oldMember));
				}

				Optional<UserRef> user = userServicePort.findByEmail(member.getPerson().getEmail());
				if (user.isPresent()) {
					Message message = new Message();
					message.setCode(MemberException.USER_ALREADY_EXIST);
					message.setDescription("Duplicate user");
					message.setSeverity(MessageSeverityEnum.critical);
					member.setMessages(List.of(message));

					throw new MemberException(requestContext.getTrackingNumber(), MemberException.USER_ALREADY_EXIST,
							"Duplicate user", memberMapper.toDto(member));
				}
			}

			if (oldMember.getUserUuid() != null) {
				UserRef user = new UserRef();
				user.setUuid(oldMember.getUserUuid());
				user.setEmail(oldMember.getPerson().getEmail());
				user.setFirstname(oldMember.getPerson().getFirstname());
				user.setLastname(oldMember.getPerson().getLastname());

				// Don't update user role from member update API,
				// as it is not in scope and can be managed separately from user management API.
				// So set null to avoid overriding existing roles.
				user.setRoles(null);
				userServicePort.patch(user);
			}
			
			oldMember = initializeAddress(oldMember, brand);
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
		Optional<Member> entity = memberRepository.findByBrandUuidAndUuidAndDeletedIsFalse(brandUuid, memberUuid);
		if (entity.isEmpty()) {
			throw new MemberException(requestContext.getTrackingNumber(), MemberException.MEMBER_NOT_FOUND,
					"Member not found");
		}

		return entity.get();
	}
	
	private BrandRef resolveBrand(String brandUuid) throws OrderException {
		return brandServicePort.find(brandUuid)
				.orElseThrow(() -> new OrderException(requestContext.getTrackingNumber(), OrderException.BRAND_NOT_FOUND,
				"Brand not found - brandUuid=" + brandUuid));
	}

}
