package com.iso.hypo.membership.application.dto;

import java.util.Date;
import java.util.List;

import com.iso.hypo.common.application.dto.BaseEntityDto;
import com.iso.hypo.common.application.dto.LocalizedStringDto;
import com.iso.hypo.common.application.dto.finance.CostDto;
import com.iso.hypo.membership.application.dto.enumeration.BillingFrequencyEnumDto;
import com.iso.hypo.membership.application.dto.enumeration.MembershipPlanPeriodEnumDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MembershipPlanDto extends BaseEntityDto {

    private String uuid;

    private String brandUuid;

    private List<LocalizedStringDto> name;

    private List<LocalizedStringDto> title;

    private List<LocalizedStringDto> description;

    private List<LocalizedStringDto> termsOfUse;

    private int numberOfClasses;

    private MembershipPlanPeriodEnumDto period;

    private BillingFrequencyEnumDto billingFrequency;

    private CostDto cost;

    private int durationInMonths;

    private boolean guestPrivilege;

    private boolean promotional;

    private boolean giftCard;
    
    private Date startDate;

    private Date endDate;

    private List<String> includedCourseUuids;
	
	private List<String> includedGymUuids;

}
