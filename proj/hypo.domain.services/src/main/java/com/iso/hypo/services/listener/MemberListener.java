package com.iso.hypo.services.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.iso.hypo.common.exception.DomainException;
import com.iso.hypo.events.event.OperationEnum;
import com.iso.hypo.services.MemberService;
import com.iso.hypo.services.event.BrandEvent;
import com.iso.hypo.services.event.UserEvent;
import com.iso.hypo.services.exception.MemberException;

@Component
public class MemberListener {

    private final MemberService memberService;

	private static final Logger logger = LoggerFactory.getLogger(MemberListener.class);
	
    public MemberListener(MemberService memberService) {
        this.memberService = memberService;
    }

    @EventListener
    public void onBrandEvent(BrandEvent event) throws DomainException {
		if (event.getOperation() == OperationEnum.delete) {
			handleDeleteBrand(event);
		}
    }
    
    private void handleDeleteBrand(BrandEvent event) throws DomainException {
		try {
			memberService.deleteAllByBrandUuid(event.getEntity().getUuid());
		} catch (MemberException e) {
			logger.error("Error - brandId={}", event.getEntity().getUuid(), e);
			throw e;
		}
	}	
    
    @EventListener
    public void onUserEvent(UserEvent event) throws DomainException {
		if (event.getOperation() == OperationEnum.delete) {
			handleDeleteUser(event);
		}
    }
    
    private void handleDeleteUser(UserEvent event) throws DomainException {
		/*
		 * try { //memberService.(event.getEntity().getUuid()); } catch (MemberException
		 * e) { logger.error("Error - brandId={}", event.getEntity().getUuid(), e);
		 * throw e; }
		 */
	}	
}