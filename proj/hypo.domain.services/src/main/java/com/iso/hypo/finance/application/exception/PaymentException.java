package com.iso.hypo.finance.application.exception;

import com.iso.hypo.common.domain.exception.DomainException;
import com.iso.hypo.finance.application.dto.PaymentDto;

import lombok.Getter;

@Getter
public class PaymentException extends DomainException {
	
	public static final String BRAND_NOT_FOUND = "404";
	public static final String MEMBER_NOT_FOUND =  "404";
	public static final String INVALID_BRAND = "403";
	public static final String FINANCIAL_INSTRUMENT_NOT_FOUND = "404";
	public static final String INVALID_AMOUNT = "1001";
	public static final String MISSING_CREDIT_CARD_INFORMATION = "1002";
	public static final String MISSING_MEMBER_INFORMATION = "1003";
	public static final String MISSING_ORDER_INFORMATION = "1004";
	public static final String PAYMENT_PROCESSING_FAILED = "1005";
	public static final String MISSING_FINANCIAL_INSTRUMENT_INFORMATION  = "1006";
	
	private PaymentDto paymentDto;
	
	private static final long serialVersionUID = 1L;

	public PaymentException(String trackingNumber, String code, String message, PaymentDto paymentDto) {
		super(trackingNumber, code, message);
		this.paymentDto = paymentDto;
	}
	
    public PaymentException(String trackingNumber, String code, String message) {
    	super(trackingNumber, code, message);
    } 
    
    public PaymentException(String trackingNumber, String code, Exception e) {
    	super(trackingNumber, code, e);
    } 
}