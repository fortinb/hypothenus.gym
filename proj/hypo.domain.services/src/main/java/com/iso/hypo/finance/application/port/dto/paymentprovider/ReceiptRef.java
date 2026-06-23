package com.iso.hypo.finance.application.port.dto.paymentprovider;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReceiptRef {

	private String cardType;
	
	private String cardNumberMasked;
	
	private String cardHolderName;

	private String permanentToken;
	
	private String transAmount;

	private String txnNumber;
	
	private String receiptId;
	
	private String transType;
	
	private String referenceNum;
	
	private String providerResponseCode;
	
	private boolean approved;
	
	private boolean error;
	
	// M = Match, N = No Match, P = Not processed, S = Should have been present, U = Issuer unable to process request
	private boolean cvdResultCode;
	
	// Z = ZipCode Match, Y = full match (street and zipcode)
	private boolean avsResultCode;
	
	private String ISO;
	
	private String message;
	
	private String authCode;
	
	private String transDate;
	
	private String transTime;
	
	private String ticket;
	
	private String issuerId;
}

