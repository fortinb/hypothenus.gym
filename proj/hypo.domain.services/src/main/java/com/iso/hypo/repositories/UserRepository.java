package com.iso.hypo.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.iso.hypo.domain.aggregate.User;

public interface UserRepository extends CrudRepository<User, String>, UserRepositoryCustom {

	Optional<User> findByEmailAndIsDeletedIsFalse(String email);

	Optional<User> findByUuidAndIsDeletedIsFalse(String userUuid);

	Page<User> findAllByIsDeletedIsFalse(Pageable pageable);
	
	Page<User> findAllByIsDeletedIsFalseAndIsActiveIsTrue(Pageable pageable);
}

