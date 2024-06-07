package com.isoceles.hypothenus.gym.domain.exception;

import lombok.Getter;

@Getter
public class DomainException extends Exception {
		
	public static final String GYM_NOT_FOUND = "404";
	
	private static final long serialVersionUID = 1L;
	private String code;
	
    public DomainException(String code, String message) {
    	super(message);
    	this.code = code;
    } 

}
