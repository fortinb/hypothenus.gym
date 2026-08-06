package com.iso.hypo.finance.domain.model;

import java.time.Instant;

import com.iso.hypo.common.application.dto.enumeration.PaymentMethodEnumDto;
import com.iso.hypo.common.domain.model.BaseEntity;
import com.iso.hypo.common.domain.model.finance.Cost;
import com.iso.hypo.finance.domain.model.enumeration.PaymentStatusEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class Payment extends BaseEntity  {

	private String id;
	
	private String uuid;
	private String brandUuid;
	private String memberUuid;
	private String orderUuid;
    private String financialInstrumentUuid;
	private String orderNumber;
	
    private PaymentMethodEnumDto paymentMethod;
    private PaymentStatusEnum paymentStatus;

    private Cost purchaseAmount;
    private Instant paidOn;

	// Transaction detail fields
	private String transactionId;
	private String paymentId;
	private String transactionReference;
	private String gatewayProvider;
	private String gatewayResponse;
	private String gatewayResponseCode;
	private String gatewayISOResponseCode;

	// Refund fields
	private Cost refundedAmount;
	private String refundTransactionId;
	private String refundReason;
	private Instant refundedOn;
	
	private String paymentServiceProviderRawResponse;

	public Payment() {
	}

	public String formatProvidePaymentAmount() {
		if (purchaseAmount != null) {
			return String.format(java.util.Locale.US, "%.2f", purchaseAmount.getAmount() / 100.0);
		}
		
		return null;
	}
	public Payment(String uuid, String orderUuid, PaymentMethodEnumDto paymentMethod,
			PaymentStatusEnum paymentStatus, String financialInstrumentUuid,
			Cost amount, Instant paidOn,
			String transactionId, String transactionReference, String gatewayProvider,
			String gatewayResponse, String gatewayResponseCode,
			Cost refundedAmount, String refundTransactionId, String refundReason, Instant refundedOn) {
		this.uuid = uuid;
		this.orderUuid = orderUuid;
		this.paymentMethod = paymentMethod;
		this.paymentStatus = paymentStatus;
		this.financialInstrumentUuid = financialInstrumentUuid;
		this.purchaseAmount = amount;
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