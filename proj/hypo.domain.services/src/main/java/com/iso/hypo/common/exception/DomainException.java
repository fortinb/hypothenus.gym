package com.iso.hypo.common.exception;

import lombok.Getter;

@Getter
public class DomainException extends Exception {
	
	public static final String FIND_FAILED = "100";
	public static final String CREATION_FAILED = "101";
	public static final String UPDATE_FAILED = "102";
	public static final String DELETE_FAILED = "103";
	public static final String ACTIVATION_FAILED =  "104";
	public static final String DEACTIVATION_FAILED ="105";
	
	private static final long serialVersionUID = 1L;
	private String code;
	
    public DomainException(String code, String message) {
    	super(message);
    	this.code = code;
    } 
    
    public DomainException(String code, Exception e) {
		super(e);
		this.code = code;
	}

}
