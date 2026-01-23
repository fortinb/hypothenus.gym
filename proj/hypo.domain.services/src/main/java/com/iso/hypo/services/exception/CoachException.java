package com.iso.hypo.services.exception;

import com.iso.hypo.common.exception.DomainException;

import lombok.Getter;

@Getter
public class CoachException extends DomainException {
	
	public static final String GYM_NOT_FOUND = "404";
	public static final String COACH_NOT_FOUND = "404";
	public static final String INVALID_BRAND = "400";
	public static final String INVALID_GYM = "400";
	
	private static final long serialVersionUID = 1L;
	
    public CoachException(String code, String message) {
    	super(code, message);
    } 
    
    public CoachException(String code, Exception e) {
    	super(code, e);
    } 
}

