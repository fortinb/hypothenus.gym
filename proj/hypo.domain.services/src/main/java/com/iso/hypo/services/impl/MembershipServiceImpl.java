package com.iso.hypo.services.impl;

import java.time.Instant;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

	private BrandQueryService brandQueryService;;
	
	private MembershipRepository membershipRepository;

	private MembershipMapper membershipMapper;

	@Autowired
	private Logger logger;
	
	@Autowired
	private RequestContext requestContext;
	
	public MembershipServiceImpl(BrandQueryService brandQueryService, MembershipRepository membershipRepository, MembershipMapper membershipMapper) {
		this.brandQueryService = brandQueryService;
		this.membershipRepository = membershipRepository;
		this.membershipMapper = membershipMapper;
	}

	@Override
	public MembershipDto create(String brandUuid, MembershipDto membershipDto) throws MembershipException {
		try {
			brandQueryService.assertExists(brandUuid);
			
			Membership membership = membershipMapper.toEntity(membershipDto);
			if (!membership.getBrandUuid().equals(brandUuid)) {
				throw new MembershipException(MembershipException.INVALID_BRAND, "Invalid brand");
			}
			
			membership.setCreatedOn(Instant.now());
			membership.setCreatedBy(requestContext.getUsername());
			
			Membership saved = membershipRepository.save(membership);
			return membershipMapper.toDto(saved);
		} catch (BrandException e) {
			throw new MembershipException(MembershipException.BRAND_NOT_FOUND, "Brand not found");
		} catch (MembershipException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Unhandled error", e);
			throw new MembershipException(MembershipException.CREATION_FAILED, e);
		}
	}

	@Override
	public MembershipDto update(String brandUuid, MembershipDto membershipDto) throws MembershipException {
		Membership membership = membershipMapper.toEntity(membershipDto);
		if (!membership.getBrandUuid().equals(brandUuid)) {
			throw new MembershipException(MembershipException.INVALID_BRAND, "Invalid brand");
		}
		
		Membership oldMembership = this.readByMembershipUuid(brandUuid, membership.getUuid());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(false);
		
		mapper = membershipMapper.initMembershipMappings(mapper);
		mapper.map(membership, oldMembership);

		oldMembership.setModifiedOn(Instant.now());
		oldMembership.setModifiedBy(requestContext.getUsername());
		
		Membership saved = membershipRepository.save(oldMembership);
		return membershipMapper.toDto(saved);
	}

	@Override
	public MembershipDto patch(String brandUuid, MembershipDto membershipDto) throws MembershipException {
		Membership membership = membershipMapper.toEntity(membershipDto);
		if (!membership.getBrandUuid().equals(brandUuid)) {
			throw new MembershipException(MembershipException.INVALID_BRAND, "Invalid brand");
		}
		
		Membership oldMembership = this.readByMembershipUuid(brandUuid, membership.getUuid());
	
		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(true);
		
		mapper = membershipMapper.initMembershipMappings(mapper);
		mapper.map(membership, oldMembership);
		
		oldMembership.setModifiedOn(Instant.now());
		oldMembership.setModifiedBy(requestContext.getUsername());
		
		Membership saved = membershipRepository.save(oldMembership);
		return membershipMapper.toDto(saved);
	}

	@Override
	public void delete(String brandUuid, String membershipUuid) throws MembershipException {
		Membership entity = this.readByMembershipUuid(brandUuid,  membershipUuid);
		entity.setDeleted(true);

		entity.setDeletedOn(Instant.now());
		entity.setDeletedBy(requestContext.getUsername());
		
		membershipRepository.save(entity);
	}
	
	@Override
	public MembershipDto activate(String brandUuid, String membershipUuid) throws MembershipException {
		Optional<Membership> entity = membershipRepository.activate(brandUuid, membershipUuid);
		if (entity.isEmpty()) {
			throw new MembershipException(MembershipException.MEMBERSHIP_NOT_FOUND, "Membership not found");
		}
		
		return membershipMapper.toDto(entity.get());
	}
	
	@Override
	public MembershipDto deactivate(String brandUuid, String membershipUuid) throws MembershipException {
			Optional<Membership> entity = membershipRepository.deactivate(brandUuid, membershipUuid);
		if (entity.isEmpty()) {
			throw new MembershipException(MembershipException.MEMBERSHIP_NOT_FOUND, "Membership not found");
		}
		
		return membershipMapper.toDto(entity.get());
	}
	
	private Membership readByMembershipUuid(String brandUuid, String membershipUuid) throws MembershipException {
		Optional<Membership> entity = membershipRepository.findByBrandUuidAndUuidAndIsDeletedIsFalse(brandUuid, membershipUuid);
		if (entity.isEmpty()) {
			throw new MembershipException(MembershipException.MEMBERSHIP_NOT_FOUND, "Membership not found");
		}

		return entity.get();
	}
}