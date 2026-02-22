package com.iso.hypo.common.context;

import java.util.List;

import com.iso.hypo.domain.security.RoleEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestContext {

    private String username;

    private String trackingNumber;

    private String brandUuid;

    private List<RoleEnum> roles;

    public RequestContext() {
        // default constructor for non-web contexts (e.g., unit tests)
    }

    public RequestContext(String username, String trackingNumber, String brandUuid, List<RoleEnum> roles) {
        super();
        this.username = username;
        this.trackingNumber = trackingNumber;
        this.brandUuid = brandUuid;
        this.roles = roles;
    }
}