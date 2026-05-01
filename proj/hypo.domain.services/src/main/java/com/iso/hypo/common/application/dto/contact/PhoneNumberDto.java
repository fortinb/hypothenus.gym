package com.iso.hypo.common.application.dto.contact;

import com.iso.hypo.common.application.dto.enumeration.PhoneNumberTypeEnumDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PhoneNumberDto {

    private String number;

    private PhoneNumberTypeEnumDto type;

    public PhoneNumberDto() {
    }
}
