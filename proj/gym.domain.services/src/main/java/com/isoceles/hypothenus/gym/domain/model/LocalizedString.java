package com.isoceles.hypothenus.gym.domain.model;

import com.isoceles.hypothenus.gym.domain.model.enumeration.LanguageEnum;

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
		this.text = text;
		this.language = language;
	}
}
