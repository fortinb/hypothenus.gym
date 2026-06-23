package com.iso.hypo.sale.application.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.iso.hypo.brand.application.event.BrandEvent;
import com.iso.hypo.common.application.event.enumeration.OperationEnum;
import com.iso.hypo.common.domain.exception.DomainException;
import com.iso.hypo.membership.application.event.MemberEvent;
import com.iso.hypo.sale.application.exception.OrderException;
import com.iso.hypo.sale.application.usecase.OrderService;

@Component
public class OrderListener {

	private final OrderService orderService;

	private static final Logger logger = LoggerFactory.getLogger(OrderListener.class);

	public OrderListener(OrderService orderService) {
		this.orderService = orderService;
	}

	@EventListener
	public void onBrandEvent(BrandEvent event) throws DomainException {
		if (event.getOperation() == OperationEnum.delete) {
			handleDeleteBrand(event);
		}
	}

	private void handleDeleteBrand(BrandEvent event) throws DomainException {
		try {
			orderService.deleteAllByBrandUuid(event.getEntity().getUuid());
		} catch (OrderException e) {
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
			orderService.deleteAllByMemberUuid(event.getEntity().getBrandUuid(), event.getEntity().getUuid());
		} catch (OrderException e) {
			logger.error("Error - brandId={} memberId={}", event.getEntity().getBrandUuid(), event.getEntity().getUuid(), e);
			throw e;
		}
	}
}
