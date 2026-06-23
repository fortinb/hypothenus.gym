package com.iso.hypo.membership.infrastructure.persistence.entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.iso.hypo.common.domain.model.BaseEntity;
import com.iso.hypo.membership.domain.model.MembershipPlan;
import com.mongodb.lang.NonNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document("membership")
public class MembershipDocument extends BaseEntity {

	@Id
	private String id;
	
	@Indexed
	private String uuid;

	@Indexed
	@NonNull
	private String brandUuid;

	private MembershipPlan membershipPlan;
	
	private String memberUuid;
	
	private int remainingClasses;
	
	private boolean autoRenewal;
	
	private boolean cancelled;
	
	private Instant cancelledOn;
	
	public MembershipDocument() {
		super();
	}

	public MembershipDocument(String brandUuid, String memberUuid, MembershipPlan membershipPlan, boolean autoRenewal, boolean isCancelled, boolean active, Instant startedOn, Instant endedOn) {
		super(active);
		this.brandUuid = brandUuid;
		this.memberUuid = memberUuid;
		this.membershipPlan = membershipPlan;
		this.autoRenewal = autoRenewal;
		this.cancelled = isCancelled;
		this.activatedOn = startedOn;
		this.deactivatedOn = endedOn;
	}
}
