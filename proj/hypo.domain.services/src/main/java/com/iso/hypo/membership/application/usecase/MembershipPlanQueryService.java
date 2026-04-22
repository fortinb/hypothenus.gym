package com.iso.hypo.membership.application.usecase;

import java.util.Date;

import org.springframework.data.domain.Page;

import com.iso.hypo.membership.application.dto.MembershipPlanDto;
import com.iso.hypo.membership.domain.exception.MembershipPlanException;

public interface MembershipPlanQueryService {
	
	void assertExists(String brandUuid, String membershipPlanUuid) throws MembershipPlanException;

    MembershipPlanDto find(String brandUuid, String membershipPlanUuid) throws MembershipPlanException;

    Page<MembershipPlanDto> list(String brandUuid, Date currentDate, int page, int pageSize, boolean includeInactive) throws MembershipPlanException;
}


