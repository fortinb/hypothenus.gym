package com.iso.hypo.membership.application.port;

/**
 * Anti-corruption port: allows membership use cases to verify brand existence
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
}
