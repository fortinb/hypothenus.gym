package com.iso.hypo.membership.application.usecase;

import org.springframework.data.domain.Page;

import com.iso.hypo.membership.application.dto.MembershipDto;
import com.iso.hypo.membership.domain.exception.MembershipException;

public interface MembershipQueryService {

	void assertExists(String brandUuid, String membershipUuid) throws MembershipException;
	
    MembershipDto find(String brandUuid, String membershipUuid) throws MembershipException;

    Page<MembershipDto> list(String brandUuid, int page, int pageSize, boolean includeInactive) throws MembershipException;
}


