package com.isoceles.hypothenus.gym.admin.papi.dto.post;

import java.time.Instant;

import com.isoceles.hypothenus.gym.admin.papi.dto.model.MembershipPlanDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.model.MemberDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostMembershipDto {

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