package com.iso.hypo.admin.papi.dto.post;

import java.time.Instant;

import com.iso.hypo.admin.papi.dto.model.MembershipPlanDto;
import com.iso.hypo.admin.papi.dto.model.MemberDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostMembershipDto {

    @NotBlank
    private String brandUuid;

    private MembershipPlanDto membershipPlan;

    private MemberDto member;

    private Integer remainingClasses;

    private boolean autoRenewal = true;

    private boolean isCancelled = false;

    private Instant cancelledOn;
}
