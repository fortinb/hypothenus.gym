package com.iso.hypo.brand.application.dto;

import java.util.List;

import com.iso.hypo.common.application.dto.BaseEntityDto;
import com.iso.hypo.common.application.dto.contact.ContactDto;
import com.iso.hypo.common.application.dto.contact.PhoneNumberDto;
import com.iso.hypo.common.application.dto.location.AddressDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BrandDto extends BaseEntityDto {

    private String uuid;

    private String code;

    private String name;

    private AddressDto address;

    private String email;

    private String note;

    private String logoUri;

    private List<ContactDto> contacts;

    private List<PhoneNumberDto> phoneNumbers;

}
