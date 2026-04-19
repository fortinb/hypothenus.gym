package com.iso.hypo.services.impl;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.iso.hypo.common.context.RequestContext;
import com.iso.hypo.domain.dto.FinancialInstrumentDto;
import com.iso.hypo.repositories.FinancialInstrumentRepository;
import com.iso.hypo.services.FinancialInstrumentQueryService;
import com.iso.hypo.services.exception.FinancialInstrumentException;
import com.iso.hypo.services.mappers.FinancialInstrumentMapper;

@Service
public class FinancialInstrumentQueryServiceImpl implements FinancialInstrumentQueryService {

    private final FinancialInstrumentRepository financialInstrumentRepository;

    private final FinancialInstrumentMapper financialInstrumentMapper;

    private static final Logger logger = LoggerFactory.getLogger(FinancialInstrumentQueryServiceImpl.class);

    private final RequestContext requestContext;

    public FinancialInstrumentQueryServiceImpl(
    		FinancialInstrumentMapper financialInstrumentMapper, 
    		FinancialInstrumentRepository financialInstrumentRepository, 
    		RequestContext requestContext) {
        this.financialInstrumentMapper = financialInstrumentMapper;
        this.financialInstrumentRepository = financialInstrumentRepository;
        this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
    }


    @Override
    public Page<FinancialInstrumentDto> list(String brandUuid, String memberUuid, int page, int pageSize, boolean includeInactive) throws FinancialInstrumentException {
        try {
            if (includeInactive) {
                return financialInstrumentRepository.findAllByBrandUuidAndMemberUuidAndDeletedIsFalse(brandUuid, memberUuid, PageRequest.of(page, pageSize))
                        .map(m -> financialInstrumentMapper.toDto(m));
            }

            return financialInstrumentRepository.findAllByBrandUuidAndMemberUuidAndDeletedIsFalseAndActiveIsTrue(brandUuid, memberUuid, PageRequest.of(page, pageSize))
                    .map(m -> financialInstrumentMapper.toDto(m));
        } catch (Exception e) {
            logger.error("Error - brandUuid={}", brandUuid, e);
            throw new FinancialInstrumentException(requestContext.getTrackingNumber(), FinancialInstrumentException.FIND_FAILED, e);
        }
    }
}
