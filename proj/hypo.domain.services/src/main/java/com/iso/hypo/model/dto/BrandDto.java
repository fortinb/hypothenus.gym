package com.iso.hypo.model.dto;

import java.util.List;

import com.iso.hypo.model.domain.contact.Contact;
import com.iso.hypo.model.domain.contact.PhoneNumber;
import com.iso.hypo.model.domain.location.Address;
import com.iso.hypo.common.dto.BaseEntityDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BrandDto extends BaseEntityDto {

    private String uuid;

    private String code;

    private String name;

    private Address address;

    private String email;

    private String note;

    private String logoUri;

    private List<Contact> contacts;

    private List<PhoneNumber> phoneNumbers;

}