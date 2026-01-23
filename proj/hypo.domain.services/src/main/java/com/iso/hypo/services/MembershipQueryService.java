package com.iso.hypo.services;

import org.springframework.data.domain.Page;

import com.iso.hypo.domain.dto.MembershipDto;
import com.iso.hypo.services.exception.MembershipException;

public interface MembershipQueryService {

	void assertExists(String brandUuid, String membershipUuid) throws MembershipException;
	
    MembershipDto find(String brandUuid, String membershipUuid) throws MembershipException;

    Page<MembershipDto> list(String brandUuid, int page, int pageSize, boolean includeInactive) throws MembershipException;
}


