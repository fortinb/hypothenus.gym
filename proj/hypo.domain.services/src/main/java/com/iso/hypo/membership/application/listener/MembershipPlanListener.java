package com.iso.hypo.membership.application.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.iso.hypo.brand.application.event.BrandEvent;
import com.iso.hypo.brand.application.event.CourseEvent;
import com.iso.hypo.brand.application.event.GymEvent;
import com.iso.hypo.common.application.event.enumeration.OperationEnum;
import com.iso.hypo.common.domain.exception.DomainException;
import com.iso.hypo.membership.application.exception.MembershipPlanException;
import com.iso.hypo.membership.application.usecase.MembershipPlanService;

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
			membershipPlanService.removeAllGymReferencesByGymUuid(event.getEntity().getBrandUuid(), event.getEntity().getUuid());
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
    		membershipPlanService.removeAllCourseReferencesByCourseUuid(event.getEntity().getBrandUuid(), event.getEntity().getUuid());
		} catch (MembershipPlanException e) {
			logger.error("Error - courseUuid={}", event.getEntity().getUuid(), e);
			throw e;
		}
	}
}