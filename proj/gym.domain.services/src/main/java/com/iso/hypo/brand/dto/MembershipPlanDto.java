package com.iso.hypo.brand.dto;

import java.util.List;

import com.iso.hypo.common.domain.LocalizedString;
import com.iso.hypo.common.domain.pricing.Cost;
import com.iso.hypo.common.domain.pricing.OneTimeFee;
import com.iso.hypo.brand.domain.enumeration.BillingFrequencyEnum;
import com.iso.hypo.brand.domain.enumeration.MembershipPlanPeriodEnum;
import com.iso.hypo.gym.dto.CourseDto;
import com.iso.hypo.gym.dto.GymDto;
import com.iso.hypo.common.dto.BaseEntityDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MembershipPlanDto extends BaseEntityDto {

    private String id;

    private String brandId;

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