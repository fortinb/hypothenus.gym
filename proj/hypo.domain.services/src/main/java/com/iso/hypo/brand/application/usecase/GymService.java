package com.iso.hypo.brand.application.usecase;

import com.iso.hypo.brand.application.dto.GymDto;
import com.iso.hypo.brand.application.exception.GymException;

public interface GymService {

    GymDto create(GymDto gymDto) throws GymException;

    GymDto update(GymDto gymDto) throws GymException;

    GymDto patch(GymDto gymDto) throws GymException;

    void delete(String brandUuid, String gymUuid) throws GymException;
    
    GymDto activate(String brandUuid, String gymUuid) throws GymException;

    GymDto deactivate(String brandUuid, String gymUuid) throws GymException;
    
    void deleteAllByBrandUuid(String brandUuid) throws GymException;

	void removeAllCoachReferencesByCoachUuid(String brandUuid, String coachUuid) throws GymException;

	GymDto assignCoach(String brandUuid, String gymUuid, String coachUuid) throws GymException;
	
	GymDto unassignCoach(String brandUuid, String gymUuid, String coachUuid) throws GymException;
}
