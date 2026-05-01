package com.iso.hypo.finance.infrastructure.persistence.repository;

public interface FinancialInstrumentMongoRepositoryCustom {

	long deleteAllByBrandUuid(String brandUuid, String deletedBy);
	
	long deleteAllByMemberUuid(String brandUuid, String memberUuid, String deletedBy);
	
    void deleteAll();
}