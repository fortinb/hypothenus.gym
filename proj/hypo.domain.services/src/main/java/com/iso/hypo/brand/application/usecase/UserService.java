package com.iso.hypo.brand.application.usecase;

import com.iso.hypo.brand.application.dto.UserDto;
import com.iso.hypo.brand.application.exception.UserException;

public interface UserService {
	
	UserDto createAdmin(UserDto userDto) throws UserException;
	
	UserDto create(UserDto userDto, String password, String groupName) throws UserException;

	void delete(String userUuid) throws UserException;

	UserDto activate(String userUuid) throws UserException;

	UserDto deactivate(String userUuid) throws UserException;

	UserDto update(UserDto userDto) throws UserException;
	
	UserDto patch(UserDto userDto) throws UserException;
}