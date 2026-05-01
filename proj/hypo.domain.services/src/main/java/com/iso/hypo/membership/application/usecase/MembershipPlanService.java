package com.iso.hypo.membership.application.usecase;

import com.iso.hypo.membership.application.dto.MembershipPlanDto;
import com.iso.hypo.membership.application.exception.MembershipPlanException;

public interface MembershipPlanService {

    MembershipPlanDto create(MembershipPlanDto membershipPlanDto) throws MembershipPlanException;

    MembershipPlanDto update(MembershipPlanDto membershipPlanDto) throws MembershipPlanException;

    MembershipPlanDto patch(MembershipPlanDto membershipPlanDto) throws MembershipPlanException;

    void delete(String brandUuid, String membershipPlanUuid) throws MembershipPlanException;

    MembershipPlanDto activate(String brandUuid, String membershipPlanUuid) throws MembershipPlanException;

    MembershipPlanDto deactivate(String brandUuid, String membershipPlanUuid) throws MembershipPlanException;
    
    void deleteAllByBrandUuid(String brandUuid) throws MembershipPlanException;

	void removeAllGymReferencesByGymUuid(String brandUuid, String gymUuid) throws MembershipPlanException;
	
	void removeAllCourseReferencesByCourseUuid(String brandUuid, String courseUuid) throws MembershipPlanException;
}