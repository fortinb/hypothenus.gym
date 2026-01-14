package com.iso.hypo.brand.services.impl;

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
import com.iso.hypo.brand.exception.BrandException;
import com.iso.hypo.common.domain.LocalizedString;
import com.iso.hypo.gym.domain.aggregate.Course;
import com.iso.hypo.gym.domain.aggregate.Gym;
import com.iso.hypo.brand.domain.aggregate.MembershipPlan;
import com.iso.hypo.common.domain.pricing.Cost;
import com.iso.hypo.common.domain.pricing.OneTimeFee;
import com.iso.hypo.brand.repository.MembershipPlanRepository;
import com.iso.hypo.brand.dto.MembershipPlanDto;
import com.iso.hypo.brand.mappers.BrandMapper;
import com.iso.hypo.brand.services.MembershipPlanService;

@Service
public class MembershipPlanServiceImpl implements MembershipPlanService {

	private MembershipPlanRepository membershipPlanRepository;

	private BrandMapper brandMapper;

	@Autowired
	private RequestContext requestContext;
	
	public MembershipPlanServiceImpl(MembershipPlanRepository membershipPlanRepository, BrandMapper brandMapper) {
		this.membershipPlanRepository = membershipPlanRepository;
		this.brandMapper = brandMapper;
	}

	@Override
	public MembershipPlanDto create(String brandId, MembershipPlanDto membershipPlanDto) throws BrandException {
		MembershipPlan membershipPlan = brandMapper.toEntity(membershipPlanDto);
		if (!membershipPlan.getBrandId().equals(brandId)) {
			throw new BrandException(BrandException.INVALID_BRAND, "Invalid brand");
		}
		
		membershipPlan.setCreatedOn(Instant.now());
		membershipPlan.setCreatedBy(requestContext.getUsername());
		membershipPlan.setUuid(UUID.randomUUID().toString());
		
		MembershipPlan saved = membershipPlanRepository.save(membershipPlan);
		return brandMapper.toDto(saved);
	}

	@Override
	public MembershipPlanDto update(String brandId, MembershipPlanDto membershipPlanDto) throws BrandException {
		MembershipPlan membershipPlan = brandMapper.toEntity(membershipPlanDto);
		if (!membershipPlan.getBrandId().equals(brandId)) {
			throw new BrandException(BrandException.INVALID_BRAND, "Invalid brand");
		}
		
		MembershipPlan oldMembershipPlan = this.readByMembershipPlanUuid(brandId, membershipPlan.getUuid());

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
	public MembershipPlanDto patch(String brandId, MembershipPlanDto membershipPlanDto) throws BrandException {
		MembershipPlan membershipPlan = brandMapper.toEntity(membershipPlanDto);
		if (!membershipPlan.getBrandId().equals(brandId)) {
			throw new BrandException(BrandException.INVALID_BRAND, "Invalid brand");
		}
		
		MembershipPlan oldMembershipPlan = this.readByMembershipPlanUuid(brandId, membershipPlan.getUuid());

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
	public void delete(String brandId, String membershipPlanUuid) throws BrandException {
		MembershipPlan entity = this.readByMembershipPlanUuid(brandId,  membershipPlanUuid);
		entity.setDeleted(true);

		entity.setDeletedOn(Instant.now());
		entity.setDeletedBy(requestContext.getUsername());
		
		membershipPlanRepository.save(entity);
	}

	@Override
	public MembershipPlanDto findByMembershipPlanUuid(String brandId, String membershipPlanUuid) throws BrandException {
		return brandMapper.toDto(readByMembershipPlanUuid(brandId, membershipPlanUuid));
	}

	@Override
	public Page<MembershipPlanDto> list(String brandId, int page, int pageSize, boolean includeInactive) throws BrandException {
		Page<MembershipPlan> pageEntities;
		if (includeInactive) {
			pageEntities = membershipPlanRepository.findAllByBrandIdAndIsDeletedIsFalse(brandId, PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname"));
		} else {
			pageEntities = membershipPlanRepository.findAllByBrandIdAndIsDeletedIsFalseAndIsActiveIsTrue(brandId, PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname"));
		}

		return pageEntities.map(e -> brandMapper.toDto(e));
	}
	
	@Override
	public MembershipPlanDto activate(String brandId, String membershipPlanUuid) throws BrandException {
		Optional<MembershipPlan> entity = membershipPlanRepository.activate(brandId, membershipPlanUuid);
		if (entity.isEmpty()) {
			throw new BrandException(BrandException.MEMBERSHIPPLAN_NOT_FOUND, "MembershipPlan not found");
		}
		
		return brandMapper.toDto(entity.get());
	}
	
	@Override
	public MembershipPlanDto deactivate(String brandId, String membershipPlanUuid) throws BrandException {
		Optional<MembershipPlan> entity = membershipPlanRepository.deactivate(brandId, membershipPlanUuid);
		if (entity.isEmpty()) {
			throw new BrandException(BrandException.MEMBERSHIPPLAN_NOT_FOUND, "MembershipPlan not found");
		}
		
		return brandMapper.toDto(entity.get());
	}
	
	private MembershipPlan readByMembershipPlanUuid(String brandId, String membershipPlanUuid) throws BrandException {
		Optional<MembershipPlan> entity = membershipPlanRepository.findByBrandIdAndUuidAndIsDeletedIsFalse(brandId, membershipPlanUuid);
		if (entity.isEmpty()) {
			throw new BrandException(BrandException.MEMBERSHIPPLAN_NOT_FOUND, "MembershipPlan not found");
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
