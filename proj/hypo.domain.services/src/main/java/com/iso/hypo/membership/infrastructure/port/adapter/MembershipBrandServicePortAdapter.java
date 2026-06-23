package com.iso.hypo.membership.infrastructure.port.adapter;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.iso.hypo.brand.application.dto.BrandDto;
import com.iso.hypo.brand.application.usecase.BrandQueryService;
import com.iso.hypo.common.application.context.RequestContext;
import com.iso.hypo.membership.application.port.BrandServicePort;
import com.iso.hypo.membership.application.port.dto.BrandRef;
import com.iso.hypo.membership.infrastructure.port.mapper.MembershipBrandRefMapper;


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
    
	private final MembershipBrandRefMapper brandRefMapper;
    
	@SuppressWarnings("unused")
	private final RequestContext requestContext;
	
    public MembershipBrandServicePortAdapter(
    		BrandQueryService brandQueryService, 
    		MembershipBrandRefMapper brandRefMapper,
    		RequestContext requestContext) {
        this.brandQueryService = brandQueryService;
        this.brandRefMapper = brandRefMapper;
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
    
	@Override
	public Optional<BrandRef> find(String brandUuid) {
        try {
        	BrandDto dto = brandQueryService.find(brandUuid);
			return Optional.of(brandRefMapper.toRef(dto));
        } catch (Exception e) {
            logger.debug("Brand not found or unavailable - brandUuid={}", brandUuid);
            return Optional.empty();
        }
	}
	
    @Override
    public boolean brandDeleted(String brandUuid) {
        try {
            brandQueryService.assertDeleted(brandUuid);
            return true;
        } catch (Exception e) {
            logger.debug("Brand not found or not deleted - brandUuid={}", brandUuid);
            return false;
        }
    }
    
}
