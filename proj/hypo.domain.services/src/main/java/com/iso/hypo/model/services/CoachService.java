package com.iso.hypo.model.services;

import org.springframework.data.domain.Page;

import com.iso.hypo.model.dto.CoachDto;
import com.iso.hypo.model.exception.GymException;

public interface CoachService {

    CoachDto create(String brandUuid, String gymUuid, CoachDto coachDto) throws GymException;

    CoachDto update(String brandUuid, String gymUuid, CoachDto coachDto) throws GymException;

    CoachDto patch(String brandUuid, String gymUuid, CoachDto coachDto) throws GymException;

    void delete(String brandUuid, String gymUuid, String coachUuid) throws GymException;

    CoachDto findByCoachUuid(String brandUuid, String gymUuid, String coachUuid) throws GymException;

    Page<CoachDto> list(String brandUuid, String gymUuid, int page, int pageSize, boolean includeInactive) throws GymException;

    CoachDto activate(String brandUuid, String gymUuid, String coachUuid) throws GymException;

    CoachDto deactivate(String brandUuid, String gymUuid, String coachUuid) throws GymException;
}