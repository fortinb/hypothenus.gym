package com.isoceles.hypothenus.gym.admin.papi.dto.patch;

import java.util.List;

import com.isoceles.hypothenus.gym.admin.papi.dto.LocalizedStringDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.enumeration.BillingFrequencyEnum;
import com.isoceles.hypothenus.gym.admin.papi.dto.enumeration.MembershipPlanPeriodEnum;
import com.isoceles.hypothenus.gym.admin.papi.dto.model.CourseDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.model.GymDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.pricing.CostDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.pricing.OneTimeFeeDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatchMembershipPlanDto {
	
	@NotBlank
	private String id;
	
	@NotBlank
	private String brandId;

	private String code;

	private List<LocalizedStringDto> name;

	private List<LocalizedStringDto> description;

	private Integer numberOfClasses;
	
	private MembershipPlanPeriodEnum period;
	
	private BillingFrequencyEnum billingFrequency;
	
	private CostDto cost;
	
	private List<OneTimeFeeDto> oneTimeFees;
	
	private Boolean guestPrivilege;
	
	private Integer durationInMonths;
	
	private Boolean isPromotional;
	
	private List<CourseDto> courses;
	
	private List<GymDto> includedGyms;
}