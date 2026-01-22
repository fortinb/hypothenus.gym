package com.iso.hypo.services.exception;

import com.iso.hypo.common.exception.DomainException;

import lombok.Getter;

@Getter
public class MemberException extends DomainException {

	public static final String MEMBERSHIP_NOT_FOUND = "404";
	public static final String BRAND_NOT_FOUND = "404";
	public static final String INVALID_BRAND = "400";
	
	private static final long serialVersionUID = 1L;
	
    public MemberException(String code, String message) {
    	super(code, message);
    } 

}

