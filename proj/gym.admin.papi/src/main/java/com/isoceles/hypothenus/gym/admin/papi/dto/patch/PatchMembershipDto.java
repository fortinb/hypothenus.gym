package com.isoceles.hypothenus.gym.admin.papi.dto.patch;

import java.time.Instant;

import com.isoceles.hypothenus.gym.admin.papi.dto.model.MembershipPlanDto;

import jakarta.validation.constraints.NotBlank;

import com.isoceles.hypothenus.gym.admin.papi.dto.model.MemberDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatchMembershipDto {

	@NotBlank
    private String id;

	@NotBlank
    private String brandId;

    private MembershipPlanDto membershipPlan;

    private MemberDto member;

    private Integer remainingClasses;

    private Boolean autoRenewal;

    private Boolean isCancelled;

    private Instant cancelledOn;
}