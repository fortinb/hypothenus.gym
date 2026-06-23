package com.iso.hypo.admin.papi.dto.model;

import java.time.Instant;

import com.iso.hypo.admin.papi.dto.BaseDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MembershipDto extends BaseDto {

    private String brandUuid;
    
    private String uuid;
    
	private String orderUuid;

    private MembershipPlanDto membershipPlan;

    private String memberUuid;

    private int remainingClasses;

    private boolean autoRenewal = true;

    private boolean cancelled = false;

    private Instant cancelledOn;
}
