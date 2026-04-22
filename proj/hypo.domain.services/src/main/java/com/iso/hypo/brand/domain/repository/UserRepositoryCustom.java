package com.iso.hypo.brand.domain.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.iso.hypo.brand.application.dto.search.UserSearchDto;
import com.iso.hypo.brand.domain.model.User;

public interface UserRepositoryCustom {

	Page<UserSearchDto> searchAutocomplete(String criteria, Pageable pageable, boolean includeInactive);
	
    Optional<User> activate(String userUuid);

    Optional<User> deactivate(String userUuid);

    void delete(String userUuid, String deletedBy);

    long deleteAll(String deletedBy);
}