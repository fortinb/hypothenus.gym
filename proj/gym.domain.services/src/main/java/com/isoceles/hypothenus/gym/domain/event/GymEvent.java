package com.isoceles.hypothenus.gym.domain.event;

import com.isoceles.hypothenus.events.event.HypothenusEvent;
import com.isoceles.hypothenus.events.event.OperationEnum;
import com.isoceles.hypothenus.gym.domain.model.aggregate.Gym;

public class GymEvent extends HypothenusEvent<Gym> {

	private static final long serialVersionUID = 1L;

	public GymEvent(Object source, Gym entity, OperationEnum operation) {
		super(source, entity, operation);
	}

}
