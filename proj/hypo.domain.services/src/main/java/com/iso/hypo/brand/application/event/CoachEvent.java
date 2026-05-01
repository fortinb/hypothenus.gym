package com.iso.hypo.brand.application.event;

import java.net.URI;

import com.iso.hypo.brand.application.dto.CoachDto;
import com.iso.hypo.common.application.event.HypothenusEvent;
import com.iso.hypo.common.application.event.enumeration.OperationEnum;

public class CoachEvent extends HypothenusEvent<CoachDto> {

	private static final long serialVersionUID = 1L;

	private static final String EVENT_SOURCE = "/hypo/brand";

	public CoachEvent(Object source, CoachDto entity, OperationEnum operation) {
		super(source, entity, operation);
	}

	@Override
	protected String resolveId() {
		return getEntity().getUuid();
	}

	@Override
	protected String resolveType() {
		return "hypo.brand.coach." + getOperation().name().toLowerCase();
	}

	@Override
	protected URI resolveSource() {
		return URI.create(EVENT_SOURCE);
	}
}