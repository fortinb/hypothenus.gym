package com.iso.hypo.gym.domain.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.iso.hypo.gym.domain.model.aggregate.Brand;

public interface BrandRepository extends PagingAndSortingRepository<Brand, String>, CrudRepository<Brand, String>, BrandRepositoryCustom {
	
	Optional<Brand> findByBrandIdAndIsDeletedIsFalse(String brandId);
	
	Optional<Brand> findByBrandId(String brandId);
	
	Page<Brand> findAllByIsDeletedIsFalse(Pageable pageable);
	
	Page<Brand> findAllByIsDeletedIsFalseAndIsActiveIsTrue(Pageable pageable);
}
