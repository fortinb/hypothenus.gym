package com.iso.hypo.membership.infrastructure.port.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.iso.hypo.brand.application.usecase.BrandQueryService;
import com.iso.hypo.common.application.context.RequestContext;
import com.iso.hypo.membership.application.port.BrandServicePort;

/**
 * Infrastructure adapter that satisfies {@link BrandServicePort} for the
 * membership module. Delegates to {@link BrandQueryService#assertExists} and
 * translates the outcome to a boolean, so membership's application layer never
 * imports any brand application type.
 */
@Component
public class MembershipBrandServicePortAdapter implements BrandServicePort {

    private static final Logger logger = LoggerFactory.getLogger(MembershipBrandServicePortAdapter.class);

    private final BrandQueryService brandQueryService;
    
	@SuppressWarnings("unused")
	private final RequestContext requestContext;
	
    public MembershipBrandServicePortAdapter(
    		BrandQueryService brandQueryService, 
    		RequestContext requestContext) {
        this.brandQueryService = brandQueryService;
		this.requestContext = requestContext;
    }

    @Override
    public boolean brandExists(String brandUuid) {
        try {
            brandQueryService.assertExists(brandUuid);
            return true;
        } catch (Exception e) {
            logger.debug("Brand not found or unavailable - brandUuid={}", brandUuid);
            return false;
        }
    }
}
