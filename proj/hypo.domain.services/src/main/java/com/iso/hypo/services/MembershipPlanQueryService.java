package com.iso.hypo.services;

import java.util.Date;

import org.springframework.data.domain.Page;

import com.iso.hypo.domain.dto.MembershipPlanDto;
import com.iso.hypo.services.exception.MembershipPlanException;

public interface MembershipPlanQueryService {
	
	void assertExists(String brandUuid, String membershipPlanUuid) throws MembershipPlanException;

    MembershipPlanDto find(String brandUuid, String membershipPlanUuid) throws MembershipPlanException;

    Page<MembershipPlanDto> list(String brandUuid, Date currentDate, int page, int pageSize, boolean includeInactive) throws MembershipPlanException;
}


