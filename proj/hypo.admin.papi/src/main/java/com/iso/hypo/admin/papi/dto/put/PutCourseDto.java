package com.iso.hypo.admin.papi.dto.put;

import java.util.Date;
import java.util.List;

import com.iso.hypo.admin.papi.dto.LocalizedStringDto;
import com.iso.hypo.admin.papi.dto.model.CoachDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PutCourseDto {
	
	@NotBlank
	private String brandUuid;
	
	@NotBlank
	private String gymUuid;
	
	@NotBlank
	private String uuid;
	
	private String code;
	
	private boolean isActive;

	private List<LocalizedStringDto> name;

	private List<LocalizedStringDto> description;
	
	private Date startDate;
	
	private Date endDate;
	
	private List<CoachDto> coachs;
}
