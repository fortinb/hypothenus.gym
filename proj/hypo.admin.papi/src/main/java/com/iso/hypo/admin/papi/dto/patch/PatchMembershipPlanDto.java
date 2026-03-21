package com.iso.hypo.admin.papi.dto.patch;

import java.util.Date;
import java.util.List;

import com.iso.hypo.admin.papi.dto.LocalizedStringDto;
import com.iso.hypo.admin.papi.dto.enumeration.BillingFrequencyEnum;
import com.iso.hypo.admin.papi.dto.enumeration.MembershipPlanPeriodEnum;
import com.iso.hypo.admin.papi.dto.model.CourseDto;
import com.iso.hypo.admin.papi.dto.model.GymDto;
import com.iso.hypo.admin.papi.dto.pricing.CostDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatchMembershipPlanDto {
	
	
	@NotBlank
	private String brandUuid;
	
	@NotBlank
	private String uuid;

	private List<LocalizedStringDto> name;

	private List<LocalizedStringDto> title;

	private List<LocalizedStringDto> description;

	private int numberOfClasses;
	
	private MembershipPlanPeriodEnum period;
	
	private BillingFrequencyEnum billingFrequency;
	
	private CostDto cost;
	
	private boolean guestPrivilege;
	
	private boolean isGiftCard;
	
	private int durationInMonths;
	
	private boolean isPromotional;
	
	private Date startDate;
	
	private Date endDate;
	
	private List<CourseDto> includedCourses;
	
	private List<GymDto> includedGyms;
}
