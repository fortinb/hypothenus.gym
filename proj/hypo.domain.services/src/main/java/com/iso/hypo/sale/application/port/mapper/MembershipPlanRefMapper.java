package com.iso.hypo.sale.application.port.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.iso.hypo.sale.application.port.dto.MembershipPlanRef;
import com.iso.hypo.sale.domain.model.MembershipPlan;

@Component
public class MembershipPlanRefMapper {

	private final ModelMapper modelMapper;

	public MembershipPlanRefMapper(ModelMapper modelMapper) {
		this.modelMapper = modelMapper;
	}

	public <D> D map(Object source, Class<D> destinationType) {
		if (source == null) return null;
		return modelMapper.map(source, destinationType);
	}
	
	public MembershipPlanRef toRef(MembershipPlan entity) {
		return map(entity, MembershipPlanRef.class);
	}
	
	public MembershipPlan toEntity(MembershipPlanRef ref) {
		return map(ref, MembershipPlan.class);
	}
}
