package com.iso.hypo.model.dto;

import java.time.Instant;

import com.iso.hypo.common.dto.BaseEntityDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MembershipDto extends BaseEntityDto {

    private String uuid;

    private String brandUuid;

    private MembershipPlanDto membershipPlan;

    private MemberDto member;

    private Integer remainingClasses;

    private boolean autoRenewal = true;

    private boolean isCancelled = false;

    private Instant cancelledOn;

}