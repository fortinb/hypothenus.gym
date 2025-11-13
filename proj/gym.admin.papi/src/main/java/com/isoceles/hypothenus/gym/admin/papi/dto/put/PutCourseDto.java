package com.isoceles.hypothenus.gym.admin.papi.dto.put;

import java.util.Date;
import java.util.List;

import com.isoceles.hypothenus.gym.admin.papi.dto.CoachDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.LocalizedStringDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PutCourseDto {
	
	@NotBlank
	private String id;
	
	@NotBlank
	private String brandId;
	
	@NotBlank
	private String gymId;

	private String code;
	
	private boolean isActive;

	private List<LocalizedStringDto> name;

	private List<LocalizedStringDto> description;
	
	private Date startDate;
	
	private Date endDate;
	
	private List<CoachDto> coachs;
}