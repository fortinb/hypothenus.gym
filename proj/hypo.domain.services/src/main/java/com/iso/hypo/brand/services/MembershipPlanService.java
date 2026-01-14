package com.iso.hypo.brand.services;

import org.springframework.data.domain.Page;

import com.iso.hypo.brand.dto.MembershipPlanDto;
import com.iso.hypo.brand.exception.BrandException;

public interface MembershipPlanService {

    MembershipPlanDto create(String brandId, MembershipPlanDto membershipPlanDto) throws BrandException;

    MembershipPlanDto update(String brandId, MembershipPlanDto membershipPlanDto) throws BrandException;

    MembershipPlanDto patch(String brandId, MembershipPlanDto membershipPlanDto) throws BrandException;

    void delete(String brandId, String membershipPlanUuid) throws BrandException;

    MembershipPlanDto findByMembershipPlanUuid(String brandId, String membershipPlanUuid) throws BrandException;

    Page<MembershipPlanDto> list(String brandId, int page, int pageSize, boolean includeInactive) throws BrandException;

    MembershipPlanDto activate(String brandId, String membershipPlanUuid) throws BrandException;

    MembershipPlanDto deactivate(String brandId, String membershipPlanUuid) throws BrandException;
}