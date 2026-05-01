package com.iso.hypo.finance.domain.repository;

import java.util.Optional;

import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;
import com.iso.hypo.finance.domain.model.FinancialInstrument;

public interface FinancialInstrumentRepository {

	Optional<FinancialInstrument> findByBrandUuidAndMemberUuidAndUuidAndDeletedIsFalse(String brandUuid, String memberUuid, String financialInstrumentUuid);

	PageResult<FinancialInstrument> findAllByBrandUuidAndMemberUuidAndDeletedIsFalse(String brandUuid, String memberUuid, PageRequest pageRequest);
	
	PageResult<FinancialInstrument> findAllByBrandUuidAndMemberUuidAndDeletedIsFalseAndActiveIsTrue(String brandUuid, String memberUuid, PageRequest pageRequest);
	
	FinancialInstrument save(FinancialInstrument financialInstrument);

    void delete(FinancialInstrument financialInstrument);

    void deleteAll();
    
    long deleteAllByBrandUuid(String brandUuid, String deletedBy);
    
    long deleteAllByMemberUuid(String brandUuid, String memberUuid, String deletedBy);
}