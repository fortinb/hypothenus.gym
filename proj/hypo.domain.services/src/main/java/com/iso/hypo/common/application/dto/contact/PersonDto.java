package com.iso.hypo.common.application.dto.contact;

import java.util.Date;
import java.util.List;

import com.iso.hypo.common.application.dto.enumeration.LanguageEnumDto;
import com.iso.hypo.common.application.dto.location.AddressDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonDto {

    private String firstname;

    private String lastname;

    private Date dateOfBirth;

    private String email;

    private AddressDto address;

    private List<PhoneNumberDto> phoneNumbers;

    private List<ContactDto> contacts;

    private String photoUri;

    private LanguageEnumDto communicationLanguage;

    private String note;

    public PersonDto() {
    }
}
