package com.iso.hypo.membership.application.event;

import com.iso.hypo.events.event.HypothenusEvent;
import com.iso.hypo.events.event.OperationEnum;
import com.iso.hypo.membership.domain.model.Member;

public class MemberEvent extends HypothenusEvent<Member> {

    private static final long serialVersionUID = 1L;

    public MemberEvent(Object source, Member entity, OperationEnum operation) {
        super(source, entity, operation);
    }

}
