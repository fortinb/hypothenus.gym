package com.iso.hypo.model.services;

import org.springframework.data.domain.Page;

import com.iso.hypo.model.dto.MembershipPlanDto;
import com.iso.hypo.model.exception.BrandException;

public interface MembershipPlanService {

    MembershipPlanDto create(String brandUuid, MembershipPlanDto membershipPlanDto) throws BrandException;

    MembershipPlanDto update(String brandUuid, MembershipPlanDto membershipPlanDto) throws BrandException;

    MembershipPlanDto patch(String brandUuid, MembershipPlanDto membershipPlanDto) throws BrandException;

    void delete(String brandUuid, String membershipPlanUuid) throws BrandException;

    MembershipPlanDto findByMembershipPlanUuid(String brandUuid, String membershipPlanUuid) throws BrandException;

    Page<MembershipPlanDto> list(String brandUuid, int page, int pageSize, boolean includeInactive) throws BrandException;

    MembershipPlanDto activate(String brandUuid, String membershipPlanUuid) throws BrandException;

    MembershipPlanDto deactivate(String brandUuid, String membershipPlanUuid) throws BrandException;
}