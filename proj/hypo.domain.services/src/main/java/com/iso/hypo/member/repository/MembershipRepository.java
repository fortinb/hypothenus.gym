package com.iso.hypo.member.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.iso.hypo.member.domain.aggregate.Membership;

public interface MembershipRepository extends PagingAndSortingRepository<Membership, String>, CrudRepository<Membership, String>, MembershipRepositoryCustom {
	
	Optional<Membership> findByBrandIdAndUuidAndIsDeletedIsFalse(String brandId, String membershipUuid);
	
	Page<Membership> findAllByBrandIdAndIsDeletedIsFalse(String brandId, Pageable pageable);
	
	Page<Membership> findAllByBrandIdAndIsDeletedIsFalseAndIsActiveIsTrue(String brandId, Pageable pageable);
	
	
}
