package com.iso.hypo.services.exception;

import com.iso.hypo.common.exception.DomainException;
import com.iso.hypo.domain.dto.MemberDto;

import lombok.Getter;

@Getter
public class MemberException extends DomainException {
	
	public static final String MEMBER_NOT_FOUND = "404";
	public static final String BRAND_NOT_FOUND = "404";
	public static final String INVALID_BRAND = "403";
	
	private MemberDto memberDto;
	
	private static final long serialVersionUID = 1L;
	
	public MemberException(String trackingNumber, String code, String message, MemberDto memberDto) {
		super(trackingNumber, code, message);
		this.memberDto = memberDto;
	}
	
    public MemberException(String trackingNumber, String code, String message) {
    	super(trackingNumber, code, message);
    } 
    
    public MemberException(String trackingNumber, String code, Exception e) {
    	super(trackingNumber, code, e);
    } 
}