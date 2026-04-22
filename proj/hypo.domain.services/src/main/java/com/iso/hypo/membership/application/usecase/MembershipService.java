package com.iso.hypo.membership.application.usecase;

import com.iso.hypo.membership.application.dto.MembershipDto;
import com.iso.hypo.membership.domain.exception.MembershipException;

public interface MembershipService {

    MembershipDto create(MembershipDto membershipDto) throws MembershipException;

    MembershipDto update(MembershipDto membershipDto) throws MembershipException;

    MembershipDto patch(MembershipDto membershipDto) throws MembershipException;

    void delete(String brandUuid, String membershipUuid) throws MembershipException;

    MembershipDto activate(String brandUuid, String membershipUuid) throws MembershipException;

    MembershipDto deactivate(String brandUuid, String membershipUuid) throws MembershipException;
    
    void deleteAllByBrandUuid(String brandUuid) throws MembershipException;
}