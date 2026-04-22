package com.iso.hypo.finance.application.exception;

import com.iso.hypo.common.domain.exception.DomainException;
import com.iso.hypo.finance.application.dto.FinancialInstrumentDto;

import lombok.Getter;

@Getter
public class FinancialInstrumentException extends DomainException {
	
	public static final String BRAND_NOT_FOUND = "404";
	public static final String INVALID_BRAND = "403";
	public static final String FINANCIAL_INSTRUMENT_NOT_FOUND = "404";
	
	public static final String FINANCIAL_INSTRUMENT_ALREADY_EXIST = "1001";

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