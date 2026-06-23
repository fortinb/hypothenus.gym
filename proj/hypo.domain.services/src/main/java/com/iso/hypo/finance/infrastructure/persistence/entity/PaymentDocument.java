package com.iso.hypo.finance.infrastructure.persistence.entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.iso.hypo.common.domain.model.finance.Cost;
import com.iso.hypo.common.infrastructure.persistence.entity.BaseDocument;
import com.iso.hypo.finance.domain.model.enumeration.PaymentStatusEnum;
import com.iso.hypo.sale.domain.model.enumeration.PaymentMethodEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document("payment")
public class PaymentDocument extends BaseDocument {
	
	@Id
	private String id;
	
	@Indexed
	private String uuid;
	
	@Indexed
	private String brandUuid;
	
	@Indexed
	private String memberUuid;

	@Indexed
	private String orderUuid;
	
    private PaymentMethodEnum paymentMethod;
    private PaymentStatusEnum paymentStatus;
    private String financialInstrumentUuid;
    private Cost amount;
    private Instant paidOn;

	// Transaction detail fields
	private String transactionId;
	private String transactionReference;
	private String gatewayProvider;
	private String gatewayResponse;
	private String gatewayResponseCode;

	// Refund fields
	private Cost refundedAmount;
	private String refundTransactionId;
	private String refundReason;
	private Instant refundedOn;
	
	public PaymentDocument() {
	}

	public PaymentDocument(String id, String uuid, String brandUuid, String memberUuid, String orderUuid,
			PaymentMethodEnum paymentMethod, PaymentStatusEnum paymentStatus, String financialInstrumentUuid,
			Cost amount, Instant paidOn, String transactionId, String transactionReference, String gatewayProvider,
			String gatewayResponse, String gatewayResponseCode, Cost refundedAmount, String refundTransactionId,
			String refundReason, Instant refundedOn) {
		this.id = id;
		this.uuid = uuid;
		this.brandUuid = brandUuid;
		this.memberUuid = memberUuid;
		this.orderUuid = orderUuid;
		this.paymentMethod = paymentMethod;
		this.paymentStatus = paymentStatus;
		this.financialInstrumentUuid = financialInstrumentUuid;
		this.amount = amount;
		this.paidOn = paidOn;
		this.transactionId = transactionId;
		this.transactionReference = transactionReference;
		this.gatewayProvider = gatewayProvider;
		this.gatewayResponse = gatewayResponse;
		this.gatewayResponseCode = gatewayResponseCode;
		this.refundedAmount = refundedAmount;
		this.refundTransactionId = refundTransactionId;
		this.refundReason = refundReason;
		this.refundedOn = refundedOn;
	}
	
	
}
