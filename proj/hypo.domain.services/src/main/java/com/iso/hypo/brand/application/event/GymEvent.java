package com.iso.hypo.brand.application.event;

import com.iso.hypo.brand.domain.model.Gym;
import com.iso.hypo.events.event.HypothenusEvent;
import com.iso.hypo.events.event.OperationEnum;

public class GymEvent extends HypothenusEvent<Gym> {

	private static final long serialVersionUID = 1L;

	public GymEvent(Object source, Gym entity, OperationEnum operation) {
		super(source, entity, operation);
	}

}

