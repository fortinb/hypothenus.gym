package com.iso.hypo.finance.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.iso.hypo.finance.infrastructure.persistence.entity.PaymentDocument;

public interface PaymentMongoRepository extends PagingAndSortingRepository<PaymentDocument, String>, CrudRepository<PaymentDocument, String>, PaymentMongoRepositoryCustom {

	Optional<PaymentDocument> findByBrandUuidAndMemberUuidAndUuidAndDeletedIsFalse(String brandUuid, String memberUuid, String paymentUuid);

	Page<PaymentDocument> findAllByBrandUuidAndMemberUuidAndDeletedIsFalse(String brandUuid, String memberUuid, Pageable pageable);
	
	Page<PaymentDocument> findAllByBrandUuidAndMemberUuidAndDeletedIsFalseAndActiveIsTrue(String brandUuid, String memberUuid, Pageable pageable);
}