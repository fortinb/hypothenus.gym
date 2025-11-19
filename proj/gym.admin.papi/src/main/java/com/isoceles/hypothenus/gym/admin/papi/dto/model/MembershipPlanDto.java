package com.isoceles.hypothenus.gym.admin.papi.dto.model;

import java.util.List;

import com.isoceles.hypothenus.gym.admin.papi.dto.BaseDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.LocalizedStringDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.enumeration.BillingFrequencyEnum;
import com.isoceles.hypothenus.gym.admin.papi.dto.enumeration.MembershipPlanPeriodEnum;
import com.isoceles.hypothenus.gym.admin.papi.dto.pricing.CostDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.pricing.OneTimeFeeDto;

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

	private Integer numberOfClasses;
	
	private MembershipPlanPeriodEnum period;
	
	private BillingFrequencyEnum billingFrequency;
	
	private CostDto cost;
	
	private List<OneTimeFeeDto> oneTimeFees;
	
	private boolean guestPrivilege;
	
	private boolean isGiftCard;
	
	private Integer durationInMonths;
	
	private boolean isPromotional;
	
	private List<CourseDto> courses;
	
	private List<GymDto> includedGyms;
}


