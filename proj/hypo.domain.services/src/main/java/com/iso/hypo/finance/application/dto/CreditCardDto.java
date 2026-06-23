package com.iso.hypo.finance.application.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreditCardDto {

	private String cardType;
	
	private String cardHolderName;
	
	private String cardNumber;

	// Expire date in MMYY format
	private String expirationDate;

	// Card verification digits (CVD)
	private String cvd;
	
	private String zipCode;

	public CreditCardDto() {
	}

	public CreditCardDto(String cardType, String cardNumber, String cardHolderName, String expirationDate, String cvd) {
		this.cardType = cardType;
		this.cardNumber = cardNumber;
		this.cardHolderName = cardHolderName;
		this.expirationDate = expirationDate;
		this.cvd = cvd;
	}
}
