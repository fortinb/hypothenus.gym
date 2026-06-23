package com.iso.hypo.sale.application.port.dto;

import com.iso.hypo.common.application.dto.enumeration.PaymentMethodEnumDto;
import com.iso.hypo.common.application.dto.enumeration.PaymentStatusEnumDto;
import com.iso.hypo.common.domain.model.finance.Cost;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class PaymentRef {

	private String uuid;
	private String brandUuid;
	private String memberUuid;
	private String orderUuid;
    private String financialInstrumentUuid;
	private String orderNumber;
	
    private PaymentMethodEnumDto paymentMethod;
    private PaymentStatusEnumDto paymentStatus;

    private Cost purchaseAmount;

	// Refund fields
	private Cost refundedAmount;
	private String refundReason;

	public PaymentRef() {
	}
}