package com.iso.hypo.finance.application.usecase.impl;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iso.hypo.common.application.context.RequestContext;
import com.iso.hypo.common.application.dto.PageResultDto;
import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;
import com.iso.hypo.finance.application.dto.FinancialInstrumentDto;
import com.iso.hypo.finance.application.exception.FinancialInstrumentException;
import com.iso.hypo.finance.application.mapper.FinancialInstrumentDtoMapper;
import com.iso.hypo.finance.application.usecase.FinancialInstrumentQueryService;
import com.iso.hypo.finance.domain.repository.FinancialInstrumentRepository;

@Service
public class FinancialInstrumentQueryServiceImpl implements FinancialInstrumentQueryService {

    private final FinancialInstrumentRepository financialInstrumentRepository;

    private final FinancialInstrumentDtoMapper financialInstrumentMapper;

    private static final Logger logger = LoggerFactory.getLogger(FinancialInstrumentQueryServiceImpl.class);

    private final RequestContext requestContext;

    public FinancialInstrumentQueryServiceImpl(
    		FinancialInstrumentDtoMapper financialInstrumentMapper, 
    		FinancialInstrumentRepository financialInstrumentRepository, 
    		RequestContext requestContext) {
        this.financialInstrumentMapper = financialInstrumentMapper;
        this.financialInstrumentRepository = financialInstrumentRepository;
        this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
    }

    @Override
    public PageResultDto<FinancialInstrumentDto> list(String brandUuid, String memberUuid, int page, int pageSize, boolean includeInactive) throws FinancialInstrumentException {
        try {
			PageRequest pageRequest = PageRequest.of(page, pageSize);
			PageResult<FinancialInstrumentDto> result = includeInactive
					? financialInstrumentRepository.findAllByBrandUuidAndMemberUuidAndDeletedIsFalse(brandUuid, memberUuid, pageRequest)
							.map(financialInstrumentMapper::toDto)
					: financialInstrumentRepository.findAllByBrandUuidAndMemberUuidAndDeletedIsFalseAndActiveIsTrue(brandUuid, memberUuid, pageRequest)
							.map(financialInstrumentMapper::toDto);
			return PageResultDto.from(result);
        } catch (Exception e) {
            logger.error("Error - brandUuid={}", brandUuid, e);
            throw new FinancialInstrumentException(requestContext.getTrackingNumber(), FinancialInstrumentException.FIND_FAILED, e);
        }
    }
}
