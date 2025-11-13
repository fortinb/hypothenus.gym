package com.isoceles.hypothenus.gym.admin.papi.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MembershipPlanDto extends BaseDto {
	
	private String id;

	private String brandId;

	private String code;

	private List<LocalizedStringDto> name;

	private List<LocalizedStringDto> description;

	private Integer maxNumberOfClassesPerPeriod;
	
	private MembershipPlanPeriodEnum period;
	
	private MembershipPlanPaymentOptionEnum paymentOption;
	
	private Integer price;
	
	private Integer durationInMonths;
	
	private List<CourseDto> courses;
}