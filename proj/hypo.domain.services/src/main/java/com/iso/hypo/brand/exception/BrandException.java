package com.iso.hypo.brand.exception;

import com.iso.hypo.common.exception.DomainException;

import lombok.Getter;

@Getter
public class BrandException extends DomainException {
	
	public static final String BRAND_NOT_FOUND = "404";	
	public static final String BRAND_CODE_ALREADY_EXIST = "2001";
	public static final String MEMBERSHIPPLAN_NOT_FOUND = "404";
	public static final String MEMBERSHIP_NOT_FOUND = "404";
	public static final String INVALID_BRAND = "404";
	
	private static final long serialVersionUID = 1L;
	
    public BrandException(String code, String message) {
    	super(code, message);
    } 

}