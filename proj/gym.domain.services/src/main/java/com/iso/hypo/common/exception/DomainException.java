package com.iso.hypo.common.exception;

import lombok.Getter;

@Getter
public class DomainException extends Exception {
	
	private static final long serialVersionUID = 1L;
	private String code;
	
    public DomainException(String code, String message) {
    	super(message);
    	this.code = code;
    } 

}