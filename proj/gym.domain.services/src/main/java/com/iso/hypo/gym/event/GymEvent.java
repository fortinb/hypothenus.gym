package com.iso.hypo.gym.event;

import com.iso.hypo.events.event.HypothenusEvent;
import com.iso.hypo.events.event.OperationEnum;
import com.iso.hypo.gym.domain.aggregate.Gym;

public class GymEvent extends HypothenusEvent<Gym> {

	private static final long serialVersionUID = 1L;

	public GymEvent(Object source, Gym entity, OperationEnum operation) {
		super(source, entity, operation);
	}

}
