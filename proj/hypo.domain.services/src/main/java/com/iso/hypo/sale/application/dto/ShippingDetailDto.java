package com.iso.hypo.sale.application.dto;

import java.time.Instant;

import com.iso.hypo.common.application.dto.enumeration.ShippingMethodEnumDto;
import com.iso.hypo.common.application.dto.location.AddressDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShippingDetailDto {

	private AddressDto address;

	private ShippingMethodEnumDto shippingMethod;

	private String carrier;

	private String trackingNumber;

	private Instant shippedOn;

	private Instant deliveredOn;

	public ShippingDetailDto() {
	}
}
