package com.iso.hypo.membership.application.port;

import java.util.Optional;

import com.iso.hypo.membership.application.port.dto.GymRef;


/**
 * Anti-corruption port: abstracts membership's read access to the brand Gym
 * aggregate. The infrastructure adapter wires this to
 * {@link com.iso.hypo.brand.domain.repository.GymRepository}.
 */
public interface GymServicePort {

    Optional<GymRef> find(String brandUuid, String gymUuid);
}