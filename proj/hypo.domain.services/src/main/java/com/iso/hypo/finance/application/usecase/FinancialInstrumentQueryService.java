package com.iso.hypo.finance.application.usecase;

import org.springframework.data.domain.Page;

import com.iso.hypo.finance.application.dto.FinancialInstrumentDto;
import com.iso.hypo.finance.application.exception.FinancialInstrumentException;

public interface FinancialInstrumentQueryService {

    Page<FinancialInstrumentDto> list(String brandUuid, String memberUuid,int page, int pageSize, boolean includeInactive) throws FinancialInstrumentException;

}