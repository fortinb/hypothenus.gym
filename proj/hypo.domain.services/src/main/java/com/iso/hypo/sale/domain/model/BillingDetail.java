package com.iso.hypo.sale.domain.model;

import com.iso.hypo.common.domain.model.location.Address;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class BillingDetail {

    private Address address;
    private String email;
    private String name;

	public BillingDetail() {
	}

	public BillingDetail(Address address, String email, String name) {
		this.address = address;
		this.email = email;
		this.name = name;
	}
}
