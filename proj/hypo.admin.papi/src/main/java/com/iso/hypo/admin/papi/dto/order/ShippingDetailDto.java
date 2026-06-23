package com.iso.hypo.admin.papi.dto.order;

import com.iso.hypo.admin.papi.dto.location.AddressDto;
import com.iso.hypo.common.application.dto.enumeration.ShippingMethodEnumDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShippingDetailDto {

	private AddressDto address;

	private ShippingMethodEnumDto shippingMethod;

	private String carrier;

	private String trackingNumber;

	public ShippingDetailDto() {
	}
}
