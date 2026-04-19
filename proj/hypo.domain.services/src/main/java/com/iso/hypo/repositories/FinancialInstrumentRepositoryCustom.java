package com.iso.hypo.repositories;

import java.util.Optional;

import com.iso.hypo.domain.aggregate.FinancialInstrument;

public interface FinancialInstrumentRepositoryCustom {

    Optional<FinancialInstrument> activate(String brandUuid, String memberUuid, String financialInstrumentUuid);

    Optional<FinancialInstrument> deactivate(String brandUuid, String memberUuid, String financialInstrumentUuid);

    void delete(String brandUuid, String memberUuid, String financialInstrumentUuid, String deletedBy);

    long deleteAllByBrandUuid(String brandUuid, String deletedBy);
    
    long deleteAllByMemberUuid(String brandUuid, String memberUuid, String deletedBy);
}