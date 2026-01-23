package com.iso.hypo.services.exception;

import com.iso.hypo.common.exception.DomainException;

import lombok.Getter;

@Getter
public class BrandException extends DomainException {
	
	public static final String BRAND_NOT_FOUND = "404";	
	public static final String BRAND_CODE_ALREADY_EXIST = "2001";

	private static final long serialVersionUID = 1L;
	
    public BrandException(String trackingNumber, String code, String message) {
    	super(trackingNumber, code, message);
    } 
    
    public BrandException(String trackingNumber, String code, Exception e) {
    	super(trackingNumber, code, e);
    } 
}