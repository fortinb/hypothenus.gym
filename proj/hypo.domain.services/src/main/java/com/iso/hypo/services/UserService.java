package com.iso.hypo.services;

import com.iso.hypo.domain.dto.UserDto;
import com.iso.hypo.services.exception.UserException;

public interface UserService {
	
	UserDto create(UserDto userDto) throws UserException;

	void delete(String userUuid) throws UserException;

	UserDto activate(String userUuid) throws UserException;

	UserDto deactivate(String userUuid) throws UserException;

	UserDto update(UserDto userDto) throws UserException;
	
	UserDto patch(UserDto userDto) throws UserException;
}