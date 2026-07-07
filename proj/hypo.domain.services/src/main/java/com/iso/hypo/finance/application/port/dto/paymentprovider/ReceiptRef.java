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
	
	private boolean cvdResultCode;
	
	private boolean avsResultCode;
	
	private String ISO;
	
	private String message;
	
	private String authCode;
	
	private String transDate;
	
	private String transTime;
	
	private String ticket;
	
	private String issuerId;

	/**
	 * Full, unmodified response object returned by the payment provider.
	 * This field is intentionally typed as {@link Object} so that no
	 * provider-specific class (e.g. Moneris) leaks into this DTO.
	 *
	 * <p>The adapter is responsible for setting any Jackson-serializable value
	 * here (a generated model POJO, a {@code Map<String,Object>}, etc.).
	 * Spring Data MongoDB will persist it as a BSON sub-document, which is
	 * readable as plain JSON in the database.
	 *
	 * <p>Example (in the Moneris adapter):
	 * <pre>{@code
	 * receipt.setProviderRawResponse(monerисPaymentObject);   // generated POJO
	 * // — or — convert to Map to avoid any serialisation surprises:
	 * receipt.setProviderRawResponse(
	 *     new ObjectMapper().convertValue(monerisPayment, Map.class));
	 * }</pre>
	 */
	private Object providerRawResponse;
}

