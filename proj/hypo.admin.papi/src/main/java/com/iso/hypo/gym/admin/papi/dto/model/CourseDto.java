package com.iso.hypo.gym.admin.papi.dto.model;

import java.util.Date;
import java.util.List;

import com.iso.hypo.gym.admin.papi.dto.BaseDto;
import com.iso.hypo.gym.admin.papi.dto.LocalizedStringDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseDto extends BaseDto {
	
	private String uuid;
	
	private String brandId;

	private String gymId;

	private String code;

	private List<LocalizedStringDto> name;

	private List<LocalizedStringDto> description;
	
	private Date startDate;
	
	private Date endDate;
	
	private List<CoachDto> coachs;
}