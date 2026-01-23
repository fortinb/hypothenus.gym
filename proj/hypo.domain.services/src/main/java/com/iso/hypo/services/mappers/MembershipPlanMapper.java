package com.iso.hypo.services.mappers;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Component;

import com.iso.hypo.domain.LocalizedString;
import com.iso.hypo.domain.aggregate.Course;
import com.iso.hypo.domain.aggregate.Gym;
import com.iso.hypo.domain.aggregate.MembershipPlan;
import com.iso.hypo.domain.dto.MembershipPlanDto;
import com.iso.hypo.domain.pricing.Cost;
import com.iso.hypo.domain.pricing.OneTimeFee;

@Component
public class MembershipPlanMapper {

    private final ModelMapper modelMapper;

    public MembershipPlanMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public <D> D map(Object source, Class<D> destinationType) {
        if (source == null) return null;
        return modelMapper.map(source, destinationType);
    }

    public MembershipPlanDto toDto(MembershipPlan entity) {
        return map(entity, MembershipPlanDto.class);
    }

    public MembershipPlan toEntity(MembershipPlanDto dto) {
        return map(dto, MembershipPlan.class);
    }
    
	public ModelMapper initMembershipPlanMappings(ModelMapper mapper) {
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

