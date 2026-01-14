package com.iso.hypo.gym.admin.papi.dto.model;

import java.time.Instant;

import com.iso.hypo.gym.admin.papi.dto.BaseDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MembershipDto extends BaseDto {

    private String uuid;

    private String brandId;

    private MembershipPlanDto membershipPlan;

    private MemberDto member;

    private Integer remainingClasses;

    private boolean autoRenewal = true;

    private boolean isCancelled = false;

    private Instant cancelledOn;
}