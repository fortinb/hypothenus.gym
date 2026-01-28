package com.iso.hypo.services.event;

import com.iso.hypo.domain.aggregate.Brand;
import com.iso.hypo.events.event.HypothenusEvent;
import com.iso.hypo.events.event.OperationEnum;

public class BrandEvent extends HypothenusEvent<Brand> {

	private static final long serialVersionUID = 1L;

	public BrandEvent(Object source, Brand entity, OperationEnum operation) {
		super(source, entity, operation);
	}
}

