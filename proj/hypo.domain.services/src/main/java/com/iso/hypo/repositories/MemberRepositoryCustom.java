package com.iso.hypo.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.iso.hypo.domain.aggregate.Member;
import com.iso.hypo.domain.dto.search.MemberSearchDto;

public interface MemberRepositoryCustom {

    Page<MemberSearchDto> searchAutocomplete(String criteria, Pageable pageable, boolean includeInactive);

    Optional<Member> activate(String brandUuid, String memberUuid);

    Optional<Member> deactivate(String brandUuid, String memberUuid);

    void delete(String brandUuid, String memberUuid, String deletedBy);

    long deleteAllByBrandUuid(String brandUuid, String deletedBy);
}