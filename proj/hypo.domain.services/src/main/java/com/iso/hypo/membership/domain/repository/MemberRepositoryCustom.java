package com.iso.hypo.membership.domain.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.iso.hypo.membership.application.dto.search.MemberSearchDto;
import com.iso.hypo.membership.domain.model.Member;

public interface MemberRepositoryCustom {

    Page<MemberSearchDto> searchAutocomplete(String criteria, Pageable pageable, boolean includeInactive);

    Optional<Member> activate(String brandUuid, String memberUuid);

    Optional<Member> deactivate(String brandUuid, String memberUuid);

    void delete(String brandUuid, String memberUuid, String deletedBy);

    long deleteAllByBrandUuid(String brandUuid, String deletedBy);
}