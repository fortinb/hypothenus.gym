package com.iso.hypo.services.exception;

import com.iso.hypo.common.exception.DomainException;
import com.iso.hypo.domain.dto.GymDto;

import lombok.Getter;

@Getter
public class GymException extends DomainException {
	
	public static final String GYM_NOT_FOUND = "404";
	public static final String GYM_CODE_ALREADY_EXIST = "1001";
	public static final String BRAND_NOT_FOUND = "404";
	public static final String INVALID_BRAND = "403";
	public static final String INVALID_GYM = "403";
	
	private GymDto gymDto;
	private static final long serialVersionUID = 1L;
	
	public GymException(String trackingNumber, String code, String message, GymDto gymDto) {
		super(trackingNumber, code, message);
		this.gymDto = gymDto;
	}
	
    public GymException(String trackingNumber, String code, String message) {
    	super(trackingNumber, code, message);
    } 
    
    public GymException(String trackingNumber, String code, Exception e) {
    	super(trackingNumber, code, e);
    } 
}