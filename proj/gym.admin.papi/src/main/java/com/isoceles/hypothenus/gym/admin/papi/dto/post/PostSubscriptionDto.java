package com.isoceles.hypothenus.gym.admin.papi.dto.post;

import java.util.List;

import com.isoceles.hypothenus.gym.admin.papi.dto.CourseDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.LocalizedStringDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.SubscriptionPaymentOptionEnum;
import com.isoceles.hypothenus.gym.admin.papi.dto.SubscriptionPeriodEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostSubscriptionDto {
	
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