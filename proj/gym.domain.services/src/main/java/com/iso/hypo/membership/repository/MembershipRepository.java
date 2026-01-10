package com.iso.hypo.membership.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.iso.hypo.membership.domain.aggregate.Membership;

public interface MembershipRepository extends PagingAndSortingRepository<Membership, String>, CrudRepository<Membership, String>, MembershipRepositoryCustom {
	
	Optional<Membership> findByBrandIdAndIdAndIsDeletedIsFalse(String brandId, String id);
	
	Page<Membership> findAllByBrandIdAndIsDeletedIsFalse(String brandId, Pageable pageable);
	
	Page<Membership> findAllByBrandIdAndIsDeletedIsFalseAndIsActiveIsTrue(String brandId, Pageable pageable);
	
	
}
