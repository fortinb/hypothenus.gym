package com.iso.hypo.finance.application.usecase.impl;

import java.util.Date;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iso.hypo.common.application.context.RequestContext;
import com.iso.hypo.common.application.dto.PageResultDto;
import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;
import com.iso.hypo.finance.application.dto.PaymentDto;
import com.iso.hypo.finance.application.exception.PaymentException;
import com.iso.hypo.finance.application.mapper.PaymentDtoMapper;
import com.iso.hypo.finance.application.usecase.PaymentQueryService;
import com.iso.hypo.finance.domain.repository.PaymentRepository;

@Service
public class PaymentQueryServiceImpl implements PaymentQueryService {

    private final PaymentRepository paymentRepository;

    private final PaymentDtoMapper paymentMapper;

    private static final Logger logger = LoggerFactory.getLogger(PaymentQueryServiceImpl.class);

    private final RequestContext requestContext;

    public PaymentQueryServiceImpl(
    		PaymentDtoMapper paymentMapper, 
    		PaymentRepository paymentRepository, 
    		RequestContext requestContext) {
        this.paymentMapper = paymentMapper;
        this.paymentRepository = paymentRepository;
        this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
    }

    @Override
    public PageResultDto<PaymentDto> list(String brandUuid, String memberUuid, Date startDate, Date endDate, int page, int pageSize) throws PaymentException {
        try {
        	PageResult<PaymentDto> result;
			PageRequest pageRequest = PageRequest.of(page, pageSize);

			result = paymentRepository.findByDates(brandUuid, startDate, endDate, pageRequest)
					.map(paymentMapper::toDto);
			
			return PageResultDto.from(result);
        } catch (Exception e) {
            logger.error("Error - brandUuid={}", brandUuid, e);
            throw new PaymentException(requestContext.getTrackingNumber(), PaymentException.FIND_FAILED, e);
        }
    }
}
