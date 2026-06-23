package com.iso.hypo.admin.papi.dto.model;

import java.util.Date;
import java.util.List;

import com.iso.hypo.admin.papi.dto.BaseDto;
import com.iso.hypo.admin.papi.dto.LocalizedStringDto;
import com.iso.hypo.admin.papi.dto.finance.CostDto;
import com.iso.hypo.common.application.dto.enumeration.BillingFrequencyEnumDto;
import com.iso.hypo.common.application.dto.enumeration.MembershipPlanPeriodEnumDto;

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

	private List<LocalizedStringDto> termsOfUse;

	private int numberOfClasses;
	
	private MembershipPlanPeriodEnumDto period;
	
	private BillingFrequencyEnumDto billingFrequency;
	
	private CostDto price;
	
	private boolean guestPrivilege;
	
	private boolean giftCard;
	
	private int durationInMonths;
	
	private boolean promotional;
	
	private Date startDate;
	
	private Date endDate;
	
	private List<String> includedCourseUuids;
	
	private List<String> includedGymUuids;
}