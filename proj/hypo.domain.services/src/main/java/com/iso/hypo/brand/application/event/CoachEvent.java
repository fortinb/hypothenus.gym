package com.iso.hypo.brand.application.event;

import com.iso.hypo.brand.domain.model.Coach;
import com.iso.hypo.events.event.HypothenusEvent;
import com.iso.hypo.events.event.OperationEnum;

public class CoachEvent extends HypothenusEvent<Coach> {

	private static final long serialVersionUID = 1L;

	public CoachEvent(Object source, Coach entity, OperationEnum operation) {
		super(source, entity, operation);
	}

}

