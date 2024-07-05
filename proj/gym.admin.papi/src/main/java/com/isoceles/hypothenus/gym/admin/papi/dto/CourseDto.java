package com.isoceles.hypothenus.gym.admin.papi.dto;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseDto extends BaseDto {
	
	private String id;

	private String gymId;

	private String code;

	private List<LocalizedStringDto> name;

	private List<LocalizedStringDto> description;

	private Instant activatedOn;

	private Instant deactivatedOn;
}