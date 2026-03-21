package com.iso.hypo.admin.papi.dto.model;

import java.util.Date;
import java.util.List;

import com.iso.hypo.admin.papi.dto.BaseDto;
import com.iso.hypo.admin.papi.dto.LocalizedStringDto;
import com.iso.hypo.admin.papi.dto.enumeration.BillingFrequencyEnum;
import com.iso.hypo.admin.papi.dto.enumeration.MembershipPlanPeriodEnum;
import com.iso.hypo.admin.papi.dto.pricing.CostDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MembershipPlanDto extends BaseDto {
	
	private String brandUuid;
	
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


