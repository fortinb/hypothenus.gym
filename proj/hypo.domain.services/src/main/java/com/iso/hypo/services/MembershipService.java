package com.iso.hypo.services;

import com.iso.hypo.domain.dto.MembershipDto;
import com.iso.hypo.services.exception.MembershipException;

public interface MembershipService {

    MembershipDto create(String brandUuid, MembershipDto membershipDto) throws MembershipException;

    MembershipDto update(String brandUuid, MembershipDto membershipDto) throws MembershipException;

    MembershipDto patch(String brandUuid, MembershipDto membershipDto) throws MembershipException;

    void delete(String brandUuid, String membershipUuid) throws MembershipException;

    MembershipDto activate(String brandUuid, String membershipUuid) throws MembershipException;

    MembershipDto deactivate(String brandUuid, String membershipUuid) throws MembershipException;
}