package com.iso.hypo.membership.application.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.iso.hypo.brand.application.event.BrandEvent;
import com.iso.hypo.common.domain.exception.DomainException;
import com.iso.hypo.common.application.event.enumeration.OperationEnum;
import com.iso.hypo.membership.application.exception.MembershipException;
import com.iso.hypo.membership.application.usecase.MembershipService;

@Component
public class MembershipListener {

    private final MembershipService membershipService;

	private static final Logger logger = LoggerFactory.getLogger(MembershipListener.class);
	
    public MembershipListener(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @EventListener
    public void onBrandEvent(BrandEvent event) throws DomainException {
		if (event.getOperation() == OperationEnum.delete) {
			handleDeleteBrand(event);
		}
    }
    
    private void handleDeleteBrand(BrandEvent event) throws DomainException {
		try {
			membershipService.deleteAllByBrandUuid(event.getEntity().getUuid());
		} catch (MembershipException e) {
			logger.error("Error - brandId={}", event.getEntity().getUuid(), e);
			throw e;
		}
	}	
}