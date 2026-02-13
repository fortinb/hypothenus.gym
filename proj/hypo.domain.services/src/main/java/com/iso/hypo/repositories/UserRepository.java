package com.iso.hypo.repositories;

import org.springframework.data.repository.CrudRepository;

import com.iso.hypo.domain.aggregate.User;

public interface UserRepository extends CrudRepository<User, String>, BrandRepositoryCustom {
	
}

