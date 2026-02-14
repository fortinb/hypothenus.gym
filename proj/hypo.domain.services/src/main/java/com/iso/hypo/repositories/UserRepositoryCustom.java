package com.iso.hypo.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.iso.hypo.domain.aggregate.User;
import com.iso.hypo.domain.dto.search.UserSearchDto;

public interface UserRepositoryCustom {

	Page<UserSearchDto> searchAutocomplete(String criteria, Pageable pageable, boolean includeInactive);
	
    Optional<User> activate(String userUuid);

    Optional<User> deactivate(String userUuid);

    void delete(String userUuid, String deletedBy);

    long deleteAll(String deletedBy);
}