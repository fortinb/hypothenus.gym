package com.iso.hypo.sale.application.port;

import java.util.List;
import java.util.Optional;

import com.iso.hypo.sale.application.port.dto.MembershipRef;

public interface MembershipServicePort {

	boolean isNewMember(String brandUuid, String memberUuid);
	
	Optional<MembershipRef> findByMembershipPlanUuid(String brandUuid, String memberUuid, String membershipPlanUuid);
	
	List<MembershipRef> create(List<MembershipRef> membershipRef);
}