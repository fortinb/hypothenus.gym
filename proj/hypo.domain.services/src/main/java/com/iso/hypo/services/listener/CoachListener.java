package com.iso.hypo.services.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.iso.hypo.common.exception.DomainException;
import com.iso.hypo.events.event.OperationEnum;
import com.iso.hypo.services.CoachService;
import com.iso.hypo.services.event.BrandEvent;
import com.iso.hypo.services.event.GymEvent;
import com.iso.hypo.services.exception.CoachException;

@Component
public class CoachListener {

    private final CoachService coachService;

	private static final Logger logger = LoggerFactory.getLogger(CoachListener.class);
	
    public CoachListener(CoachService coachService) {
        this.coachService = coachService;
    }

    @EventListener
    public void onBrandEvent(BrandEvent event) throws DomainException {
		if (event.getOperation() == OperationEnum.delete) {
			handleDeleteBrand(event);
		}
    }
    
    private void handleDeleteBrand(BrandEvent event) throws DomainException {
		try {
			coachService.deleteAllByBrandUuid(event.getEntity().getUuid());
		} catch (CoachException e) {
			logger.error("Error - brandId={}", event.getEntity().getUuid(), e);
			throw e;
		}
	}	
    
    @EventListener
    public void onGymEvent(GymEvent event) throws DomainException {
		if (event.getOperation() == OperationEnum.delete) {
			handleDeleteGym(event);
		}
    }
    
    private void handleDeleteGym(GymEvent event) throws DomainException {
		try {
			coachService.deleteAllByGymUuid(event.getEntity().getBrandUuid(), event.getEntity().getUuid());
		} catch (CoachException e) {
			logger.error("Error - brandId={} gymId={}", event.getEntity().getBrandUuid(), event.getEntity().getUuid(), e);
			throw e;
		}
	}
}