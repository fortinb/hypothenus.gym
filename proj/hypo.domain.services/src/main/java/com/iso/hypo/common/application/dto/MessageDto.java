package com.iso.hypo.common.application.dto;

import com.iso.hypo.common.application.dto.enumeration.MessageSeverityEnumDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageDto {

    private String code;

    private String description;

    private MessageSeverityEnumDto severity;

    public MessageDto() {
    }
}
