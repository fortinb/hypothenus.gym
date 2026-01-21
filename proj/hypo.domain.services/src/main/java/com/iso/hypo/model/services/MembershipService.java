package com.iso.hypo.model.services;

import org.springframework.data.domain.Page;

import com.iso.hypo.model.dto.MembershipDto;
import com.iso.hypo.model.exception.MemberException;

public interface MembershipService {

    MembershipDto create(String brandUuid, MembershipDto membershipDto) throws MemberException;

    MembershipDto update(String brandUuid, MembershipDto membershipDto) throws MemberException;

    MembershipDto patch(String brandUuid, MembershipDto membershipDto) throws MemberException;

    void delete(String brandUuid, String membershipUuid) throws MemberException;

    MembershipDto findByMembershipUuid(String brandUuid, String membershipUuid) throws MemberException;

    Page<MembershipDto> list(String brandUuid, int page, int pageSize, boolean includeInactive) throws MemberException;

    MembershipDto activate(String brandUuid, String membershipUuid) throws MemberException;

    MembershipDto deactivate(String brandUuid, String membershipUuid) throws MemberException;
}