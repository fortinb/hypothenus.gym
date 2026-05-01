package com.iso.hypo.membership.application.event;

import java.net.URI;

import com.iso.hypo.common.application.event.HypothenusEvent;
import com.iso.hypo.common.application.event.enumeration.OperationEnum;
import com.iso.hypo.membership.application.dto.MemberDto;

public class MemberEvent extends HypothenusEvent<MemberDto> {

    private static final long serialVersionUID = 1L;

    private static final String EVENT_SOURCE = "/hypo/gym/member";

    public MemberEvent(Object source, MemberDto entity, OperationEnum operation) {
        super(source, entity, operation);
    }

    @Override
    protected String resolveId() {
        return getEntity().getUuid();
    }

    @Override
    protected String resolveType() {
        return "hypo.gym.member." + getOperation().name().toLowerCase();
    }

    @Override
    protected URI resolveSource() {
        return URI.create(EVENT_SOURCE);
    }
}