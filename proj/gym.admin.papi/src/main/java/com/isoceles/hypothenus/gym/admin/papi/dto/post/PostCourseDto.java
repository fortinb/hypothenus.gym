package com.isoceles.hypothenus.gym.admin.papi.dto.post;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.isoceles.hypothenus.gym.admin.papi.dto.LocalizedStringDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.model.CoachDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostCourseDto {
	
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