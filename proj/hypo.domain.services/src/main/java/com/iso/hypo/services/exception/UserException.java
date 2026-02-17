package com.iso.hypo.services.exception;

import com.iso.hypo.common.exception.DomainException;
import com.iso.hypo.domain.dto.UserDto;

import lombok.Getter;

@Getter
public class UserException extends DomainException {
	
	public static final String USER_NOT_FOUND = "404";
	public static final String USER_ALREADY_EXIST = "1001";
	
	private UserDto userDto;
	
	private static final long serialVersionUID = 1L;
	public static final String ASSIGNROLE_FAILED = "500";
	public static final String UNASSIGNROLE_FAILED = "500";
	public static final String ROLE_ASSIGNMENT_NOT_ALLOWED = "201";

	
	public UserException(String trackingNumber, String code, String message, UserDto userDto) {
		super(trackingNumber, code, message);
		this.userDto = userDto;
	}
	
    public UserException(String trackingNumber, String code, String message) {
    	super(trackingNumber, code, message);
    } 
    
    public UserException(String trackingNumber, String code, Exception e) {
    	super(trackingNumber, code, e);
    } 
}