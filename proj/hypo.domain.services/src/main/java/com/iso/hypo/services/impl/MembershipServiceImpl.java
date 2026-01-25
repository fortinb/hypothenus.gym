package com.iso.hypo.services.impl;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.iso.hypo.common.context.RequestContext;
import com.iso.hypo.domain.aggregate.Membership;
import com.iso.hypo.domain.dto.MembershipDto;
import com.iso.hypo.repositories.MembershipRepository;
import com.iso.hypo.services.BrandQueryService;
import com.iso.hypo.services.MembershipService;
import com.iso.hypo.services.exception.BrandException;
import com.iso.hypo.services.exception.MembershipException;
import com.iso.hypo.services.mappers.MembershipMapper;

@Service
public class MembershipServiceImpl implements MembershipService {

	private final BrandQueryService brandQueryService;;
	
	private final MembershipRepository membershipRepository;

	private final MembershipMapper membershipMapper;

	// replace field-injected logger with static logger
	private static final Logger logger = LoggerFactory.getLogger(MembershipServiceImpl.class);

	private final RequestContext requestContext;
	
	public MembershipServiceImpl(MembershipMapper membershipMapper, MembershipRepository membershipRepository, BrandQueryService brandQueryService, RequestContext requestContext) {
		this.membershipMapper = membershipMapper;
		this.membershipRepository = membershipRepository;
		this.brandQueryService = brandQueryService;
		this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
	}

	@Override
	public MembershipDto create(MembershipDto membershipDto) throws MembershipException {
		try {
			Assert.notNull(membershipDto, "membershipDto must not be null");
			Membership membership = membershipMapper.toEntity(membershipDto);

			brandQueryService.assertExists(membership.getBrandUuid());
			
			membership.setCreatedOn(Instant.now());
			membership.setCreatedBy(requestContext.getUsername());
			
			Membership saved = membershipRepository.save(membership);
			return membershipMapper.toDto(saved);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}", membershipDto != null ? membershipDto.getBrandUuid() : null, e);
			
			if (e instanceof BrandException) {
				throw new MembershipException(requestContext.getTrackingNumber(), MembershipException.BRAND_NOT_FOUND, "Brand not found");
			}
			if (e instanceof MembershipException) {
				throw (MembershipException) e;
			}
			throw new MembershipException(requestContext.getTrackingNumber(), MembershipException.CREATION_FAILED, e);
		}
	}

	@Override
	public MembershipDto update(MembershipDto membershipDto) throws MembershipException {
		try {
			Assert.notNull(membershipDto, "membershipDto must not be null");
			Membership membership = membershipMapper.toEntity(membershipDto);
			
			Membership oldMembership = this.readByMembershipUuid(membership.getBrandUuid(), membership.getUuid());

			ModelMapper mapper = new ModelMapper();
			mapper.getConfiguration().setSkipNullEnabled(false);
			
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

	@Override
	public MembershipDto patch(MembershipDto membershipDto) throws MembershipException {
		try {
			Assert.notNull(membershipDto, "membershipDto must not be null");
			Membership membership = membershipMapper.toEntity(membershipDto);

			Membership oldMembership = this.readByMembershipUuid(membership.getBrandUuid(), membership.getUuid());
		
			ModelMapper mapper = new ModelMapper();
			mapper.getConfiguration().setSkipNullEnabled(true);
			
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

	@Override
	public void delete(String brandUuid, String membershipUuid) throws MembershipException {
		try {
			Membership entity = this.readByMembershipUuid(brandUuid,  membershipUuid);
			entity.setDeleted(true);

			entity.setDeletedOn(Instant.now());
			entity.setDeletedBy(requestContext.getUsername());
			
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
	public MembershipDto activate(String brandUuid, String membershipUuid) throws MembershipException {
		try {
			Optional<Membership> entity = membershipRepository.activate(brandUuid, membershipUuid);
			if (entity.isEmpty()) {
				throw new MembershipException(requestContext.getTrackingNumber(), MembershipException.MEMBERSHIP_NOT_FOUND, "Membership not found");
			}
			
			return membershipMapper.toDto(entity.get());
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, membershipUuid={}", brandUuid, membershipUuid, e);
			
			if (e instanceof MembershipException) {
				throw (MembershipException) e;
			}
			throw new MembershipException(requestContext.getTrackingNumber(), MembershipException.ACTIVATION_FAILED, e);
		}
	}

	@Override
	public MembershipDto deactivate(String brandUuid, String membershipUuid) throws MembershipException {
		try {
			Optional<Membership> entity = membershipRepository.deactivate(brandUuid, membershipUuid);
			if (entity.isEmpty()) {
				throw new MembershipException(requestContext.getTrackingNumber(), MembershipException.MEMBERSHIP_NOT_FOUND, "Membership not found");
			}
			
			return membershipMapper.toDto(entity.get());
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, membershipUuid={}", brandUuid, membershipUuid, e);
			
			if (e instanceof MembershipException) {
				throw (MembershipException) e;
			}
			throw new MembershipException(requestContext.getTrackingNumber(), MembershipException.DEACTIVATION_FAILED, e);
		}
	}

	private Membership readByMembershipUuid(String brandUuid, String membershipUuid) throws MembershipException {
		Optional<Membership> entity = membershipRepository.findByBrandUuidAndUuidAndIsDeletedIsFalse(brandUuid, membershipUuid);
		if (entity.isEmpty()) {
			throw new MembershipException(requestContext.getTrackingNumber(), MembershipException.MEMBERSHIP_NOT_FOUND, "Membership not found");
		}

		return entity.get();
	}
}