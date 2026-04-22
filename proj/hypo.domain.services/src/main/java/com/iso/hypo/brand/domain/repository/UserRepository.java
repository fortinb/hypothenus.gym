package com.iso.hypo.brand.domain.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.iso.hypo.brand.domain.model.User;

public interface UserRepository extends CrudRepository<User, String>, UserRepositoryCustom {

	Optional<User> findByEmailAndDeletedIsFalse(String email);

	Optional<User> findByIdpIdAndDeletedIsFalse(String idpId);

	Optional<User> findByUuidAndDeletedIsFalse(String userUuid);

	Page<User> findAllByDeletedIsFalse(Pageable pageable);
	
	Page<User> findAllByDeletedIsFalseAndActiveIsTrue(Pageable pageable);
}

