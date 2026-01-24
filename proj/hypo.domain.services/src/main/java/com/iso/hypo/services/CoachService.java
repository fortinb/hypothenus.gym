package com.iso.hypo.services;

import com.iso.hypo.domain.dto.CoachDto;
import com.iso.hypo.services.exception.CoachException;

public interface CoachService {

    CoachDto create(CoachDto coachDto) throws CoachException;

    CoachDto update(CoachDto coachDto) throws CoachException;

    CoachDto patch(CoachDto coachDto) throws CoachException;

    void delete(String brandUuid, String gymUuid, String coachUuid) throws CoachException;

    CoachDto activate(String brandUuid, String gymUuid, String coachUuid) throws CoachException;

    CoachDto deactivate(String brandUuid, String gymUuid, String coachUuid) throws CoachException;
}


