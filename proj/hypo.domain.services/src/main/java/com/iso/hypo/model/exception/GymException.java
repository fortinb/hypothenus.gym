package com.iso.hypo.model.exception;

import com.iso.hypo.common.exception.DomainException;

import lombok.Getter;

@Getter
public class GymException extends DomainException {
	
	public static final String GYM_NOT_FOUND = "404";
	public static final String GYM_CODE_ALREADY_EXIST = "1001";
	
	public static final String COURSE_CODE_ALREADY_EXIST = "1002";
	public static final String COACH_NOT_FOUND = "404";
	public static final String COURSE_NOT_FOUND = "404";
	
	public static final String BRAND_NOT_FOUND = "404";
	public static final String INVALID_BRAND = "400";
	public static final String INVALID_GYM = "404";
	
	private static final long serialVersionUID = 1L;
	
    public GymException(String code, String message) {
    	super(code, message);
    } 

}