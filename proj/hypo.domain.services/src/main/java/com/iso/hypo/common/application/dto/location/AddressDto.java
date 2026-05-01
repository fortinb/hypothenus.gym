package com.iso.hypo.common.application.dto.location;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressDto {

    private String civicNumber;

    private String streetName;

    private String appartment;

    private String city;

    private String state;

    private String zipCode;

    public AddressDto() {
    }
}
