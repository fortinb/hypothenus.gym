package com.iso.hypo.model.dto;

import java.util.List;

import com.iso.hypo.model.domain.LocalizedString;
import com.iso.hypo.model.domain.pricing.Cost;
import com.iso.hypo.model.domain.pricing.OneTimeFee;
import com.iso.hypo.model.enumeration.BillingFrequencyEnum;
import com.iso.hypo.model.enumeration.MembershipPlanPeriodEnum;
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