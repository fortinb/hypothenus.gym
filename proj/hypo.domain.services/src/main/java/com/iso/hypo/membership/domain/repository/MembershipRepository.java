package com.iso.hypo.membership.domain.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.iso.hypo.membership.domain.model.Membership;

public interface MembershipRepository extends PagingAndSortingRepository<Membership, String>, CrudRepository<Membership, String>, MembershipRepositoryCustom {
	
	Optional<Membership> findByBrandUuidAndUuidAndDeletedIsFalse(String brandUuid, String membershipUuid);
	
	Page<Membership> findAllByBrandUuidAndDeletedIsFalse(String brandUuid, Pageable pageable);
	
	Page<Membership> findAllByBrandUuidAndDeletedIsFalseAndActiveIsTrue(String brandUuid, Pageable pageable);
	
	
}

