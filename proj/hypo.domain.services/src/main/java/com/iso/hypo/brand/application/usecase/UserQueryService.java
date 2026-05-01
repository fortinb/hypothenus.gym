package com.iso.hypo.brand.application.usecase;

import java.util.Optional;

import com.iso.hypo.brand.application.dto.UserDto;
import com.iso.hypo.brand.application.dto.search.UserSearchDto;
import com.iso.hypo.brand.application.exception.UserException;
import com.iso.hypo.common.application.dto.PageResultDto;

public interface UserQueryService {

    UserDto find(String userUuid) throws UserException;

    Optional<UserDto> findByEmail(String email) throws UserException;

    Optional<UserDto> findByIdpId(String idpId) throws UserException;

    PageResultDto<UserSearchDto> search(int page, int pageSize, String criteria, boolean includeInactive) throws UserException;

    PageResultDto<UserDto> list(int page, int pageSize, boolean includeInactive) throws UserException;
}