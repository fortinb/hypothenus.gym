package com.iso.hypo.brand.application.exception;

import com.iso.hypo.brand.application.dto.UserDto;
import com.iso.hypo.common.domain.exception.DomainException;

import lombok.Getter;

@Getter
public class UserException extends DomainException {
	
	public static final String USER_NOT_FOUND = "404";
	public static final String USER_ALREADY_EXIST = "1001";
	public static final String ROLE_ASSIGNMENT_NOT_ALLOWED = "1002";
	public static final String USER_ALREADY_EXIST_IN_IDP = "1001";
	
	private UserDto userDto;
	
	private static final long serialVersionUID = 1L;
	public static final String ASSIGNROLE_FAILED = "500";
	public static final String UNASSIGNROLE_FAILED = "500";

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