package com.iso.hypo.services.impl;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.iso.hypo.common.context.RequestContext;
import com.iso.hypo.domain.aggregate.Brand;
import com.iso.hypo.domain.aggregate.Course;
import com.iso.hypo.domain.aggregate.Gym;
import com.iso.hypo.domain.aggregate.MembershipPlan;
import com.iso.hypo.domain.LocalizedString;
import com.iso.hypo.domain.pricing.Cost;
import com.iso.hypo.domain.pricing.OneTimeFee;
import com.iso.hypo.domain.dto.MembershipPlanDto;
import com.iso.hypo.services.exception.MembershipPlanException;
import com.iso.hypo.services.mappers.BrandMapper;
import com.iso.hypo.repositories.BrandRepository;
import com.iso.hypo.repositories.MembershipPlanRepository;
import com.iso.hypo.services.MembershipPlanService;

@Service
public class MembershipPlanServiceImpl implements MembershipPlanService {

	private BrandRepository brandRepository;
	
	private MembershipPlanRepository membershipPlanRepository;

	private BrandMapper brandMapper;

	@Autowired
	private RequestContext requestContext;
	
	public MembershipPlanServiceImpl(BrandRepository brandRepository, MembershipPlanRepository membershipPlanRepository, BrandMapper brandMapper) {
		this.brandRepository = brandRepository;
		this.membershipPlanRepository = membershipPlanRepository;
		this.brandMapper = brandMapper;
	}

	@Override
	public MembershipPlanDto create(String brandUuid, MembershipPlanDto membershipPlanDto) throws MembershipPlanException {
		Optional<Brand> existingBrand = brandRepository.findByUuidAndIsDeletedIsFalse(membershipPlanDto.getBrandUuid());
		if (!existingBrand.isPresent()) {
			throw new MembershipPlanException(MembershipPlanException.BRAND_NOT_FOUND, "Brand not found");
		}
		
		MembershipPlan membershipPlan = brandMapper.toEntity(membershipPlanDto);
		if (!membershipPlan.getBrandUuid().equals(brandUuid)) {
			throw new MembershipPlanException(MembershipPlanException.INVALID_BRAND, "Invalid brand");
		}
		
		membershipPlan.setCreatedOn(Instant.now());
		membershipPlan.setCreatedBy(requestContext.getUsername());
		membershipPlan.setUuid(UUID.randomUUID().toString());
		
		MembershipPlan saved = membershipPlanRepository.save(membershipPlan);
		return brandMapper.toDto(saved);
	}

	@Override
	public MembershipPlanDto update(String brandUuid, MembershipPlanDto membershipPlanDto) throws MembershipPlanException {
		MembershipPlan membershipPlan = brandMapper.toEntity(membershipPlanDto);
		if (!membershipPlan.getBrandUuid().equals(brandUuid)) {
			throw new MembershipPlanException(MembershipPlanException.INVALID_BRAND, "Invalid brand");
		}
		
		MembershipPlan oldMembershipPlan = this.readByMembershipPlanUuid(brandUuid, membershipPlan.getUuid());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(false);
		
		mapper = initMembershipPlanMappings(mapper);
		mapper.map(membershipPlan, oldMembershipPlan);

		oldMembershipPlan.setModifiedOn(Instant.now());
		oldMembershipPlan.setModifiedBy(requestContext.getUsername());
		
		MembershipPlan saved = membershipPlanRepository.save(oldMembershipPlan);
		return brandMapper.toDto(saved);
	}

