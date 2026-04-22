package com.iso.hypo.membership.domain.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.iso.hypo.common.domain.model.BaseEntity;
import com.mongodb.lang.NonNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document("membership")
public class Membership extends BaseEntity {

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
	
	private boolean autoRenewal = true;
	
	private boolean isCancelled = false;
	
	private Instant cancelledOn;
	
	public Membership() {
		super();
	}

	public Membership(String brandUuid, String memberUuid, MembershipPlan membershipPlan, boolean autoRenewal, boolean isCancelled, boolean active, Instant startedOn, Instant endedOn) {
		super(active);
		this.brandUuid = brandUuid;
		this.memberUuid = memberUuid;
		this.membershipPlan = membershipPlan;
		this.autoRenewal = autoRenewal;
		this.isCancelled = isCancelled;
		this.activatedOn = startedOn;
		this.deactivatedOn = endedOn;
	}
}
