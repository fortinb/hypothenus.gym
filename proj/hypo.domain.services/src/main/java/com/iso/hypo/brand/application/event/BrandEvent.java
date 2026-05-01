package com.iso.hypo.brand.application.event;

import java.net.URI;

import com.iso.hypo.brand.application.dto.BrandDto;
import com.iso.hypo.common.application.event.HypothenusEvent;
import com.iso.hypo.common.application.event.enumeration.OperationEnum;

public class BrandEvent extends HypothenusEvent<BrandDto> {

	private static final long serialVersionUID = 1L;

	private static final String EVENT_SOURCE = "/hypo/brand";

	public BrandEvent(Object source, BrandDto entity, OperationEnum operation) {
		super(source, entity, operation);
	}

	@Override
	protected String resolveId() {
		return getEntity().getUuid();
	}

	@Override
	protected String resolveType() {
		return "hypo.brand.brand." + getOperation().name().toLowerCase();
	}

	@Override
	protected URI resolveSource() {
		return URI.create(EVENT_SOURCE);
	}
}