	@Override
	public MembershipPlanDto patch(String brandUuid, MembershipPlanDto membershipPlanDto) throws MembershipPlanException {
		MembershipPlan membershipPlan = brandMapper.toEntity(membershipPlanDto);
		if (!membershipPlan.getBrandUuid().equals(brandUuid)) {
			throw new MembershipPlanException(MembershipPlanException.INVALID_BRAND, "Invalid brand");
		}
		
		MembershipPlan oldMembershipPlan = this.readByMembershipPlanUuid(brandUuid, membershipPlan.getUuid());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(true);
		
		mapper = initMembershipPlanMappings(mapper);
		mapper.map(membershipPlan, oldMembershipPlan);
		
		oldMembershipPlan.setModifiedOn(Instant.now());
		oldMembershipPlan.setModifiedBy(requestContext.getUsername());
		
		MembershipPlan saved = membershipPlanRepository.save(oldMembershipPlan);
		return brandMapper.toDto(saved);
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
	public MembershipPlanDto findByMembershipPlanUuid(String brandUuid, String membershipPlanUuid) throws MembershipPlanException {
		return brandMapper.toDto(readByMembershipPlanUuid(brandUuid, membershipPlanUuid));
	}

	@Override
	public Page<MembershipPlanDto> list(String brandUuid, int page, int pageSize, boolean includeInactive) throws MembershipPlanException {
		Page<MembershipPlan> pageEntities;
		if (includeInactive) {
			pageEntities = membershipPlanRepository.findAllByBrandUuidAndIsDeletedIsFalse(brandUuid, PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname"));
		} else {
			pageEntities = membershipPlanRepository.findAllByBrandUuidAndIsDeletedIsFalseAndIsActiveIsTrue(brandUuid, PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname"));
		}

		return pageEntities.map(e -> brandMapper.toDto(e));
	}
	
	@Override
	public MembershipPlanDto activate(String brandUuid, String membershipPlanUuid) throws MembershipPlanException {
		Optional<MembershipPlan> entity = membershipPlanRepository.activate(brandUuid, membershipPlanUuid);
		if (entity.isEmpty()) {
			throw new MembershipPlanException(MembershipPlanException.MEMBERSHIPPLAN_NOT_FOUND, "MembershipPlan not found");
		}
		
		return brandMapper.toDto(entity.get());
	}
	
	@Override
	public MembershipPlanDto deactivate(String brandUuid, String membershipPlanUuid) throws MembershipPlanException {
		Optional<MembershipPlan> entity = membershipPlanRepository.deactivate(brandUuid, membershipPlanUuid);
		if (entity.isEmpty()) {
			throw new MembershipPlanException(MembershipPlanException.MEMBERSHIPPLAN_NOT_FOUND, "MembershipPlan not found");
		}
		
		return brandMapper.toDto(entity.get());
	}
	
	private MembershipPlan readByMembershipPlanUuid(String brandUuid, String membershipPlanUuid) throws MembershipPlanException {
		Optional<MembershipPlan> entity = membershipPlanRepository.findByBrandUuidAndUuidAndIsDeletedIsFalse(brandUuid, membershipPlanUuid);
		if (entity.isEmpty()) {
			throw new MembershipPlanException(MembershipPlanException.MEMBERSHIPPLAN_NOT_FOUND, "MembershipPlan not found");
		}

		return entity.get();
	}

	private ModelMapper initMembershipPlanMappings(ModelMapper mapper) {
		PropertyMap<MembershipPlan, MembershipPlan> membershipPlanPropertyMap = new PropertyMap<MembershipPlan, MembershipPlan>()
	    {
	        protected void configure()
	        {
	            skip().setId(null);
	            skip().setActive(false);
	            skip().setActivatedOn(null);
	            skip().setDeactivatedOn(null);
	        }
	    };
	    
	    PropertyMap<LocalizedString, LocalizedString> localizedStringPropertyMap = new PropertyMap<LocalizedString, LocalizedString>() {
			@Override
			protected void configure() {
			}
		};
		
	    PropertyMap<Course, Course> coursePropertyMap = new PropertyMap<Course, Course>() {
			@Override
			protected void configure() {
			}
		};
		
	    PropertyMap<OneTimeFee, OneTimeFee> oneTimeFeePropertyMap = new PropertyMap<OneTimeFee, OneTimeFee>() {
			@Override
			protected void configure() {
			}
		};
		
	    PropertyMap<Gym, Gym> includedGymsPropertyMap = new PropertyMap<Gym, Gym>() {
			@Override
			protected void configure() {
			}
		};

	    PropertyMap<Cost, Cost> costPropertyMap = new PropertyMap<Cost, Cost>() {
			@Override
			protected void configure() {
			}
		};
		
		mapper.addMappings(membershipPlanPropertyMap);
		mapper.addMappings(localizedStringPropertyMap);
		mapper.addMappings(coursePropertyMap);
		mapper.addMappings(oneTimeFeePropertyMap);
		mapper.addMappings(includedGymsPropertyMap);
		mapper.addMappings(costPropertyMap);
		
		return mapper;
	}
}


