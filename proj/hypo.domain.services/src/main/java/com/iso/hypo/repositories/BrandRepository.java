package com.iso.hypo.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.iso.hypo.domain.aggregate.Brand;

public interface BrandRepository extends PagingAndSortingRepository<Brand, String>, CrudRepository<Brand, String>, BrandRepositoryCustom {
	
	Optional<Brand> findByUuidAndIsDeletedIsFalse(String brandUuid);
	
	Optional<Brand> findByCode(String code);
	
	Page<Brand> findAllByIsDeletedIsFalse(Pageable pageable);
	
	Page<Brand> findAllByIsDeletedIsFalseAndIsActiveIsTrue(Pageable pageable);
}

