package com.iso.hypo.membership.application.mapper;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Component;

import com.iso.hypo.brand.domain.model.Course;
import com.iso.hypo.brand.domain.model.Gym;
import com.iso.hypo.common.domain.model.LocalizedString;
import com.iso.hypo.common.domain.model.finance.Cost;
import com.iso.hypo.membership.application.dto.MembershipPlanDto;
import com.iso.hypo.membership.domain.model.MembershipPlan;

@Component
public class MembershipPlanDtoMapper {

    private final ModelMapper modelMapper;

    public MembershipPlanDtoMapper(ModelMapper modelMapper) {
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
		
	    PropertyMap<Gym, Gym> gymsPropertyMap = new PropertyMap<Gym, Gym>() {
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
		mapper.addMappings(gymsPropertyMap);
		mapper.addMappings(costPropertyMap);
		
		return mapper;
	}
}

