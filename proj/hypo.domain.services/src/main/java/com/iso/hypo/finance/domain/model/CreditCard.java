package com.iso.hypo.finance.domain.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreditCard {
	
	private String cardType;
	
	private String cardHolderName;
	
	private String cardNumber;
	
	private String expirationDate;
	
	// After verification
	private String temporaryToken;
	
	// After registration
	private String permanentToken;
	
	// Issuer ID - Bank Identification Number
	private String issuerId;
	
	public CreditCard() {
	}
	
	public CreditCard(String cardNumber, String cardHolderName, String expirationDate, String permanentToken) {
		this.cardNumber = cardNumber;
		this.cardHolderName = cardHolderName;
		this.expirationDate = expirationDate;
		this.permanentToken = permanentToken;
	}
}
