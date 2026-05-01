package com.iso.hypo.brand.domain.repository;

import java.util.Optional;

import com.iso.hypo.brand.domain.model.Coach;
import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;

public interface CoachRepository {
	
	Optional<Coach> findByBrandUuidAndUuidAndDeletedIsFalse(String brandUuid, String coachUuid);
	
	PageResult<Coach> findAllByBrandUuidAndDeletedIsFalse(String brandUuid, PageRequest pageRequest);
	
	PageResult<Coach> findAllByBrandUuidAndDeletedIsFalseAndActiveIsTrue(String brandUuid, PageRequest pageRequest);
	
	Coach save(Coach coach);

    void delete(Coach coach);
    
    void deleteAll();
    
	long deleteAllByBrandUuid(String brandUuid, String deletedBy);
}