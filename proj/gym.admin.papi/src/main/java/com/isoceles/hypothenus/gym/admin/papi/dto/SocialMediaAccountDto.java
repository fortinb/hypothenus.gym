package com.isoceles.hypothenus.gym.admin.papi.dto;

import java.net.URI;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SocialMediaAccountDto {
	
	@NotNull
	private SocialMediaTypeEnum socialMedia;
	
	@NotBlank
	private String accountName;
	
	@NotBlank
	private URI url;
}
