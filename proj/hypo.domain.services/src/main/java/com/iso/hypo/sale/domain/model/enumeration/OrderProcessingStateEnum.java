package com.iso.hypo.sale.domain.model.enumeration;

public enum OrderProcessingStateEnum {
	idle, 
	takePayment,
	createMembership,
	completed;

	OrderProcessingStateEnum() {

	}
}
