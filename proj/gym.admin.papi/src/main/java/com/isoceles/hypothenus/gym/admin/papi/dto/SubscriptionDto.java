package com.isoceles.hypothenus.gym.admin.papi.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionDto extends BaseDto {
	
	private String id;

	private String gymId;

	private String code;

	private List<LocalizedStringDto> name;

	private List<LocalizedStringDto> description;

	private Integer maxNumberOfClassesPerPeriod;
	
	private SubscriptionPeriodEnum period;
	
	private SubscriptionPaymentOptionEnum paymentOption;
	
	private Integer price;
	
	private Integer durationInMonths;
	
	private List<CourseDto> courses;
}