package com.iso.hypo.sale.application.port;

import java.util.Optional;

import com.iso.hypo.sale.application.port.dto.BrandRef;

/**
 * Anti-corruption port: allows finance use cases to verify brand existence
 * without importing any type from the brand application layer.
 * The infrastructure adapter delegates to
 * {@link com.iso.hypo.brand.application.usecase.BrandQueryService}.
 */
public interface BrandServicePort {

    /**
     * Returns {@code true} if the brand identified by {@code brandUuid} exists
     * and has not been deleted; {@code false} otherwise.
     */
    boolean brandExists(String brandUuid);
    
    boolean brandDeleted(String brandUuid);
    
    Optional<BrandRef> find(String brandUuid);
}
