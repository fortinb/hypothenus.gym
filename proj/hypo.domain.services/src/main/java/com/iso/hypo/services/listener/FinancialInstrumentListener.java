package com.iso.hypo.services.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.iso.hypo.common.exception.DomainException;
import com.iso.hypo.events.event.OperationEnum;
import com.iso.hypo.services.FinancialInstrumentService;
import com.iso.hypo.services.event.BrandEvent;
import com.iso.hypo.services.event.MemberEvent;
import com.iso.hypo.services.exception.FinancialInstrumentException;

@Component
public class FinancialInstrumentListener {

    private final FinancialInstrumentService financialInstrumentService;

	private static final Logger logger = LoggerFactory.getLogger(FinancialInstrumentListener.class);
	
    public FinancialInstrumentListener(FinancialInstrumentService FinancialInstrumentService) {
        this.financialInstrumentService = FinancialInstrumentService;
    }

    @EventListener
    public void onBrandEvent(BrandEvent event) throws DomainException {
		if (event.getOperation() == OperationEnum.delete) {
			handleDeleteBrand(event);
		}
    }
    
    private void handleDeleteBrand(BrandEvent event) throws DomainException {
		try {
			financialInstrumentService.deleteAllByBrandUuid(event.getEntity().getUuid());
		} catch (FinancialInstrumentException e) {
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
			financialInstrumentService.deleteAllByMemberUuid(event.getEntity().getBrandUuid(), event.getEntity().getUuid());
		} catch (FinancialInstrumentException e) {
			logger.error("Error - brandId={} memberId={}", event.getEntity().getBrandUuid(), event.getEntity().getUuid(), e);
			throw e;
		}
	}	
}