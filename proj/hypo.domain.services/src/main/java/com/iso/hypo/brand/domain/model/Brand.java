package com.iso.hypo.brand.domain.model;

import java.time.Instant;
import java.util.List;

import com.iso.hypo.common.domain.model.BaseEntity;
import com.iso.hypo.common.domain.model.contact.Contact;
import com.iso.hypo.common.domain.model.contact.PhoneNumber;
import com.iso.hypo.common.domain.model.finance.Currency;
import com.iso.hypo.common.domain.model.location.Address;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Brand extends BaseEntity {
	
	private String id;
	
	private String uuid;

	private String code;
	
	private String name;
	
	private Address address;
	
	private Currency currency;
	
	private String email;
	
	private String note;
	
	private String logoUri;
	
	private List<Contact> contacts;
	
	private List<PhoneNumber> phoneNumbers;
	
	public Brand() {
		super();
	}
	
	public Brand(String code, String name, Address address, Currency currency, String email, String logoUri, boolean active,
			List<PhoneNumber> phoneNumbers, List<Contact> contacts, Instant activatedOn, Instant deactivatedOn) {
		super(active);
		this.code = code;
		this.name = name;
		this.address = address;
		this.currency = currency;
		this.email = email;
		this.logoUri = logoUri;
		this.phoneNumbers = phoneNumbers;
		this.contacts = contacts;
		this.activatedOn = activatedOn;
		this.deactivatedOn = deactivatedOn;
	}
}
