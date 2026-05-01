package com.iso.hypo.membership.application.usecase.impl;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.iso.hypo.common.application.context.RequestContext;
import com.iso.hypo.membership.application.dto.MembershipDto;
import com.iso.hypo.membership.application.exception.MembershipException;
import com.iso.hypo.membership.application.mapper.MembershipDtoMapper;
import com.iso.hypo.membership.application.port.BrandServicePort;
import com.iso.hypo.membership.application.usecase.MembershipService;
import com.iso.hypo.membership.domain.model.Membership;
import com.iso.hypo.membership.domain.repository.MembershipRepository;

@Service
public class MembershipServiceImpl implements MembershipService {

	private final BrandServicePort brandValidationPort;
	
	private final MembershipRepository membershipRepository;

	private final MembershipDtoMapper membershipMapper;

	// replace field-injected logger with static logger
	private static final Logger logger = LoggerFactory.getLogger(MembershipServiceImpl.class);

	private final RequestContext requestContext;
	
	public MembershipServiceImpl(MembershipDtoMapper membershipMapper, MembershipRepository membershipRepository,
			BrandServicePort brandValidationPort, RequestContext requestContext) {
		this.membershipMapper = membershipMapper;
		this.membershipRepository = membershipRepository;
		this.brandValidationPort = brandValidationPort;
		this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
	}

	@Override
	@Transactional
	public MembershipDto create(MembershipDto membershipDto) throws MembershipException {
		try {
			Assert.notNull(membershipDto, "membershipDto must not be null");
			Membership membership = membershipMapper.toEntity(membershipDto);

			if (!brandValidationPort.brandExists(membership.getBrandUuid())) {
				throw new MembershipException(requestContext.getTrackingNumber(), MembershipException.BRAND_NOT_FOUND, "Brand not found");
			}
			
			membership.setCreatedOn(Instant.now());
			membership.setCreatedBy(requestContext.getUsername());
			
			Membership saved = membershipRepository.save(membership);
			return membershipMapper.toDto(saved);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}", membershipDto != null ? membershipDto.getBrandUuid() : null, e);
			
			if (e instanceof MembershipException) {
				throw (MembershipException) e;
			}
			throw new MembershipException(requestContext.getTrackingNumber(), MembershipException.CREATION_FAILED, e);
		}
	}

	@Override
	@Transactional
	public MembershipDto update(MembershipDto membershipDto) throws MembershipException {
		try {
			return updateMembership(membershipDto, false);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, membershipUuid={}", membershipDto != null ? membershipDto.getBrandUuid() : null, membershipDto != null ? membershipDto.getUuid() : null, e);
			
			if (e instanceof MembershipException) {
				throw (MembershipException) e;
			}
			throw new MembershipException(requestContext.getTrackingNumber(), MembershipException.UPDATE_FAILED, e);
		}
	}

	@Override
	@Transactional
	public MembershipDto patch(MembershipDto membershipDto) throws MembershipException {
		try {
			return updateMembership(membershipDto, true);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, membershipUuid={}", membershipDto != null ? membershipDto.getBrandUuid() : null, membershipDto != null ? membershipDto.getUuid() : null, e);
			
			if (e instanceof MembershipException) {
				throw (MembershipException) e;
			}
			throw new MembershipException(requestContext.getTrackingNumber(), MembershipException.UPDATE_FAILED, e);
		}
	}

	@Override
	@Transactional
	public MembershipDto activate(String brandUuid, String membershipUuid) throws MembershipException {
		try {
			Membership entity = this.readByMembershipUuid(brandUuid, membershipUuid);
			entity.activate(requestContext.getUsername());
			membershipRepository.save(entity);
			
			return membershipMapper.toDto(entity);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, membershipUuid={}", brandUuid, membershipUuid, e);
			
			if (e instanceof MembershipException) {
				throw (MembershipException) e;
			}
			throw new MembershipException(requestContext.getTrackingNumber(), MembershipException.ACTIVATION_FAILED, e);
		}
	}

	@Override
	@Transactional
	public MembershipDto deactivate(String brandUuid, String membershipUuid) throws MembershipException {
		try {
			Membership entity = this.readByMembershipUuid(brandUuid, membershipUuid);
			entity.deactivate(requestContext.getUsername());
			membershipRepository.save(entity);
			
			return membershipMapper.toDto(entity);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, membershipUuid={}", brandUuid, membershipUuid, e);
			
			if (e instanceof MembershipException) {
				throw (MembershipException) e;
			}
			throw new MembershipException(requestContext.getTrackingNumber(), MembershipException.DEACTIVATION_FAILED, e);
		}
	}
	
	@Override
	@Transactional
	public void delete(String brandUuid, String membershipUuid) throws MembershipException {
		try {
			Membership entity = this.readByMembershipUuid(brandUuid, membershipUuid);
			entity.delete(requestContext.getUsername());
			membershipRepository.save(entity);
			
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, membershipUuid={}", brandUuid, membershipUuid, e);
			
			if (e instanceof MembershipException) {
				throw (MembershipException) e;
			}
			throw new MembershipException(requestContext.getTrackingNumber(), MembershipException.DELETE_FAILED, e);
		}
	}
	
	@Override
	public void deleteAllByBrandUuid(String brandUuid) throws MembershipException {
		try {
			long deletedCount = membershipRepository.deleteAllByBrandUuid(brandUuid, requestContext.getUsername());
			
			logger.info("MembershipPlan deleted for brand - brandUuid={} deletedCount={} ", brandUuid, deletedCount);
		} catch (Exception e) {
			logger.error("Error - brandId={}", brandUuid, e);
			
			throw new MembershipException(requestContext.getTrackingNumber(), MembershipException.DELETE_FAILED, e);
		}
	}
	
	private MembershipDto updateMembership(MembershipDto membershipDto, boolean skipNull) throws MembershipException {
		try {
			Assert.notNull(membershipDto, "membershipDto must not be null");
			Membership membership = membershipMapper.toEntity(membershipDto);
			
			Membership oldMembership = this.readByMembershipUuid(membership.getBrandUuid(), membership.getUuid());

			ModelMapper mapper = new ModelMapper();
			mapper.getConfiguration().setSkipNullEnabled(skipNull);
			
			mapper = membershipMapper.initMembershipMappings(mapper);
			mapper.map(membership, oldMembership);

			oldMembership.setModifiedOn(Instant.now());
			oldMembership.setModifiedBy(requestContext.getUsername());
			
			Membership saved = membershipRepository.save(oldMembership);
			return membershipMapper.toDto(saved);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, membershipUuid={}", membershipDto != null ? membershipDto.getBrandUuid() : null, membershipDto != null ? membershipDto.getUuid() : null, e);
			
			if (e instanceof MembershipException) {
				throw (MembershipException) e;
			}
			throw new MembershipException(requestContext.getTrackingNumber(), MembershipException.UPDATE_FAILED, e);
		}
	}
	
	private Membership readByMembershipUuid(String brandUuid, String membershipUuid) throws MembershipException {
		Optional<Membership> entity = membershipRepository.findByBrandUuidAndUuidAndDeletedIsFalse(brandUuid, membershipUuid);
		if (entity.isEmpty()) {
			throw new MembershipException(requestContext.getTrackingNumber(), MembershipException.MEMBERSHIP_NOT_FOUND, "Membership not found");
		}

		return entity.get();
	}
}