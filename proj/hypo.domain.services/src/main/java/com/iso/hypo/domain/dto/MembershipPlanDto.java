package com.iso.hypo.domain.dto;

import java.util.List;

import com.iso.hypo.domain.LocalizedString;
import com.iso.hypo.domain.enumeration.BillingFrequencyEnum;
import com.iso.hypo.domain.enumeration.MembershipPlanPeriodEnum;
import com.iso.hypo.domain.pricing.Cost;
import com.iso.hypo.domain.pricing.OneTimeFee;
import com.iso.hypo.common.dto.BaseEntityDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MembershipPlanDto extends BaseEntityDto {

    private String uuid;

    private String brandUuid;

    private String code;

    private List<LocalizedString> name;

    private List<LocalizedString> description;

    private Integer numberOfClasses;

    private MembershipPlanPeriodEnum period;

    private BillingFrequencyEnum billingFrequency;

    private Cost cost;

    private List<OneTimeFee> oneTimeFees;

    private Integer durationInMonths;

    private boolean guestPrivilege;

    private boolean isPromotional;

    private boolean isGiftCard;

    private List<CourseDto> courses;

    private List<GymDto> includedGyms;

}
