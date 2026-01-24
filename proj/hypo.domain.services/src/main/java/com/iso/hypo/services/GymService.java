package com.iso.hypo.services;

import com.iso.hypo.domain.dto.GymDto;
import com.iso.hypo.services.exception.GymException;

public interface GymService {

    GymDto create(GymDto gymDto) throws GymException;

    GymDto update(GymDto gymDto) throws GymException;

    GymDto patch(GymDto gymDto) throws GymException;

    void delete(String brandUuid, String gymUuid) throws GymException;

    GymDto activate(String brandUuid, String gymUuid) throws GymException;

    GymDto deactivate(String brandUuid, String gymUuid) throws GymException;
}


