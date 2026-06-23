package com.iso.hypo.finance.application.usecase;

import com.iso.hypo.common.application.dto.PageResultDto;
import com.iso.hypo.finance.application.dto.FinancialInstrumentDto;
import com.iso.hypo.finance.application.exception.FinancialInstrumentException;

public interface FinancialInstrumentQueryService {

	FinancialInstrumentDto find(String brandUuid, String memberUuid, String financialInstrumentUUid) throws FinancialInstrumentException;
    
	PageResultDto<FinancialInstrumentDto> list(String brandUuid, String memberUuid,int page, int pageSize, boolean includeInactive) throws FinancialInstrumentException;

}