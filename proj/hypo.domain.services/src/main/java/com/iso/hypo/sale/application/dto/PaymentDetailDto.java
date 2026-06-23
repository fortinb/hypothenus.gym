package com.iso.hypo.sale.application.dto;

import com.iso.hypo.common.application.dto.enumeration.PaymentMethodEnumDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentDetailDto {

	private PaymentMethodEnumDto paymentMethod;

	private String financialInstrumentUuid;

	public PaymentDetailDto() {
	}
}
