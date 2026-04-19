package com.iso.hypo.admin.papi.dto.financial;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreditCardDto {

	private String cardNumber;

	private String cardHolderName;

	private String expirationDate;

	private String cvv;

	public CreditCardDto() {
	}

	public CreditCardDto(String cardNumber, String cardHolderName, String expirationDate, String cvv) {
		this.cardNumber = cardNumber;
		this.cardHolderName = cardHolderName;
		this.expirationDate = expirationDate;
		this.cvv = cvv;
	}
}
