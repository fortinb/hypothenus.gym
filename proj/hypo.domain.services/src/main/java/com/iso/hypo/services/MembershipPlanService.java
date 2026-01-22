package com.iso.hypo.services;

import org.springframework.data.domain.Page;

import com.iso.hypo.domain.dto.MembershipPlanDto;
import com.iso.hypo.services.exception.MembershipPlanException;

public interface MembershipPlanService {

    MembershipPlanDto create(String brandUuid, MembershipPlanDto membershipPlanDto) throws MembershipPlanException;

    MembershipPlanDto update(String brandUuid, MembershipPlanDto membershipPlanDto) throws MembershipPlanException;

    MembershipPlanDto patch(String brandUuid, MembershipPlanDto membershipPlanDto) throws MembershipPlanException;

    void delete(String brandUuid, String membershipPlanUuid) throws MembershipPlanException;

    MembershipPlanDto findByMembershipPlanUuid(String brandUuid, String membershipPlanUuid) throws MembershipPlanException;

    Page<MembershipPlanDto> list(String brandUuid, int page, int pageSize, boolean includeInactive) throws MembershipPlanException;

    MembershipPlanDto activate(String brandUuid, String membershipPlanUuid) throws MembershipPlanException;

    MembershipPlanDto deactivate(String brandUuid, String membershipPlanUuid) throws MembershipPlanException;
}


