package com.isoceles.hypothenus.gym.admin.papi.dto;

import java.net.URL;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SocialMediaAccount {

	private SocialMediaTypeEnum socialMedia;
	private String accountName;
	private URL url;
}
