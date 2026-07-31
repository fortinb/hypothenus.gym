package com.iso.hypo.finance.application.exception;

import com.iso.hypo.common.domain.exception.DomainException;
import com.iso.hypo.finance.application.dto.FinancialInstrumentDto;

import lombok.Getter;

@Getter
public class FinancialInstrumentException extends DomainException {
	
	public static final String BRAND_NOT_FOUND = "404";
	public static final String MEMBER_NOT_FOUND = "404";
	public static final String INVALID_BRAND = "403";
	public static final String FINANCIAL_INSTRUMENT_NOT_FOUND = "404";
	
	public static final String FINANCIAL_INSTRUMENT_ALREADY_EXIST = "1001";
	public static final String CARD_VERIFICATION_FAILED = "1002";
	public static final String CARD_VERIFICATION_CVD_FAILED = "1003";
	public static final String CARD_VERIFICATION_AVS_FAILED = "1004";
	public static final String CARD_ZIPCODE_REQUIRED = "1005";
	public static final String CARD_HOLDER_NAME_REQUIRED = "1006";
	public static final String CARD_NUMBER_REQUIRED = "1007";
	public static final String CARD_EXPIRY_DATE_REQUIRED = "1008";
	public static final String CARD_CVD_REQUIRED = "1009";
	public static final String CARD_REGISTRATION_FAILED  = "1010";
	public static final String CARD_DELETION_FAILED = "1011";
	
	private FinancialInstrumentDto financialInstrumentDto;
	
	private static final long serialVersionUID = 1L;

	
	public FinancialInstrumentException(String trackingNumber, String code, String message, FinancialInstrumentDto financialInstrumentDto) {
		super(trackingNumber, code, message);
		this.financialInstrumentDto = financialInstrumentDto;
	}
	
    public FinancialInstrumentException(String trackingNumber, String code, String message) {
    	super(trackingNumber, code, message);
    } 
    
    public FinancialInstrumentException(String trackingNumber, String code, Exception e) {
    	super(trackingNumber, code, e);
    } 
}