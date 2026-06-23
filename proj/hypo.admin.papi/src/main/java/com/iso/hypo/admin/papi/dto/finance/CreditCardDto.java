package com.iso.hypo.admin.papi.dto.finance;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreditCardDto {

	private String cardHolderName;
	
	private String cardNumber;

	// Expire date in MMYY format
	private String expirationDate;

	// Card verification digits (CVD)
	private String cvd;
	
	private String zipCode;

	public CreditCardDto() {
	}

	public CreditCardDto(String cardNumber, String cardHolderName, String expirationDate, String cvd, String zipCode) {
		this.cardNumber = cardNumber;
		this.cardHolderName = cardHolderName;
		this.expirationDate = expirationDate;
		this.cvd = cvd;
		this.zipCode = zipCode;
	}
}
