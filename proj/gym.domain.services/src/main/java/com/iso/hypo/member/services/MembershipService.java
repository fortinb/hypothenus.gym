package com.iso.hypo.member.services;

import org.springframework.data.domain.Page;

import com.iso.hypo.member.dto.MembershipDto;
import com.iso.hypo.member.exception.MemberException;

public interface MembershipService {

    MembershipDto create(String brandId, MembershipDto membershipDto) throws MemberException;

    MembershipDto update(String brandId, MembershipDto membershipDto) throws MemberException;

    MembershipDto patch(String brandId, MembershipDto membershipDto) throws MemberException;

    void delete(String brandId, String membershipId) throws MemberException;

    MembershipDto findByMembershipId(String brandId, String id) throws MemberException;

    Page<MembershipDto> list(String brandId, int page, int pageSize, boolean includeInactive) throws MemberException;

    MembershipDto activate(String brandId, String id) throws MemberException;

    MembershipDto deactivate(String brandId, String id) throws MemberException;
}