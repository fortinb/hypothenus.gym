package com.iso.hypo.admin.papi.dto.search;

import java.util.List;

import com.iso.hypo.admin.papi.dto.contact.PhoneNumberDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberSearchDto {

    private String brandUuid;

    private String uuid;

    private String firstname;
    
    private String lastname;
    
    private String email;
    
    private String zipcode;

    private List<PhoneNumberDto> phoneNumbers;
    
    private boolean isActive;
}