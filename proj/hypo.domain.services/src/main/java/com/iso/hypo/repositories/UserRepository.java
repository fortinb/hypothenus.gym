package com.iso.hypo.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.iso.hypo.domain.aggregate.User;

public interface UserRepository extends CrudRepository<User, String>, UserRepositoryCustom {

	Optional<User> findByEmailAndDeletedIsFalse(String email);

	Optional<User> findByUuidAndDeletedIsFalse(String userUuid);

	Page<User> findAllByDeletedIsFalse(Pageable pageable);
	
	Page<User> findAllByDeletedIsFalseAndActiveIsTrue(Pageable pageable);
}

