package com.iso.hypo.services.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.iso.hypo.common.exception.DomainException;
import com.iso.hypo.events.event.OperationEnum;
import com.iso.hypo.services.MembershipPlanService;
import com.iso.hypo.services.event.BrandEvent;
import com.iso.hypo.services.event.CourseEvent;
import com.iso.hypo.services.event.GymEvent;
import com.iso.hypo.services.exception.MembershipPlanException;

@Component
public class MembershipPlanListener {

    private final MembershipPlanService membershipPlanService;

	private static final Logger logger = LoggerFactory.getLogger(MembershipPlanListener.class);
	
    public MembershipPlanListener(MembershipPlanService membershipPlanService) {
        this.membershipPlanService = membershipPlanService;
    }

    @EventListener
    public void onBrandEvent(BrandEvent event) throws DomainException {
		if (event.getOperation() == OperationEnum.delete) {
			handleDeleteBrand(event);
		}
    }
    
    private void handleDeleteBrand(BrandEvent event) throws DomainException {
		try {
			membershipPlanService.deleteAllByBrandUuid(event.getEntity().getUuid());
		} catch (MembershipPlanException e) {
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
			membershipPlanService.removeAllGymReferencesByGymId(event.getEntity().getId());
		} catch (MembershipPlanException e) {
			logger.error("Error - brandUuid={}", event.getEntity().getUuid(), e);
			throw e;
		}
	}
    
    @EventListener
    public void onCourseEvent(CourseEvent event) throws DomainException {
		if (event.getOperation() == OperationEnum.delete) {
			handleDeleteCourse(event);
		}
    }
    
    private void handleDeleteCourse(CourseEvent event) throws DomainException {
    	try {
    		membershipPlanService.removeAllCourseReferencesByCourseId(event.getEntity().getId());
		} catch (MembershipPlanException e) {
			logger.error("Error - courseUuid={}", event.getEntity().getUuid(), e);
			throw e;
		}
	}
}