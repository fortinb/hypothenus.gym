package com.iso.hypo.model.services;

import org.springframework.data.domain.Page;

import com.iso.hypo.model.dto.GymDto;
import com.iso.hypo.model.dto.GymSearchDto;
import com.iso.hypo.model.exception.GymException;

public interface GymService {

    GymDto create(GymDto gymDto) throws GymException;

    GymDto update(String brandUuid, GymDto gymDto) throws GymException;

    GymDto patch(String brandUuid, GymDto gymDto) throws GymException;

    void delete(String brandUuid, String gymUuid) throws GymException;

    GymDto findByCode(String brandUuid, String code) throws GymException;

    Page<GymSearchDto> search(int page, int pageSize, String criteria, boolean includeInactive) throws GymException;

    Page<GymDto> list(String brandUuid, int page, int pageSize, boolean includeInactive) throws GymException;

    GymDto activate(String brandUuid, String gymUuid) throws GymException;

    GymDto deactivate(String brandUuid, String gymUuid) throws GymException;
}