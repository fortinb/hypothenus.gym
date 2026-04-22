package com.iso.hypo.membership.application.dto;

import java.util.Date;
import java.util.List;

import com.iso.hypo.brand.application.dto.CourseDto;
import com.iso.hypo.brand.application.dto.GymDto;
import com.iso.hypo.common.application.dto.BaseEntityDto;
import com.iso.hypo.common.domain.model.LocalizedString;
import com.iso.hypo.common.domain.model.finance.Cost;
import com.iso.hypo.membership.domain.model.enumeration.BillingFrequencyEnum;
import com.iso.hypo.membership.domain.model.enumeration.MembershipPlanPeriodEnum;

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

    private List<LocalizedString> termsOfUse;

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
