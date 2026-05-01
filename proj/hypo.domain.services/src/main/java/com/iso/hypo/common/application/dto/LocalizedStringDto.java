package com.iso.hypo.common.application.dto;

import com.iso.hypo.common.application.dto.enumeration.LanguageEnumDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocalizedStringDto {

    private String text;

    private LanguageEnumDto language;

    public LocalizedStringDto() {
    }
}
