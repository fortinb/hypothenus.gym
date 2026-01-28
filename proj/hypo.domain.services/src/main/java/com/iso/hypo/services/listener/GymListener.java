package com.iso.hypo.services.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.iso.hypo.common.exception.DomainException;
import com.iso.hypo.events.event.OperationEnum;
import com.iso.hypo.services.GymService;
import com.iso.hypo.services.event.BrandEvent;
import com.iso.hypo.services.exception.GymException;

@Component
public class GymListener {

    private final GymService gymService;

	private static final Logger logger = LoggerFactory.getLogger(GymListener.class);
	
    public GymListener(GymService gymService) {
        this.gymService = gymService;
    }

    @EventListener
    public void onBrandEvent(BrandEvent event) throws DomainException {
		if (event.getOperation() == OperationEnum.delete) {
			handleDeleteBrand(event);
		}
    }
    
    private void handleDeleteBrand(BrandEvent event) throws DomainException {
		try {
			gymService.deleteAllByBrandUuid(event.getEntity().getUuid());
		} catch (GymException e) {
			logger.error("Error - brandId={}", event.getEntity().getUuid(), e);
			throw e;
		}
	}	
}