package com.iso.hypo.domain.financial;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreditCard {
	
	private String cardNumber;
	
	private String cardHolderName;
	
	private String expirationDate;
	
	private String cvv;
	
	private String permanentToken;
	
	public CreditCard() {
	}
	
	public CreditCard(String cardNumber, String cardHolderName, String expirationDate, String cvv, String permanentToken) {
		this.cardNumber = cardNumber;
		this.cardHolderName = cardHolderName;
		this.expirationDate = expirationDate;
		this.cvv = cvv;
		this.permanentToken = permanentToken;
	}
}
