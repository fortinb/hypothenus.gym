package com.iso.hypo.membership.application.dto;

import java.time.Instant;

import com.iso.hypo.common.application.dto.BaseEntityDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MembershipDto extends BaseEntityDto {

    private String uuid;

    private String brandUuid;
    
	private String orderUuid;

    private MembershipPlanDto membershipPlan;

    private String memberUuid;

    private int remainingClasses;

    private boolean autoRenewal = true;

    private boolean cancelled = false;

    private Instant cancelledOn;

}
