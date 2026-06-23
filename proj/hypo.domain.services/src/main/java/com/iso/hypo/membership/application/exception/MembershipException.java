package com.iso.hypo.membership.application.exception;

import com.iso.hypo.common.domain.exception.DomainException;

import lombok.Getter;

@Getter
public class MembershipException extends DomainException {

	public static final String MEMBERSHIP_NOT_FOUND = "404";
	public static final String MEMBER_NOT_FOUND = "404";
	public static final String MEMBERSHIP_PLAN_NOT_FOUND = "404";
	public static final String BRAND_NOT_FOUND = "404";
	public static final String INVALID_BRAND = "403";

	public static final String MISSING_MEMBERSHIP_PLAN = "1001";
	public static final String MISSING_MEMBER = "1002";
	public static final String MISSING_ORDER = "1003";
	
	private static final long serialVersionUID = 1L;

    public MembershipException(String trackingNumber, String code, String message) {
    	super(trackingNumber, code, message);
    } 

    public MembershipException(String trackingNumber, String code, Exception e) {
    	super(trackingNumber, code, e);
    } 
}