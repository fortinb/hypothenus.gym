package com.iso.hypo.services.event;

import com.iso.hypo.domain.aggregate.User;
import com.iso.hypo.events.event.HypothenusEvent;
import com.iso.hypo.events.event.OperationEnum;

public class UserEvent extends HypothenusEvent<User> {

    private static final long serialVersionUID = 1L;

    public UserEvent(Object source, User entity, OperationEnum operation) {
        super(source, entity, operation);
    }

}
