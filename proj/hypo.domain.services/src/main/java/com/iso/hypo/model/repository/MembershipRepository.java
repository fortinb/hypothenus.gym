package com.iso.hypo.model.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.iso.hypo.model.aggregate.Membership;

public interface MembershipRepository extends PagingAndSortingRepository<Membership, String>, CrudRepository<Membership, String>, MembershipRepositoryCustom {
	
	Optional<Membership> findByBrandUuidAndUuidAndIsDeletedIsFalse(String brandUuid, String membershipUuid);
	
	Page<Membership> findAllByBrandUuidAndIsDeletedIsFalse(String brandUuid, Pageable pageable);
	
	Page<Membership> findAllByBrandUuidAndIsDeletedIsFalseAndIsActiveIsTrue(String brandUuid, Pageable pageable);
	
	
}
