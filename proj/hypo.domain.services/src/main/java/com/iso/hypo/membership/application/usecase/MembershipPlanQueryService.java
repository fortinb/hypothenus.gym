package com.iso.hypo.membership.application.usecase;

import java.util.Date;

import com.iso.hypo.common.application.dto.PageResultDto;
import com.iso.hypo.membership.application.dto.MembershipPlanDto;
import com.iso.hypo.membership.application.exception.MembershipPlanException;

public interface MembershipPlanQueryService {
	
	void assertExists(String brandUuid, String membershipPlanUuid) throws MembershipPlanException;

    MembershipPlanDto find(String brandUuid, String membershipPlanUuid) throws MembershipPlanException;

    PageResultDto<MembershipPlanDto> list(String brandUuid, Date currentDate, int page, int pageSize, boolean includeInactive) throws MembershipPlanException;
}


