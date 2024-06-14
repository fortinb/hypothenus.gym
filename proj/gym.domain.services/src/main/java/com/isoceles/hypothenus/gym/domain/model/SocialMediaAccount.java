package com.isoceles.hypothenus.gym.domain.model;

import java.net.URI;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SocialMediaAccount {
	
	private SocialMediaTypeEnum socialMedia;
	
	private String accountName;
	
	private URI url;

	public SocialMediaAccount() {
	}
	
	public SocialMediaAccount(SocialMediaTypeEnum socialMedia, String accountName, URI url) {
		super();
		this.socialMedia = socialMedia;
		this.accountName = accountName;
		this.url = url;
	}
}
