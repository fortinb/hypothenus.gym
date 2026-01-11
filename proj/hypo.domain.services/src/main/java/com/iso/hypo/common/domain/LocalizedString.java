package com.iso.hypo.common.domain;

import com.iso.hypo.common.domain.enumeration.LanguageEnum;

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
