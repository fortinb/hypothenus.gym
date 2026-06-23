package com.iso.hypo.sale.domain.model;

import com.iso.hypo.sale.domain.model.enumeration.PaymentMethodEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class PaymentDetail {

    private PaymentMethodEnum paymentMethod;
    
    private String financialInstrumentUuid;

	public PaymentDetail() {
	}

	public PaymentDetail(PaymentMethodEnum paymentMethod, String financialInstrumentUuid) {
		this.paymentMethod = paymentMethod;
		this.financialInstrumentUuid = financialInstrumentUuid;
	}
}
