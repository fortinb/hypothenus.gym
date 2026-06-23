package com.iso.hypo.sale.application.port;

import java.util.Optional;

import com.iso.hypo.sale.application.port.dto.MembershipPlanRef;

public interface MembershipPlanServicePort {

	 Optional<MembershipPlanRef> find(String brandUuid, String membershipPlanUuid);
}