package com.iso.hypo.admin.papi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorDto {

	private String code;

	private String description;
	
	private String data;
	
	private String trackingNumber;
	
	public ErrorDto(String trackingNumber, String code, String description, String data) {
		this.trackingNumber = trackingNumber;
		this.code = code;
		this.description = description;
		this.data = data;
	}
}