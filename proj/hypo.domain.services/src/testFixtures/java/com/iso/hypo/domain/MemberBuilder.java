package com.iso.hypo.domain;

import java.time.Instant;
import java.util.UUID;

import com.iso.hypo.domain.aggregate.Member;
import com.iso.hypo.domain.enumeration.MemberTypeEnum;

public class MemberBuilder {
	
	public static Member build(String brandUuid, MemberTypeEnum memberType) {
		Member entity = new Member(brandUuid, PersonBuilder.build(), memberType, true, Instant.now(), null);
		entity.setUuid(UUID.randomUUID().toString());
		return entity;
	}
}