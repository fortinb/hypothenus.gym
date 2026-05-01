package com.iso.hypo.common.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.Transient;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseDocument {

    @Transient
    protected List<Object> messages = new ArrayList<>();

    protected boolean deleted = false;
    protected boolean active  = true;

    @CreatedBy
    protected String createdBy;
    protected Instant createdOn;

    protected String deletedBy;
    protected Instant deletedOn;

    @LastModifiedBy
    protected String modifiedBy;
    protected Instant modifiedOn;

    protected Instant activatedOn;
    protected Instant deactivatedOn;

    protected BaseDocument() {}

    protected BaseDocument(boolean active) {
        this.active = active;
    }
}
