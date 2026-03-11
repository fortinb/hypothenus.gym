package com.iso.hypo.admin.papi.dto.patch;

import java.util.Date;
import java.util.List;

import com.iso.hypo.admin.papi.dto.LocalizedStringDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatchCourseDto {
	
	@NotBlank
	private String brandUuid;

	@NotBlank
	private String uuid;
	
	private String code;

	private List<LocalizedStringDto> name;

	private List<LocalizedStringDto> description;
	
	private Date startDate;
	
	private Date endDate;
}