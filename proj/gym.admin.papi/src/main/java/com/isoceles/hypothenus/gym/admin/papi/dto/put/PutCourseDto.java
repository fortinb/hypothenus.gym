package com.isoceles.hypothenus.gym.admin.papi.dto.put;

import java.util.Date;
import java.util.List;

import com.isoceles.hypothenus.gym.admin.papi.dto.CoachDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.LocalizedStringDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PutCourseDto {
	
	private String id;

	private String gymId;

	private String code;
	
	private boolean isActive;

	private List<LocalizedStringDto> name;

	private List<LocalizedStringDto> description;
	
	private Date startDate;
	
	private Date endDate;
	
	private List<CoachDto> coachs;
}