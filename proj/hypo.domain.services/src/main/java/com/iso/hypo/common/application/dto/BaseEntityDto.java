package com.iso.hypo.common.application.dto;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseEntityDto {

    private List<MessageDto> messages = new java.util.ArrayList<MessageDto>();

    private boolean deleted = false;
    private boolean active = true;

    private String createdBy;
    private Instant createdOn;

    private String deletedBy;
    private Instant deletedOn;

    private String modifiedBy;
    private Instant modifiedOn;

    private Instant activatedOn;
    private Instant deactivatedOn;

}
