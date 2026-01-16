package com.iso.hypo.gym.dto;

import java.util.List;

import com.iso.hypo.common.domain.contact.Contact;
import com.iso.hypo.common.domain.contact.PhoneNumber;
import com.iso.hypo.common.domain.location.Address;
import com.iso.hypo.common.dto.BaseEntityDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GymDto extends BaseEntityDto {

    private String uuid;

    private String brandId;

    private String gymId;

    private String name;

    private Address address;

    private String email;

    private String logoUri;

    private String note;

    private List<Contact> contacts;

    private List<PhoneNumber> phoneNumbers;

}