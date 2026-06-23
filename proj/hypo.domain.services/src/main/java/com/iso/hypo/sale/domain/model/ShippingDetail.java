package com.iso.hypo.sale.domain.model;

import java.time.Instant;

import com.iso.hypo.common.domain.model.finance.Cost;
import com.iso.hypo.common.domain.model.location.Address;
import com.iso.hypo.sale.domain.model.enumeration.ShippingMethodEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class ShippingDetail {

    private Address address;
    private ShippingMethodEnum shippingMethod;
    private String carrier;
    private String trackingNumber;
    private Instant shippedOn;
    private Instant deliveredOn;

	public ShippingDetail() {
	}

	public ShippingDetail(Address address, ShippingMethodEnum shippingMethod, String carrier,
			String trackingNumber, Instant shippedOn, Instant deliveredOn) {
		this.address = address;
		this.shippingMethod = shippingMethod;
		this.carrier = carrier;
		this.trackingNumber = trackingNumber;
		this.shippedOn = shippedOn;
		this.deliveredOn = deliveredOn;
	}
	
	public Cost getShippingCost(Cost baseCost) {
		switch (shippingMethod) {
			case standard:
				return baseCost; // No additional cost for standard shipping
			case express:
				return new Cost((int)(baseCost.getAmount() * 1.5), baseCost.getCurrency()); // 50% more for express
			case overnight:
				return new Cost(baseCost.getAmount() * 2, baseCost.getCurrency()); // Double for overnight
			case pickup:
				return new Cost(0, baseCost.getCurrency()); 
			case email:
				return new Cost(0, baseCost.getCurrency()); 
			default:
				return baseCost; // Default to base cost if method is unknown
		}
	}
}
