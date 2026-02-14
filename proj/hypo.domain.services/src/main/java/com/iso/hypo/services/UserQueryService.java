package com.iso.hypo.services;

import org.springframework.data.domain.Page;

import com.iso.hypo.domain.dto.UserDto;
import com.iso.hypo.domain.dto.search.UserSearchDto;
import com.iso.hypo.services.exception.UserException;

public interface UserQueryService {

    UserDto find(String userUuid) throws UserException;

    Page<UserSearchDto> search(int page, int pageSize, String criteria, boolean includeInactive) throws UserException;

    Page<UserDto> list(String brandUuid, int page, int pageSize, boolean includeInactive) throws UserException;
}