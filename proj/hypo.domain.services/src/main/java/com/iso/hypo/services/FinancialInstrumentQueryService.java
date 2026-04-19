package com.iso.hypo.services;

import org.springframework.data.domain.Page;

import com.iso.hypo.domain.dto.FinancialInstrumentDto;
import com.iso.hypo.services.exception.FinancialInstrumentException;

public interface FinancialInstrumentQueryService {

    Page<FinancialInstrumentDto> list(String brandUuid, String memberUuid,int page, int pageSize, boolean includeInactive) throws FinancialInstrumentException;

}