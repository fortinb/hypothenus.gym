package com.iso.hypo.brand.domain.repository;

import java.util.Optional;

import com.iso.hypo.brand.domain.model.User;
import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;

public interface UserRepository {

	Optional<User> findByEmailAndDeletedIsFalse(String email);

	Optional<User> findByIdpIdAndDeletedIsFalse(String idpId);

	Optional<User> findByUuidAndDeletedIsFalse(String userUuid);

	PageResult<User> findAllByDeletedIsFalse(PageRequest pageRequest);
	
	PageResult<User> findAllByDeletedIsFalseAndActiveIsTrue(PageRequest pageRequest);
	
	User save(User user);

    void delete(User user);

    void deleteAll();
}

