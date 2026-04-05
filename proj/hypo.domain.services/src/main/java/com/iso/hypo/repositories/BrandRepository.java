package com.iso.hypo.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.iso.hypo.domain.aggregate.Brand;

public interface BrandRepository extends PagingAndSortingRepository<Brand, String>, CrudRepository<Brand, String>, BrandRepositoryCustom {
	
	Optional<Brand> findByUuidAndDeletedIsFalse(String brandUuid);
	
	Optional<Brand> findByCode(String code);
	
	Optional<Brand> findByCodeAndDeletedIsFalse(String code);
	
	Page<Brand> findAllByDeletedIsFalse(Pageable pageable);
	
	Page<Brand> findAllByDeletedIsFalseAndActiveIsTrue(Pageable pageable);
}

