package com.iso.hypo.membership.application.usecase;

import com.iso.hypo.common.application.dto.PageResultDto;
import com.iso.hypo.membership.application.dto.MemberDto;
import com.iso.hypo.membership.application.dto.search.MemberSearchDto;
import com.iso.hypo.membership.application.exception.MemberException;

public interface MemberQueryService {

    void assertExists(String brandUuid, String memberUuid) throws MemberException;

    MemberDto find(String brandUuid, String memberUuid) throws MemberException;

    PageResultDto<MemberSearchDto> search(int page, int pageSize, String criteria, boolean includeInactive) throws MemberException;

    PageResultDto<MemberDto> list(String brandUuid, int page, int pageSize, boolean includeInactive) throws MemberException;

    MemberDto findByUserIdpId(String brandUuid, String idpId) throws MemberException;
}