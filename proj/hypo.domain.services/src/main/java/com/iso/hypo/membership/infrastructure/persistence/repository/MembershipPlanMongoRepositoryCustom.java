package com.iso.hypo.membership.infrastructure.persistence.repository;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.iso.hypo.membership.infrastructure.persistence.entity.MembershipPlanDocument;

public interface MembershipPlanMongoRepositoryCustom {
	
	Page<MembershipPlanDocument> findActiveOnDate(String brandUuid, Date currentDate, Pageable pageable);
	
    void deleteAll();
    
	long deleteAllByBrandUuid(String brandUuid, String deletedBy);

	long removeGymReferences(String brandUuid, String gymUuid);

	long removeCourseReferences(String brandUuid, String courseUuid);
}