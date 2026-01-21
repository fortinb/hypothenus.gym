package com.iso.hypo.admin.papi.dto.post;

import java.util.List;

import com.iso.hypo.admin.papi.dto.LocalizedStringDto;
import com.iso.hypo.admin.papi.dto.enumeration.BillingFrequencyEnum;
import com.iso.hypo.admin.papi.dto.enumeration.MembershipPlanPeriodEnum;
import com.iso.hypo.admin.papi.dto.model.CourseDto;
import com.iso.hypo.admin.papi.dto.model.GymDto;
import com.iso.hypo.admin.papi.dto.pricing.CostDto;
import com.iso.hypo.admin.papi.dto.pricing.OneTimeFeeDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostMembershipPlanDto {
	
	@NotBlank
	private String brandUuid;

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
