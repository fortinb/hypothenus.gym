package com.iso.hypo.brand.application.event;

import java.net.URI;

import com.iso.hypo.brand.application.dto.CourseDto;
import com.iso.hypo.common.application.event.HypothenusEvent;
import com.iso.hypo.common.application.event.enumeration.OperationEnum;

public class CourseEvent extends HypothenusEvent<CourseDto> {

	private static final long serialVersionUID = 1L;

	private static final String EVENT_SOURCE = "/hypo/brand";

	public CourseEvent(Object source, CourseDto entity, OperationEnum operation) {
		super(source, entity, operation);
	}

	@Override
	protected String resolveId() {
		return getEntity().getUuid();
	}

	@Override
	protected String resolveType() {
		return "hypo.brand.course." + getOperation().name().toLowerCase();
	}

	@Override
	protected URI resolveSource() {
		return URI.create(EVENT_SOURCE);
	}
}