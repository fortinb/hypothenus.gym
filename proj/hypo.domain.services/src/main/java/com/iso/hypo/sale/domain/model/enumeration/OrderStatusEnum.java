package com.iso.hypo.sale.domain.model.enumeration;

public enum OrderStatusEnum {
	idle,
	created, 
	submitted,
	paymentFailed,
	paymentSucceded,
	completed,
	cancelled;

	OrderStatusEnum() {

	}
}
