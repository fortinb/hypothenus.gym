package com.iso.hypo.admin.papi.dto.post;

import com.iso.hypo.admin.papi.dto.BaseDto;
import com.iso.hypo.admin.papi.dto.contact.PersonDto;
import com.iso.hypo.domain.enumeration.MemberTypeEnum;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostMemberDto extends BaseDto {

	@NotBlank
	private String brandUuid;
	
	private PersonDto person;
	
	private String password;
	
	private MemberTypeEnum memberType;
	
	private String preferredGymUuid;
}
