package com.iso.hypo.brand.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.iso.hypo.common.domain.model.contact.Contact;
import com.iso.hypo.common.domain.model.contact.PhoneNumber;
import com.iso.hypo.common.domain.model.finance.Currency;
import com.iso.hypo.common.domain.model.location.Address;
import com.iso.hypo.common.infrastructure.persistence.entity.BaseDocument;
import com.mongodb.lang.NonNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document("brand")
public class BrandDocument extends BaseDocument {

	@Id
	private String id;

	@Indexed
	private String uuid;

	@Indexed(unique = true)
	@NonNull
	private String code;

	@NonNull
	private String name;

	private Address address;
	
	private Currency currency;

	private String email;

	private String note;

	private String logoUri;

	private List<Contact> contacts;

	private List<PhoneNumber> phoneNumbers;

	public BrandDocument() {
		super();
	}

	public BrandDocument(String code, String name, Address address, Currency currency, String email, String logoUri, boolean active,
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