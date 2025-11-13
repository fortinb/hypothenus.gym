package com.isoceles.hypothenus.gym.admin.papi.dto.post;

import java.util.List;

import com.isoceles.hypothenus.gym.admin.papi.dto.CourseDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.LocalizedStringDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.MembershipPlanPaymentOptionEnum;
import com.isoceles.hypothenus.gym.admin.papi.dto.MembershipPlanPeriodEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostMembershipPlanDto {
	
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