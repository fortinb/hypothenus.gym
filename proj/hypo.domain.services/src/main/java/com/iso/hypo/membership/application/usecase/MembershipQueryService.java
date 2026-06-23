package com.iso.hypo.membership.application.usecase;

import java.util.Optional;

import com.iso.hypo.common.application.dto.PageResultDto;
import com.iso.hypo.membership.application.dto.MembershipDto;
import com.iso.hypo.membership.application.exception.MembershipException;

public interface MembershipQueryService {

	void assertExists(String brandUuid, String membershipUuid) throws MembershipException;
	
	boolean isNewMember(String brandUuid, String memberUuid) throws MembershipException;
	
    MembershipDto find(String brandUuid, String membershipUuid) throws MembershipException;
    
    Optional<MembershipDto> findByMembershipPlanUuid(String brandUuid, String memberUuid, String membershipPlanUuid) throws MembershipException;

    PageResultDto<MembershipDto> list(String brandUuid, int page, int pageSize, boolean includeInactive) throws MembershipException;
}


