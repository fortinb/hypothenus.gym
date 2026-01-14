package com.iso.hypo.gym.services;

import org.springframework.data.domain.Page;

import com.iso.hypo.gym.dto.CoachDto;
import com.iso.hypo.gym.exception.GymException;

public interface CoachService {

    CoachDto create(String brandId, String gymId, CoachDto coachDto) throws GymException;

    CoachDto update(String brandId, String gymId, CoachDto coachDto) throws GymException;

    CoachDto patch(String brandId, String gymId, CoachDto coachDto) throws GymException;

    void delete(String brandId, String gymId, String coachUuid) throws GymException;

    CoachDto findByCoachUuid(String brandId, String gymId, String coachUuid) throws GymException;

    Page<CoachDto> list(String brandId, String gymId, int page, int pageSize, boolean includeInactive) throws GymException;

    CoachDto activate(String brandId, String gymId, String coachUuid) throws GymException;

    CoachDto deactivate(String brandId, String gymId, String coachUuid) throws GymException;
}