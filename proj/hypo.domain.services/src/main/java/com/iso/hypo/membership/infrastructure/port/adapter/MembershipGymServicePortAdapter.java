package com.iso.hypo.membership.infrastructure.port.adapter;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.iso.hypo.brand.application.dto.GymDto;
import com.iso.hypo.brand.application.exception.GymException;
import com.iso.hypo.brand.application.usecase.GymQueryService;
import com.iso.hypo.common.application.context.RequestContext;
import com.iso.hypo.membership.application.port.GymServicePort;
import com.iso.hypo.membership.application.port.dto.GymRef;
import com.iso.hypo.membership.infrastructure.port.mapper.GymRefMapper;

/**
 * Infrastructure adapter that satisfies {@link GymServicePort} by delegating
 * to {@link GymQueryService}. Translates the brand-owned {@link GymDto} into
 * the membership-owned {@link GymRef} so that the membership application layer
 * has no compile-time dependency on the brand application layer.
 */
@Component
public class MembershipGymServicePortAdapter implements GymServicePort {

    private static final Logger logger = LoggerFactory.getLogger(MembershipGymServicePortAdapter.class);

    private final GymQueryService gymQueryService;
    private final GymRefMapper gymRefMapper;

	@SuppressWarnings("unused")
	private final RequestContext requestContext;
	
    public MembershipGymServicePortAdapter(
    		GymQueryService gymQueryService, 
    		GymRefMapper gymRefMapper, 
    		RequestContext requestContext) {
        this.gymQueryService = gymQueryService;
		this.gymRefMapper = gymRefMapper;
		this.requestContext = requestContext;
    }

    @Override
    public Optional<GymRef> find(String brandUuid, String gymUuid) {
        try {
            GymDto dto = gymQueryService.find(brandUuid, gymUuid);
            return Optional.of(gymRefMapper.toRef(dto));
        } catch (GymException e) {
            logger.debug("Gym not found - brandUuid={}, gymUuid={}", brandUuid, gymUuid);
            return Optional.empty();
        }
    }
}