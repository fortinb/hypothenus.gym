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

@Service
public class MembershipPlanService {

	private MembershipPlanRepository membershipPlanRepository;

	@Autowired
	private RequestContext requestContext;
	
	public MembershipPlanService(MembershipPlanRepository membershipPlanRepository) {
		this.membershipPlanRepository = membershipPlanRepository;
	}

	public MembershipPlan create(String brandId, MembershipPlan membershipPlan) throws BrandException {
		if (!membershipPlan.getBrandId().equals(brandId)) {
			throw new BrandException(BrandException.INVALID_BRAND, "Invalid brand");
		}
		
		membershipPlan.setCreatedOn(Instant.now());
		membershipPlan.setCreatedBy(requestContext.getUsername());
		
		return membershipPlanRepository.save(membershipPlan);
	}

	public MembershipPlan update(String brandId, MembershipPlan membershipPlan) throws BrandException {
		if (!membershipPlan.getBrandId().equals(brandId)) {
			throw new BrandException(BrandException.INVALID_BRAND, "Invalid brand");
		}
		
		MembershipPlan oldMembershipPlan = this.findByMembershipPlanId(brandId, membershipPlan.getId());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(false);
		
		mapper = initMembershipPlanMappings(mapper);
		mapper.map(membershipPlan, oldMembershipPlan);

		oldMembershipPlan.setModifiedOn(Instant.now());
		oldMembershipPlan.setModifiedBy(requestContext.getUsername());
		
		return membershipPlanRepository.save(oldMembershipPlan);
	}

	public MembershipPlan patch(String brandId, MembershipPlan membershipPlan) throws BrandException {
		if (!membershipPlan.getBrandId().equals(brandId)) {
			throw new BrandException(BrandException.INVALID_BRAND, "Invalid brand");
		}
		
		MembershipPlan oldMembershipPlan = this.findByMembershipPlanId(brandId, membershipPlan.getId());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(true);
		
		mapper = initMembershipPlanMappings(mapper);
		mapper.map(membershipPlan, oldMembershipPlan);
		
		oldMembershipPlan.setModifiedOn(Instant.now());
		oldMembershipPlan.setModifiedBy(requestContext.getUsername());
		
		return membershipPlanRepository.save(oldMembershipPlan);
	}

	public void delete(String brandId, String membershipPlanId) throws BrandException {
		MembershipPlan oldMembershipPlan = this.findByMembershipPlanId(brandId,  membershipPlanId);
		oldMembershipPlan.setDeleted(true);

		oldMembershipPlan.setDeletedOn(Instant.now());
		oldMembershipPlan.setDeletedBy(requestContext.getUsername());
		
		membershipPlanRepository.save(oldMembershipPlan);
	}

	public MembershipPlan findByMembershipPlanId(String brandId, String id) throws BrandException {
		Optional<MembershipPlan> entity = membershipPlanRepository.findByBrandIdAndIdAndIsDeletedIsFalse(brandId, id);
		if (entity.isEmpty()) {
			throw new BrandException(BrandException.MEMBERSHIPPLAN_NOT_FOUND, "MembershipPlan not found");
		}

		return entity.get();
	}

	public Page<MembershipPlan> list(String brandId, int page, int pageSize, boolean includeInactive) throws BrandException {
		if (includeInactive) {
			return membershipPlanRepository.findAllByBrandIdAndIsDeletedIsFalse(brandId, PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname"));
		}

		return membershipPlanRepository.findAllByBrandIdAndIsDeletedIsFalseAndIsActiveIsTrue(brandId, PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname"));
	}
	
	public MembershipPlan activate(String brandId, String id) throws BrandException {
		
		Optional<MembershipPlan> oldMembershipPlan = membershipPlanRepository.activate(brandId, id);
		if (oldMembershipPlan.isEmpty()) {
			throw new BrandException(BrandException.MEMBERSHIPPLAN_NOT_FOUND, "MembershipPlan not found");
		}
		
		return oldMembershipPlan.get();
	}
	
	public MembershipPlan deactivate(String brandId, String id) throws BrandException {
		
		Optional<MembershipPlan> oldMembershipPlan = membershipPlanRepository.deactivate(brandId, id);
		if (oldMembershipPlan.isEmpty()) {
			throw new BrandException(BrandException.MEMBERSHIPPLAN_NOT_FOUND, "MembershipPlan not found");
		}
		
		return oldMembershipPlan.get();
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
