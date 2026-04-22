package com.iso.hypo.brand.application.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.iso.hypo.brand.application.event.BrandEvent;
import com.iso.hypo.brand.application.event.CoachEvent;
import com.iso.hypo.brand.application.usecase.GymService;
import com.iso.hypo.brand.domain.exception.GymException;
import com.iso.hypo.common.domain.exception.DomainException;
import com.iso.hypo.events.event.OperationEnum;

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
    
    @EventListener
    public void onCoachEvent(CoachEvent event) throws DomainException {
		if (event.getOperation() == OperationEnum.delete) {
			handleDeleteCoach(event);
		}
    }
    
    private void handleDeleteCoach(CoachEvent event) throws DomainException {
    	try {
    		gymService.removeAllCoachReferencesByCoachId(event.getEntity().getId());
		} catch (GymException e) {
			logger.error("Error - coachUuid={}", event.getEntity().getUuid(), e);
			throw e;
		}
	}
}