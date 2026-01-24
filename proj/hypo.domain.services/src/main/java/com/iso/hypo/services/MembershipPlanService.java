package com.iso.hypo.services;

import com.iso.hypo.domain.dto.MembershipPlanDto;
import com.iso.hypo.services.exception.MembershipPlanException;

public interface MembershipPlanService {

    MembershipPlanDto create(MembershipPlanDto membershipPlanDto) throws MembershipPlanException;

    MembershipPlanDto update(MembershipPlanDto membershipPlanDto) throws MembershipPlanException;

    MembershipPlanDto patch(MembershipPlanDto membershipPlanDto) throws MembershipPlanException;

    void delete(String brandUuid, String membershipPlanUuid) throws MembershipPlanException;

    MembershipPlanDto activate(String brandUuid, String membershipPlanUuid) throws MembershipPlanException;

    MembershipPlanDto deactivate(String brandUuid, String membershipPlanUuid) throws MembershipPlanException;
}