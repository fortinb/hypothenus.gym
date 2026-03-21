package com.iso.hypo.services.event;

import com.iso.hypo.domain.aggregate.Course;
import com.iso.hypo.events.event.HypothenusEvent;
import com.iso.hypo.events.event.OperationEnum;

public class CourseEvent extends HypothenusEvent<Course> {

	private static final long serialVersionUID = 1L;

	public CourseEvent(Object source, Course entity, OperationEnum operation) {
		super(source, entity, operation);
	}
}

