package com.iso.hypo.brand.application.dto;

import java.util.List;

import com.iso.hypo.common.application.dto.BaseEntityDto;
import com.iso.hypo.common.application.dto.contact.ContactDto;
import com.iso.hypo.common.application.dto.contact.PhoneNumberDto;
import com.iso.hypo.common.domain.model.location.Address;

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

    private List<ContactDto> contacts;

    private List<PhoneNumberDto> phoneNumbers;

    private List<CoachDto> coachs;

}
