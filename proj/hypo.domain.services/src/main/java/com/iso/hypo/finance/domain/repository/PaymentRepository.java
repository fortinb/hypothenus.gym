package com.iso.hypo.finance.domain.repository;

import java.util.Date;
import java.util.Optional;

import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;
import com.iso.hypo.finance.domain.model.Payment;

public interface PaymentRepository {

	Optional<Payment> findByBrandUuidAndMemberUuidAndUuidAndDeletedIsFalse(String brandUuid, String memberUuid, String paymentUuid);

	PageResult<Payment> findAllByBrandUuidAndMemberUuidAndDeletedIsFalse(String brandUuid, String memberUuid, PageRequest pageRequest);
	
	PageResult<Payment> findAllByBrandUuidAndMemberUuidAndDeletedIsFalseAndActiveIsTrue(String brandUuid, String memberUuid, PageRequest pageRequest);
	
	Payment save(Payment payment);

    void delete(Payment payment);

    void deleteAll();
    
    long deleteAllByBrandUuid(String brandUuid, String deletedBy);
    
    long deleteAllByMemberUuid(String brandUuid, String memberUuid, String deletedBy);
    
	PageResult<Payment> findByDates(String brandUuid, Date startDate, Date endDate, PageRequest pageRequest);
}