package com.iso.hypo.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.iso.hypo.domain.aggregate.FinancialInstrument;

public interface FinancialInstrumentRepository extends CrudRepository<FinancialInstrument, String>, FinancialInstrumentRepositoryCustom {

	Optional<FinancialInstrument> findByBrandUuidAndMemberUuidAndUuidAndDeletedIsFalse(String brandUuid, String memberUuid, String financialInstrumentUuid);

	Page<FinancialInstrument> findAllByBrandUuidAndMemberUuidAndDeletedIsFalse(String brandUuid, String memberUuid, Pageable pageable);
	
	Page<FinancialInstrument> findAllByBrandUuidAndMemberUuidAndDeletedIsFalseAndActiveIsTrue(String brandUuid, String memberUuid, Pageable pageable);
}