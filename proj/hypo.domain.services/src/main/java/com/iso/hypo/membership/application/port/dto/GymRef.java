package com.iso.hypo.membership.application.port.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Membership-owned read-only reference to a Brand Gym aggregate.
 * Contains only the fields that the membership bounded context actually needs,
 * so that the membership application layer has no compile-time dependency on
 * {@code com.iso.hypo.brand.application.dto.GymDto}.
 */
@Getter
@Setter
public class GymRef {

    private String uuid;

    private String brandUuid;

    private String code;

    private String name;
}
