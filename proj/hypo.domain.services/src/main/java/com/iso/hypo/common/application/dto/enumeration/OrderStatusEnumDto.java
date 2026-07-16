package com.iso.hypo.common.application.dto.enumeration;

public enum OrderStatusEnumDto {
	idle,
	created, 
	submitted,
	paymentFailed,
	paymentSucceded,
	completed,
	cancelled;

	OrderStatusEnumDto() {
	}
}
