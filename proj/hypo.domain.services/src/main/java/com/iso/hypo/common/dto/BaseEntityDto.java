package com.iso.hypo.common.dto;

import java.time.Instant;
import java.util.List;

import com.iso.hypo.domain.Message;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseEntityDto {

    private List<Message> messages;

    private boolean isDeleted = false;
    private boolean isActive = true;

    private String createdBy;
    private Instant createdOn;

    private String deletedBy;
    private Instant deletedOn;

    private String modifiedBy;
    private Instant modifiedOn;

    private Instant activatedOn;
    private Instant deactivatedOn;

}
