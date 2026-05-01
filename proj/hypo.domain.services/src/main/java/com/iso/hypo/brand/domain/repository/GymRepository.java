package com.iso.hypo.brand.domain.repository;

import java.util.Optional;

import com.iso.hypo.brand.domain.model.Gym;
import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;

public interface GymRepository {
	
	Optional<Gym> findByBrandUuidAndUuidAndDeletedIsFalse(String brandUuid, String gymUuid);
	
	Optional<Gym> findByBrandUuidAndCode(String brandUuid, String code);
	
	PageResult<Gym> findAllByBrandUuidAndDeletedIsFalse(String brandUuid, PageRequest pageRequest);
	
	PageResult<Gym> findAllByBrandUuidAndDeletedIsFalseAndActiveIsTrue(String brandUuid, PageRequest pageRequest);
	
	Gym save(Gym gym);

    void delete(Gym gym);

    void deleteAll();
    
	long deleteAllByBrandUuid(String brandUuid, String deletedBy);
	
	long removeCoachReferences(String brandUuid, String coachUuid);
}

