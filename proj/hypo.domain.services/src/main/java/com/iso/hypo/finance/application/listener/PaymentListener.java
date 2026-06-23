package com.iso.hypo.finance.application.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.iso.hypo.brand.application.event.BrandEvent;
import com.iso.hypo.common.application.event.enumeration.OperationEnum;
import com.iso.hypo.common.domain.exception.DomainException;
import com.iso.hypo.finance.application.exception.PaymentException;
import com.iso.hypo.finance.application.usecase.PaymentService;
import com.iso.hypo.membership.application.event.MemberEvent;

@Component
public class PaymentListener {

    private final PaymentService paymentService;

	private static final Logger logger = LoggerFactory.getLogger(PaymentListener.class);
	
    public PaymentListener(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @EventListener
    public void onBrandEvent(BrandEvent event) throws DomainException {
		if (event.getOperation() == OperationEnum.delete) {
			handleDeleteBrand(event);
		}
    }
    
    private void handleDeleteBrand(BrandEvent event) throws DomainException {
		try {
			paymentService.deleteAllByBrandUuid(event.getEntity().getUuid());
		} catch (PaymentException e) {
			logger.error("Error - brandId={}", event.getEntity().getUuid(), e);
			throw e;
		}
	}	
    
    @EventListener
    public void onMemberEvent(MemberEvent event) throws DomainException {
		if (event.getOperation() == OperationEnum.delete) {
			handleDeleteMember(event);
		}
    }
    
    private void handleDeleteMember(MemberEvent event) throws DomainException {
		try {
			paymentService.deleteAllByMemberUuid(event.getEntity().getBrandUuid(), event.getEntity().getUuid());
		} catch (PaymentException e) {
			logger.error("Error - brandId={} memberId={}", event.getEntity().getBrandUuid(), event.getEntity().getUuid(), e);
			throw e;
		}
	}	
}