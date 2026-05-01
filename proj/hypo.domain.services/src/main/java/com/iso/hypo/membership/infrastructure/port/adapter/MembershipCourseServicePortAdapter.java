package com.iso.hypo.membership.infrastructure.port.adapter;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.iso.hypo.brand.application.dto.CourseDto;
import com.iso.hypo.brand.application.exception.CourseException;
import com.iso.hypo.brand.application.usecase.CourseQueryService;
import com.iso.hypo.common.application.context.RequestContext;
import com.iso.hypo.membership.application.port.CourseServicePort;
import com.iso.hypo.membership.application.port.dto.CourseRef;
import com.iso.hypo.membership.infrastructure.port.mapper.CourseRefMapper;

/**
 * Infrastructure adapter that satisfies {@link CourseServicePort} by delegating
 * to {@link CourseQueryService}. Translates the brand-owned {@link CourseDto} into
 * the membership-owned {@link CourseRef} so that the membership application layer
 * has no compile-time dependency on the brand application layer.
 */
@Component
public class MembershipCourseServicePortAdapter implements CourseServicePort {

    private static final Logger logger = LoggerFactory.getLogger(MembershipCourseServicePortAdapter.class);

    private final CourseQueryService courseQueryService;
    private final CourseRefMapper courseRefMapper;
    
	@SuppressWarnings("unused")
	private final RequestContext requestContext;
	
    public MembershipCourseServicePortAdapter(
    		CourseQueryService courseQueryService,
    		CourseRefMapper courseRefMapper, 
    		RequestContext requestContext) {
        this.courseQueryService = courseQueryService;
		this.courseRefMapper = courseRefMapper;
		this.requestContext = requestContext;
    }

    @Override
    public Optional<CourseRef> find(String brandUuid, String courseUuid) {
        try {
            CourseDto dto = courseQueryService.find(brandUuid, courseUuid);
            return Optional.of(courseRefMapper.toRef(dto));
        } catch (CourseException e) {
            logger.debug("Course not found - brandUuid={}, courseUuid={}", brandUuid, courseUuid);
            return Optional.empty();
        }
    }
}