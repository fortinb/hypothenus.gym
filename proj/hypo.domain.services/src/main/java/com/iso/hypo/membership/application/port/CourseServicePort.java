package com.iso.hypo.membership.application.port;

import java.util.Optional;

import com.iso.hypo.membership.application.port.dto.CourseRef;

/**
 * Anti-corruption port: abstracts membership's read access to the brand Course
 * aggregate. The infrastructure adapter wires this to
 * {@link com.iso.hypo.brand.domain.repository.CourseRepository}.
 */
public interface CourseServicePort {

    Optional<CourseRef> find(String brandUuid, String courseUuid);
}