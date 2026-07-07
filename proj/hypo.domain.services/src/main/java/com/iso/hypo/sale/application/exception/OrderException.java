package com.iso.hypo.sale.application.exception;

import com.iso.hypo.common.domain.exception.DomainException;
import com.iso.hypo.sale.application.dto.OrderDto;

import lombok.Getter;

@Getter
public class OrderException extends DomainException {

	public static final String BRAND_NOT_FOUND = "404";
	public static final String INVALID_BRAND = "403";
	public static final String ORDER_NOT_FOUND = "404";
	public static final String MEMBER_NOT_FOUND = "404";
	public static final String MEMBERSHIP_PLAN_NOT_FOUND = "404";
	
	public static final String TRIAL_MEMBERSHIP_PLAN_ONLY_FOR_NEW_MEMBER = "1001";
	public static final String ORDER_ALREADY_PROCESSED = "1002";
	public static final String ORDER_CANCELLED = "1003";
	public static final String MISSING_BILLING_DETAIL = "1004";
	public static final String MISSING_SHIPPING_DETAIL = "1005";
	public static final String MISSING_PAYMENT_DETAIL = "1006";	
	public static final String ORDER_SUBMIT_FAILED = "1007";	
	public static final String ORDER_PAYMENT_FAILED = "1008";
	public static final String MEMBERSHIP_CREATION_FAILED  = "1009";
	public static final String ORDER_ALREADY_EXISTS =  "1010";


	private OrderDto orderDto;

	private static final long serialVersionUID = 1L;


	public OrderException(String trackingNumber, String code, String message, OrderDto orderDto) {
		super(trackingNumber, code, message);
		this.orderDto = orderDto;
	}

	public OrderException(String trackingNumber, String code, String message) {
		super(trackingNumber, code, message);
	}

	public OrderException(String trackingNumber, String code, Exception e) {
		super(trackingNumber, code, e);
	}
}
