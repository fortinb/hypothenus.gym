package com.iso.hypo.brand.application.event;

import com.iso.hypo.brand.domain.model.Brand;
import com.iso.hypo.events.event.HypothenusEvent;
import com.iso.hypo.events.event.OperationEnum;

public class BrandEvent extends HypothenusEvent<Brand> {

	private static final long serialVersionUID = 1L;

	public BrandEvent(Object source, Brand entity, OperationEnum operation) {
		super(source, entity, operation);
	}
}

