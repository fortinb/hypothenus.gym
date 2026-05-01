package com.iso.hypo.finance.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.iso.hypo.finance.infrastructure.persistence.entity.FinancialInstrumentDocument;

public interface FinancialInstrumentMongoRepository extends PagingAndSortingRepository<FinancialInstrumentDocument, String>, CrudRepository<FinancialInstrumentDocument, String>, FinancialInstrumentMongoRepositoryCustom {

	Optional<FinancialInstrumentDocument> findByBrandUuidAndMemberUuidAndUuidAndDeletedIsFalse(String brandUuid, String memberUuid, String financialInstrumentUuid);

	Page<FinancialInstrumentDocument> findAllByBrandUuidAndMemberUuidAndDeletedIsFalse(String brandUuid, String memberUuid, Pageable pageable);
	
	Page<FinancialInstrumentDocument> findAllByBrandUuidAndMemberUuidAndDeletedIsFalseAndActiveIsTrue(String brandUuid, String memberUuid, Pageable pageable);
}