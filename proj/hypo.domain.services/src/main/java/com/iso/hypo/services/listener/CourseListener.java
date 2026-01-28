package com.iso.hypo.services.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.iso.hypo.common.exception.DomainException;
import com.iso.hypo.events.event.OperationEnum;
import com.iso.hypo.services.CourseService;
import com.iso.hypo.services.event.BrandEvent;
import com.iso.hypo.services.event.GymEvent;
import com.iso.hypo.services.exception.CourseException;

@Component
public class CourseListener {

    private final CourseService courseService;

	private static final Logger logger = LoggerFactory.getLogger(CourseListener.class);
	
    public CourseListener(CourseService courseService) {
        this.courseService = courseService;
    }

    @EventListener
    public void onBrandEvent(BrandEvent event) throws DomainException {
		if (event.getOperation() == OperationEnum.delete) {
			handleDeleteBrand(event);
		}
    }
    
    private void handleDeleteBrand(BrandEvent event) throws DomainException {
		try {
			courseService.deleteAllByBrandUuid(event.getEntity().getUuid());
		} catch (CourseException e) {
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
			courseService.deleteAllByGymUuid(event.getEntity().getBrandUuid(), event.getEntity().getUuid());
		} catch (CourseException e) {
			logger.error("Error - brandId={} gymId={}", event.getEntity().getBrandUuid(), event.getEntity().getUuid(), e);
			throw e;
		}
	}
}