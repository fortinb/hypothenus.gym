package com.iso.hypo.membership.application.port.dto;

import java.util.List;

import com.iso.hypo.common.domain.model.enumeration.RoleEnum;

import lombok.Getter;
import lombok.Setter;

/**
 * Membership-owned reference to a Brand User aggregate.
 * Contains only the fields that the membership bounded context actually needs,
 * so that the membership application layer has no compile-time dependency on
 * {@code com.iso.hypo.brand.application.dto.UserDto}.
 */
@Getter
@Setter
public class UserRef {

    private String uuid;

    private String idpId;

    private String upn;

    private String firstname;

    private String lastname;

    private String email;

    private List<RoleEnum> roles;
}
