package com.iso.hypo.admin.papi.dto.model;

import java.time.Instant;

import com.iso.hypo.admin.papi.dto.BaseDto;
import com.iso.hypo.common.application.dto.enumeration.PaymentMethodEnumDto;
import com.iso.hypo.common.domain.model.finance.Cost;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentDto extends BaseDto {

	private String uuid;
	private String brandUuid;
	private String memberUuid;
	private String orderUuid;
	
    private PaymentMethodEnumDto paymentMethod;
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

	public PaymentDto() {
	}

	public PaymentDto(String uuid, String brandUuid, String memberUuid, String orderUuid,
			PaymentMethodEnumDto paymentMethod, String financialInstrumentUuid,
			Cost amount, Instant paidOn, String transactionId, String transactionReference, String gatewayProvider,
			String gatewayResponse, String gatewayResponseCode, Cost refundedAmount, String refundTransactionId,
			String refundReason, Instant refundedOn) {
		this.uuid = uuid;
		this.brandUuid = brandUuid;
		this.memberUuid = memberUuid;
		this.orderUuid = orderUuid;
		this.paymentMethod = paymentMethod;
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
