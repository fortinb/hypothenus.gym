package com.iso.hypo.services.exception;

import com.iso.hypo.common.exception.DomainException;

import lombok.Getter;

@Getter
public class MembershipException extends DomainException {

	public static final String MEMBERSHIP_NOT_FOUND = "404";
	public static final String BRAND_NOT_FOUND = "404";
	public static final String INVALID_BRAND = "400";
	
	private static final long serialVersionUID = 1L;
	
    public MembershipException(String trackingNumber, String code, String message) {
    	super(trackingNumber, code, message);
    } 

    public MembershipException(String trackingNumber, String code, Exception e) {
    	super(trackingNumber, code, e);
    } 
}