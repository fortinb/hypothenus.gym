package com.iso.hypo.finance.application.dto;

import java.time.Instant;

import com.iso.hypo.common.application.dto.BaseEntityDto;
import com.iso.hypo.common.application.dto.enumeration.PaymentMethodEnumDto;
import com.iso.hypo.common.application.dto.enumeration.PaymentStatusEnumDto;
import com.iso.hypo.common.domain.model.finance.Cost;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class PaymentDto extends BaseEntityDto {

	private String uuid;
	private String brandUuid;
	private String memberUuid;
	private String orderUuid;
    private String financialInstrumentUuid;
	private String orderNumber;
	
    private PaymentMethodEnumDto paymentMethod;
    private PaymentStatusEnumDto paymentStatus;

    private Cost purchaseAmount;
    private Instant paidOn;

	// Transaction detail fields
	private String transactionId;
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

	public PaymentDto() {
	}

	public PaymentDto(String uuid, String orderUuid, PaymentMethodEnumDto paymentMethod,
		 String financialInstrumentUuid,
			Cost amount, Instant paidOn,
			String transactionId, String transactionReference, String gatewayProvider,
			String gatewayResponse, String gatewayResponseCode,
			Cost refundedAmount, String refundTransactionId, String refundReason, Instant refundedOn) {
		this.uuid = uuid;
		this.orderUuid = orderUuid;
		this.paymentMethod = paymentMethod;
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