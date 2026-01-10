package com.iso.hypo.gym.admin.papi.dto.put;

import java.time.Instant;

import com.iso.hypo.gym.admin.papi.dto.model.MembershipPlanDto;
import com.iso.hypo.gym.admin.papi.dto.model.MemberDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PutMembershipDto {

	@NotBlank
    private String id;

    @NotBlank
    private String brandId;

    private MembershipPlanDto membershipPlan;

    private MemberDto member;

    private Integer remainingClasses;

    private boolean autoRenewal = true;

    private boolean isCancelled = false;

    private Instant cancelledOn;
}