package com.iso.hypo.services.exception;

import com.iso.hypo.common.exception.DomainException;

import lombok.Getter;

@Getter
public class MembershipPlanException extends DomainException {
	
	public static final String MEMBERSHIPPLAN_NOT_FOUND = "404";
	public static final String MEMBERSHIP_NOT_FOUND = "404";
	public static final String BRAND_NOT_FOUND = "404";
	public static final String INVALID_BRAND = "403";
	
	private static final long serialVersionUID = 1L;
	
    public MembershipPlanException(String trackingNumber, String code, String message) {
    	super(trackingNumber, code, message);
    } 
    
    public MembershipPlanException(String trackingNumber, String code, Exception e) {
    	super(trackingNumber, code, e);
    } 
}