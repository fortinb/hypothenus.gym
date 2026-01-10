package com.iso.hypo.gym.admin.papi.dto.patch;

import java.util.Date;
import java.util.List;

import com.iso.hypo.gym.admin.papi.dto.LocalizedStringDto;
import com.iso.hypo.gym.admin.papi.dto.model.CoachDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatchCourseDto {
	
	@NotBlank
	private String id;
	
	@NotBlank
	private String brandId;

	@NotBlank
	private String gymId;

	private String code;

	private List<LocalizedStringDto> name;

	private List<LocalizedStringDto> description;
	
	private Date startDate;
	
	private Date endDate;
	
	private List<CoachDto> coachs;
}