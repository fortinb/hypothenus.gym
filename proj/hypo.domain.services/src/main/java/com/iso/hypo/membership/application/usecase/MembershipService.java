package com.iso.hypo.membership.application.usecase;

import java.util.List;

import com.iso.hypo.membership.application.dto.MembershipDto;
import com.iso.hypo.membership.application.exception.MembershipException;

public interface MembershipService {

	List<MembershipDto> create(List<MembershipDto> membershipsDto) throws MembershipException;

    MembershipDto update(MembershipDto membershipDto) throws MembershipException;

    MembershipDto patch(MembershipDto membershipDto) throws MembershipException;

    void delete(String brandUuid, String membershipUuid) throws MembershipException;

    MembershipDto activate(String brandUuid, String membershipUuid) throws MembershipException;

    MembershipDto deactivate(String brandUuid, String membershipUuid) throws MembershipException;
    
    void deleteAllByBrandUuid(String brandUuid) throws MembershipException;
}