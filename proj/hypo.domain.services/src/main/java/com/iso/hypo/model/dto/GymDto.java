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
public class GymDto extends BaseEntityDto {

    private String brandUuid;
    
    private String uuid;

    private String code;

    private String name;

    private Address address;

    private String email;

    private String logoUri;

    private String note;

    private List<Contact> contacts;

    private List<PhoneNumber> phoneNumbers;

}