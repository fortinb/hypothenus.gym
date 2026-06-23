package com.iso.hypo.sale.infrastructure.port.adapter;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.iso.hypo.common.application.context.RequestContext;
import com.iso.hypo.finance.application.dto.PaymentDto;
import com.iso.hypo.finance.application.usecase.PaymentService;
import com.iso.hypo.sale.application.port.PaymentServicePort;
import com.iso.hypo.sale.application.port.dto.PaymentRef;
import com.iso.hypo.sale.infrastructure.port.mapper.SalePaymentRefMapper;

@Component
public class SalePaymentServicePortAdapter implements PaymentServicePort {

    private static final Logger logger = LoggerFactory.getLogger(SalePaymentServicePortAdapter.class);

    private final PaymentService paymentService;

	private final SalePaymentRefMapper paymentRefMapper;
	
	@SuppressWarnings("unused")
	private final RequestContext requestContext;
	
    public SalePaymentServicePortAdapter(
    		PaymentService paymentService, 
    		SalePaymentRefMapper paymentRefMapper,
    		RequestContext requestContext) {
        this.paymentService = paymentService;
        this.paymentRefMapper = paymentRefMapper;
		this.requestContext = requestContext;
    }


	@Override
	public Optional<PaymentRef> purchase(PaymentRef paymentRef) {
        try {
        	PaymentDto dto = paymentRefMapper.toDto(paymentRef);
        	
			PaymentDto result = paymentService.purchase(dto);
			return Optional.ofNullable(paymentRefMapper.toRef(result));
        } catch (Exception e) {
            logger.debug("Payment Purchase Error - brandUuid={}, orderNumber={} ", paymentRef.getBrandUuid(), paymentRef.getOrderNumber());
            return Optional.empty();
        }
	}
}
