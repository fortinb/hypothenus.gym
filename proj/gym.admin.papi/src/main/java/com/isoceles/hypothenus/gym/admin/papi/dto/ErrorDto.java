package com.isoceles.hypothenus.gym.admin.papi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorDto {

	private String code;

	private String Description;
	
	private String data;
	
	public ErrorDto(String code, String description, String data) {
		this.code = code;
		this.Description = description;
		this.data = data;
	}
}
