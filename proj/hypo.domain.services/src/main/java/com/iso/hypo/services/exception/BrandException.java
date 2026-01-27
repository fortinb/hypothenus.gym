package com.iso.hypo.services.exception;

import com.iso.hypo.common.exception.DomainException;
import com.iso.hypo.domain.dto.BrandDto;

import lombok.Getter;

@Getter
public class BrandException extends DomainException {

	public static final String BRAND_NOT_FOUND = "404";
	public static final String BRAND_CODE_ALREADY_EXIST = "2001";
	
	private BrandDto brandDto;
	private static final long serialVersionUID = 1L;

	public BrandException(String trackingNumber, String code, String message, BrandDto brandDto) {
		super(trackingNumber, code, message);
		this.brandDto = brandDto;
	}

	public BrandException(String trackingNumber, String code, String message) {
		super(trackingNumber, code, message);
	}

	public BrandException(String trackingNumber, String code, Exception e) {
		super(trackingNumber, code, e);
	}
}