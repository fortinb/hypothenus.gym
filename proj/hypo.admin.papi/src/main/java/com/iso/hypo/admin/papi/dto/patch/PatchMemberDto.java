package com.iso.hypo.admin.papi.dto.patch;

import com.iso.hypo.admin.papi.dto.BaseDto;
import com.iso.hypo.admin.papi.dto.contact.PersonDto;
import com.iso.hypo.domain.enumeration.MemberTypeEnum;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatchMemberDto extends BaseDto {

	@NotBlank
	private String brandUuid;
	
	@NotBlank
    private String uuid;

	private PersonDto person;
	
	private MemberTypeEnum memberType;
	
	private String preferredGymUuid;
}
