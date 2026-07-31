package com.iso.hypo.finance.application.port.dto.paymentprovider;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreditCardRef {

	private String cardType;
	
	private String cardHolderName;
	
	private String cardNumber;

	// Expire date in MMYY format
	private String expirationDate;

	// Card verification digits (CVD)
	private String cvd;

	private String phoneNumber;

	private String email;
	
	private String zipCode;
	
	private String customerId;
	
	private String countryCode;
	
	private String issuerId;
	
	// After verification
	private String temporaryToken;
	
	// After registration
	private String permanentToken;
	
}
