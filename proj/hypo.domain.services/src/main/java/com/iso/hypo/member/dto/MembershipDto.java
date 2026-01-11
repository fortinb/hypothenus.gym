package com.iso.hypo.member.dto;

import java.time.Instant;

import com.iso.hypo.brand.dto.MembershipPlanDto;
import com.iso.hypo.common.dto.BaseEntityDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MembershipDto extends BaseEntityDto {

    private String id;

    private String brandId;

    private MembershipPlanDto membershipPlan;

    private MemberDto member;

    private Integer remainingClasses;

    private boolean autoRenewal = true;

    private boolean isCancelled = false;

    private Instant cancelledOn;

}