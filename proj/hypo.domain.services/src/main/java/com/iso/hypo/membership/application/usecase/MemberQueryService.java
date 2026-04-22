package com.iso.hypo.membership.application.usecase;

import org.springframework.data.domain.Page;

import com.iso.hypo.membership.application.dto.MemberDto;
import com.iso.hypo.membership.application.dto.search.MemberSearchDto;
import com.iso.hypo.membership.domain.exception.MemberException;

public interface MemberQueryService {

    void assertExists(String brandUuid, String memberUuid) throws MemberException;

    MemberDto find(String brandUuid, String memberUuid) throws MemberException;

    Page<MemberSearchDto> search(int page, int pageSize, String criteria, boolean includeInactive) throws MemberException;

    Page<MemberDto> list(String brandUuid, int page, int pageSize, boolean includeInactive) throws MemberException;

    MemberDto findByUserIdpId(String brandUuid, String idpId) throws MemberException;
}