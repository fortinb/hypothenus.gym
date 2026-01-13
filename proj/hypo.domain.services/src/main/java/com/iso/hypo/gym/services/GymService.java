package com.iso.hypo.gym.services;

import org.springframework.data.domain.Page;

import com.iso.hypo.gym.dto.GymDto;
import com.iso.hypo.gym.dto.GymSearchDto;
import com.iso.hypo.gym.exception.GymException;

public interface GymService {

    GymDto create(GymDto gymDto) throws GymException;

    GymDto update(String brandId, GymDto gymDto) throws GymException;

    GymDto patch(String brandId, GymDto gymDto) throws GymException;

    void delete(String brandId, String gymId) throws GymException;

    GymDto findByGymId(String brandId, String id) throws GymException;

    Page<GymSearchDto> search(int page, int pageSize, String criteria, boolean includeInactive) throws GymException;

    Page<GymDto> list(String brandId, int page, int pageSize, boolean includeInactive) throws GymException;

    GymDto activate(String brandId, String gymId) throws GymException;

    GymDto deactivate(String brandId, String gymId) throws GymException;
}