package com.iso.hypo.domain;

import java.time.Instant;
import java.util.UUID;

import com.iso.hypo.membership.domain.model.Membership;
import com.iso.hypo.membership.domain.model.MembershipPlan;

public class MembershipBuilder {
	
	public static Membership build(String brandUuid, String memberUuid, MembershipPlan membershipPlan) {
		Membership entity = new Membership(brandUuid, memberUuid, membershipPlan, false, false, true,Instant.now(), null);
		entity.setUuid(UUID.randomUUID().toString());
		return entity;
	}
}