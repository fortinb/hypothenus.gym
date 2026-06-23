package com.iso.hypo.finance.infrastructure.persistence.repository;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.iso.hypo.finance.infrastructure.persistence.entity.PaymentDocument;

public interface PaymentMongoRepositoryCustom {

	long deleteAllByBrandUuid(String brandUuid, String deletedBy);
	
	long deleteAllByMemberUuid(String brandUuid, String memberUuid, String deletedBy);
	
    void deleteAll();
    
	Page<PaymentDocument> findByDates(String brandUuid, Date startDate, Date endDate, Pageable pageable);
}