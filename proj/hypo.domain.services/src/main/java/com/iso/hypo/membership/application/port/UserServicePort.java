package com.iso.hypo.membership.application.port;

import java.util.Optional;

import com.iso.hypo.membership.application.port.dto.UserRef;

/**
 * Anti-corruption port: abstracts membership's read/write access to the brand
 * User aggregate. The infrastructure adapter wires this to
 * {@link com.iso.hypo.brand.domain.repository.UserRepository}.
 */
public interface UserServicePort {

	Optional<UserRef> findByUuid(String userUuid);
	 
    Optional<UserRef> findByEmail(String email);

    Optional<UserRef> findByIdpId(String idpId);

    UserRef create(UserRef userRef, String password, String initialGroupName);
    
    UserRef patch(UserRef userRef);
}