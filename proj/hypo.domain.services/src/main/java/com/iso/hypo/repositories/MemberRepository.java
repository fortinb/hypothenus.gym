package com.iso.hypo.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.iso.hypo.domain.aggregate.Member;

public interface MemberRepository extends PagingAndSortingRepository<Member, String>, CrudRepository<Member, String>, MemberRepositoryCustom {

    Optional<Member> findByBrandUuidAndUuidAndDeletedIsFalse(String brandUuid, String memberUuid);

    Page<Member> findAllByBrandUuidAndDeletedIsFalse(String brandUuid, Pageable pageable);

    Page<Member> findAllByBrandUuidAndDeletedIsFalseAndActiveIsTrue(String brandUuid, Pageable pageable);

	Optional<Member> findByBrandUuidAndPersonEmailAndDeletedIsFalse(String brandUuid, String email);
}