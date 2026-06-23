package com.iso.hypo.sale.domain.model.enumeration;

public enum OrderStatusEnum {
	created, 
	submitted,
	paymentFailed,
	completed,
	cancelled;

	OrderStatusEnum() {

	}
}
