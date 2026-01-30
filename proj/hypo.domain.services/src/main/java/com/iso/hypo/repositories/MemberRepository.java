package com.iso.hypo.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.iso.hypo.domain.aggregate.Member;

public interface MemberRepository extends PagingAndSortingRepository<Member, String>, CrudRepository<Member, String>, MemberRepositoryCustom {

    Optional<Member> findByBrandUuidAndUuidAndIsDeletedIsFalse(String brandUuid, String memberUuid);

    Page<Member> findAllByBrandUuidAndIsDeletedIsFalse(String brandUuid, Pageable pageable);

    Page<Member> findAllByBrandUuidAndIsDeletedIsFalseAndIsActiveIsTrue(String brandUuid, Pageable pageable);

}