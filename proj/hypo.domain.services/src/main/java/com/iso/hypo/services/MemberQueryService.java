package com.iso.hypo.services;

import org.springframework.data.domain.Page;

import com.iso.hypo.domain.dto.MemberDto;
import com.iso.hypo.domain.dto.search.MemberSearchDto;
import com.iso.hypo.services.exception.MemberException;

public interface MemberQueryService {

    void assertExists(String brandUuid, String memberUuid) throws MemberException;

    MemberDto find(String brandUuid, String memberUuid) throws MemberException;

    Page<MemberSearchDto> search(int page, int pageSize, String criteria, boolean includeInactive) throws MemberException;

    Page<MemberDto> list(String brandUuid, int page, int pageSize, boolean includeInactive) throws MemberException;
}