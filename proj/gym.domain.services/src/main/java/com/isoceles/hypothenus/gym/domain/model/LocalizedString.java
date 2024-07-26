package com.isoceles.hypothenus.gym.domain.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocalizedString {

	private String text;
	
	private LanguageEnum language;
	
	public LocalizedString() {
	}
	
	public LocalizedString(String text, LanguageEnum language) {
		super();
		this.text = text;
		this.language = language;
	}
}
