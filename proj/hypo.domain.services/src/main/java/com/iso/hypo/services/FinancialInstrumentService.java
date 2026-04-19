package com.iso.hypo.services;

import com.iso.hypo.domain.dto.FinancialInstrumentDto;
import com.iso.hypo.services.exception.FinancialInstrumentException;

public interface FinancialInstrumentService {

    FinancialInstrumentDto create(FinancialInstrumentDto financialInstrumentDto) throws FinancialInstrumentException;

    void delete(String brandUuid, String memberUuid, String financialInstrumentUuid) throws FinancialInstrumentException;

    FinancialInstrumentDto activate(String brandUuid, String memberUuid, String financialInstrumentUuid) throws FinancialInstrumentException;

    FinancialInstrumentDto deactivate(String brandUuid, String memberUuid, String financialInstrumentUuid) throws FinancialInstrumentException;

    void deleteAllByBrandUuid(String brandUuid) throws FinancialInstrumentException;
    
    void deleteAllByMemberUuid(String brandUuid, String memberUuid) throws FinancialInstrumentException;
}