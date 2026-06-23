package com.iso.hypo.membership.domain.repository;

import java.util.Optional;

import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;
import com.iso.hypo.membership.domain.model.Member;

public interface MemberRepository {

    Optional<Member> findByBrandUuidAndUuidAndDeletedIsFalse(String brandUuid, String memberUuid);
    
    Optional<Member> findByBrandUuidAndUuid(String brandUuid, String memberUuid);

    PageResult<Member> findAllByBrandUuidAndDeletedIsFalse(String brandUuid, PageRequest pageRequest);

    PageResult<Member> findAllByBrandUuidAndDeletedIsFalseAndActiveIsTrue(String brandUuid, PageRequest pageRequest);

	Optional<Member> findByBrandUuidAndPersonEmailAndDeletedIsFalse(String brandUuid, String email);

	Optional<Member> findByBrandUuidAndUserUuidAndDeletedIsFalse(String brandUuid, String userUuid);
	
	Member save(Member member);

    void delete(Member member);

    void deleteAll();
    
    long deleteAllByBrandUuid(String brandUuid, String deletedBy);
}