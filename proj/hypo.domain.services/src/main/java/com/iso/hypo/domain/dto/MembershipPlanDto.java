package com.iso.hypo.domain.dto;

import java.util.Date;
import java.util.List;

import com.iso.hypo.common.dto.BaseEntityDto;
import com.iso.hypo.domain.LocalizedString;
import com.iso.hypo.domain.enumeration.BillingFrequencyEnum;
import com.iso.hypo.domain.enumeration.MembershipPlanPeriodEnum;
import com.iso.hypo.domain.pricing.Cost;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MembershipPlanDto extends BaseEntityDto {

    private String uuid;

    private String brandUuid;

    private List<LocalizedString> name;

    private List<LocalizedString> title;

    private List<LocalizedString> description;

    private List<LocalizedString> detail;

    private int numberOfClasses;

    private MembershipPlanPeriodEnum period;

    private BillingFrequencyEnum billingFrequency;

    private Cost cost;

    private int durationInMonths;

    private boolean guestPrivilege;

    private boolean promotional;

    private boolean giftCard;
    
    private Date startDate;

    private Date endDate;

    private List<CourseDto> includedCourses;

    private List<GymDto> includedGyms;

}
