package com.iso.hypo.brand.application.usecase;

import com.iso.hypo.brand.application.dto.CoachDto;
import com.iso.hypo.brand.domain.exception.CoachException;

public interface CoachService {

    CoachDto create(CoachDto coachDto) throws CoachException;

    CoachDto update(CoachDto coachDto) throws CoachException;

    CoachDto patch(CoachDto coachDto) throws CoachException;

    void delete(String brandUuid, String coachUuid) throws CoachException;

    CoachDto activate(String brandUuid, String coachUuid) throws CoachException;

    CoachDto deactivate(String brandUuid, String coachUuid) throws CoachException;
    
    void deleteAllByBrandUuid(String brandUuid) throws CoachException;
}