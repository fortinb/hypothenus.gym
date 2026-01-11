package com.iso.hypo.brand.services;

import java.time.Instant;
import java.util.Optional;

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

@Service
public class MembershipPlanService {

	private MembershipPlanRepository membershipPlanRepository;

	private BrandMapper brandMapper;

	@Autowired
	private RequestContext requestContext;
	
	public MembershipPlanService(MembershipPlanRepository membershipPlanRepository, BrandMapper brandMapper) {
		this.membershipPlanRepository = membershipPlanRepository;
		this.brandMapper = brandMapper;
	}

	public MembershipPlanDto create(String brandId, MembershipPlanDto membershipPlanDto) throws BrandException {
		MembershipPlan membershipPlan = brandMapper.toEntity(membershipPlanDto);
		if (!membershipPlan.getBrandId().equals(brandId)) {
			throw new BrandException(BrandException.INVALID_BRAND, "Invalid brand");
		}
		
		membershipPlan.setCreatedOn(Instant.now());
		membershipPlan.setCreatedBy(requestContext.getUsername());
		
		MembershipPlan saved = membershipPlanRepository.save(membershipPlan);
		return brandMapper.toDto(saved);
	}

	public MembershipPlanDto update(String brandId, MembershipPlanDto membershipPlanDto) throws BrandException {
		MembershipPlan membershipPlan = brandMapper.toEntity(membershipPlanDto);
		if (!membershipPlan.getBrandId().equals(brandId)) {
			throw new BrandException(BrandException.INVALID_BRAND, "Invalid brand");
		}
		
		MembershipPlan oldMembershipPlan = this.findByMembershipPlanIdEntity(brandId, membershipPlan.getId());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(false);
		
		mapper = initMembershipPlanMappings(mapper);
		mapper.map(membershipPlan, oldMembershipPlan);

		oldMembershipPlan.setModifiedOn(Instant.now());
		oldMembershipPlan.setModifiedBy(requestContext.getUsername());
		
		MembershipPlan saved = membershipPlanRepository.save(oldMembershipPlan);
		return brandMapper.toDto(saved);
	}

	public MembershipPlanDto patch(String brandId, MembershipPlanDto membershipPlanDto) throws BrandException {
		MembershipPlan membershipPlan = brandMapper.toEntity(membershipPlanDto);
		if (!membershipPlan.getBrandId().equals(brandId)) {
			throw new BrandException(BrandException.INVALID_BRAND, "Invalid brand");
		}
		
		MembershipPlan oldMembershipPlan = this.findByMembershipPlanIdEntity(brandId, membershipPlan.getId());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(true);
		
		mapper = initMembershipPlanMappings(mapper);
		mapper.map(membershipPlan, oldMembershipPlan);
		
		oldMembershipPlan.setModifiedOn(Instant.now());
		oldMembershipPlan.setModifiedBy(requestContext.getUsername());
		
		MembershipPlan saved = membershipPlanRepository.save(oldMembershipPlan);
		return brandMapper.toDto(saved);
	}

	public void delete(String brandId, String membershipPlanId) throws BrandException {
		MembershipPlan oldMembershipPlan = this.findByMembershipPlanIdEntity(brandId,  membershipPlanId);
		oldMembershipPlan.setDeleted(true);

		oldMembershipPlan.setDeletedOn(Instant.now());
		oldMembershipPlan.setDeletedBy(requestContext.getUsername());
		
		membershipPlanRepository.save(oldMembershipPlan);
	}

	public MembershipPlanDto findByMembershipPlanId(String brandId, String id) throws BrandException {
		MembershipPlan entity = findByMembershipPlanIdEntity(brandId, id);
		return brandMapper.toDto(entity);
	}

	private MembershipPlan findByMembershipPlanIdEntity(String brandId, String id) throws BrandException {
		Optional<MembershipPlan> entity = membershipPlanRepository.findByBrandIdAndIdAndIsDeletedIsFalse(brandId, id);
		if (entity.isEmpty()) {
			throw new BrandException(BrandException.MEMBERSHIPPLAN_NOT_FOUND, "MembershipPlan not found");
		}

		return entity.get();
	}

	public Page<MembershipPlanDto> list(String brandId, int page, int pageSize, boolean includeInactive) throws BrandException {
		Page<MembershipPlan> pageEntities;
		if (includeInactive) {
			pageEntities = membershipPlanRepository.findAllByBrandIdAndIsDeletedIsFalse(brandId, PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname"));
		} else {
			pageEntities = membershipPlanRepository.findAllByBrandIdAndIsDeletedIsFalseAndIsActiveIsTrue(brandId, PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname"));
		}

		return pageEntities.map(e -> brandMapper.toDto(e));
	}
	
	public MembershipPlanDto activate(String brandId, String id) throws BrandException {
		
		Optional<MembershipPlan> oldMembershipPlan = membershipPlanRepository.activate(brandId, id);
		if (oldMembershipPlan.isEmpty()) {
			throw new BrandException(BrandException.MEMBERSHIPPLAN_NOT_FOUND, "MembershipPlan not found");
		}
		
		return brandMapper.toDto(oldMembershipPlan.get());
	}
	
	public MembershipPlanDto deactivate(String brandId, String id) throws BrandException {
		
		Optional<MembershipPlan> oldMembershipPlan = membershipPlanRepository.deactivate(brandId, id);
		if (oldMembershipPlan.isEmpty()) {
			throw new BrandException(BrandException.MEMBERSHIPPLAN_NOT_FOUND, "MembershipPlan not found");
		}
		
		return brandMapper.toDto(oldMembershipPlan.get());
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