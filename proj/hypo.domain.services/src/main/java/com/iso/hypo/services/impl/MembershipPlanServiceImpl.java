package com.iso.hypo.services.impl;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iso.hypo.common.context.RequestContext;
import com.iso.hypo.domain.aggregate.Brand;
import com.iso.hypo.domain.aggregate.MembershipPlan;
import com.iso.hypo.domain.dto.MembershipPlanDto;
import com.iso.hypo.repositories.BrandRepository;
import com.iso.hypo.repositories.MembershipPlanRepository;
import com.iso.hypo.services.MembershipPlanService;
import com.iso.hypo.services.exception.MembershipPlanException;
import com.iso.hypo.services.mappers.MembershipPlanMapper;

@Service
public class MembershipPlanServiceImpl implements MembershipPlanService {

	private BrandRepository brandRepository;
	
	private MembershipPlanRepository membershipPlanRepository;

	private MembershipPlanMapper membershipPlanMapper;

	@Autowired
	private RequestContext requestContext;
	
	public MembershipPlanServiceImpl(BrandRepository brandRepository, MembershipPlanRepository membershipPlanRepository, MembershipPlanMapper membershipPlanMapper) {
		this.brandRepository = brandRepository;
		this.membershipPlanRepository = membershipPlanRepository;
		this.membershipPlanMapper = membershipPlanMapper;
	}

	@Override
	public MembershipPlanDto create(String brandUuid, MembershipPlanDto membershipPlanDto) throws MembershipPlanException {
		Optional<Brand> existingBrand = brandRepository.findByUuidAndIsDeletedIsFalse(membershipPlanDto.getBrandUuid());
		if (!existingBrand.isPresent()) {
			throw new MembershipPlanException(MembershipPlanException.BRAND_NOT_FOUND, "Brand not found");
		}
		
		MembershipPlan membershipPlan = membershipPlanMapper.toEntity(membershipPlanDto);
		if (!membershipPlan.getBrandUuid().equals(brandUuid)) {
			throw new MembershipPlanException(MembershipPlanException.INVALID_BRAND, "Invalid brand");
		}
		
		membershipPlan.setCreatedOn(Instant.now());
		membershipPlan.setCreatedBy(requestContext.getUsername());
		membershipPlan.setUuid(UUID.randomUUID().toString());
		
		MembershipPlan saved = membershipPlanRepository.save(membershipPlan);
		return membershipPlanMapper.toDto(saved);
	}

	@Override
	public MembershipPlanDto update(String brandUuid, MembershipPlanDto membershipPlanDto) throws MembershipPlanException {
		MembershipPlan membershipPlan = membershipPlanMapper.toEntity(membershipPlanDto);
		if (!membershipPlan.getBrandUuid().equals(brandUuid)) {
			throw new MembershipPlanException(MembershipPlanException.INVALID_BRAND, "Invalid brand");
		}
		
		MembershipPlan oldMembershipPlan = this.readByMembershipPlanUuid(brandUuid, membershipPlan.getUuid());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(false);
		
		mapper = membershipPlanMapper.initMembershipPlanMappings(mapper);
		mapper.map(membershipPlan, oldMembershipPlan);

		oldMembershipPlan.setModifiedOn(Instant.now());
		oldMembershipPlan.setModifiedBy(requestContext.getUsername());
		
		MembershipPlan saved = membershipPlanRepository.save(oldMembershipPlan);
		return membershipPlanMapper.toDto(saved);
	}

	@Override
	public MembershipPlanDto patch(String brandUuid, MembershipPlanDto membershipPlanDto) throws MembershipPlanException {
		MembershipPlan membershipPlan = membershipPlanMapper.toEntity(membershipPlanDto);
		if (!membershipPlan.getBrandUuid().equals(brandUuid)) {
			throw new MembershipPlanException(MembershipPlanException.INVALID_BRAND, "Invalid brand");
		}
		
		MembershipPlan oldMembershipPlan = this.readByMembershipPlanUuid(brandUuid, membershipPlan.getUuid());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(true);
		
		mapper = membershipPlanMapper.initMembershipPlanMappings(mapper);
		mapper.map(membershipPlan, oldMembershipPlan);
		
		oldMembershipPlan.setModifiedOn(Instant.now());
		oldMembershipPlan.setModifiedBy(requestContext.getUsername());
		
		MembershipPlan saved = membershipPlanRepository.save(oldMembershipPlan);
		return membershipPlanMapper.toDto(saved);
	}

	@Override
	public void delete(String brandUuid, String membershipPlanUuid) throws MembershipPlanException {
		MembershipPlan entity = this.readByMembershipPlanUuid(brandUuid,  membershipPlanUuid);
		entity.setDeleted(true);

		entity.setDeletedOn(Instant.now());
		entity.setDeletedBy(requestContext.getUsername());
		
		membershipPlanRepository.save(entity);
	}
	
	@Override
	public MembershipPlanDto activate(String brandUuid, String membershipPlanUuid) throws MembershipPlanException {
		Optional<MembershipPlan> entity = membershipPlanRepository.activate(brandUuid, membershipPlanUuid);
		if (entity.isEmpty()) {
			throw new MembershipPlanException(MembershipPlanException.MEMBERSHIPPLAN_NOT_FOUND, "MembershipPlan not found");
		}
		
		return membershipPlanMapper.toDto(entity.get());
	}
	
	@Override
	public MembershipPlanDto deactivate(String brandUuid, String membershipPlanUuid) throws MembershipPlanException {
		Optional<MembershipPlan> entity = membershipPlanRepository.deactivate(brandUuid, membershipPlanUuid);
		if (entity.isEmpty()) {
			throw new MembershipPlanException(MembershipPlanException.MEMBERSHIPPLAN_NOT_FOUND, "MembershipPlan not found");
		}
		
		return membershipPlanMapper.toDto(entity.get());
	}
	
	private MembershipPlan readByMembershipPlanUuid(String brandUuid, String membershipPlanUuid) throws MembershipPlanException {
		Optional<MembershipPlan> entity = membershipPlanRepository.findByBrandUuidAndUuidAndIsDeletedIsFalse(brandUuid, membershipPlanUuid);
		if (entity.isEmpty()) {
			throw new MembershipPlanException(MembershipPlanException.MEMBERSHIPPLAN_NOT_FOUND, "MembershipPlan not found");
		}

		return entity.get();
	}
}