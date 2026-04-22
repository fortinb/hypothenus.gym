package com.iso.hypo.brand.application.usecase;

import org.springframework.data.domain.Page;

import com.iso.hypo.brand.application.dto.UserDto;
import com.iso.hypo.brand.application.dto.search.UserSearchDto;
import com.iso.hypo.brand.domain.exception.UserException;

public interface UserQueryService {

    UserDto find(String userUuid) throws UserException;

    Page<UserSearchDto> search(int page, int pageSize, String criteria, boolean includeInactive) throws UserException;

    Page<UserDto> list(int page, int pageSize, boolean includeInactive) throws UserException;
}