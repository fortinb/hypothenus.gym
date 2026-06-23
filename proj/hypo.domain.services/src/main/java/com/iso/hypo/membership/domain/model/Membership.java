package com.iso.hypo.membership.domain.model;

import java.time.Instant;

import com.iso.hypo.common.domain.model.BaseEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Membership extends BaseEntity {

	private String id;
	
	private String uuid;

	private String brandUuid;
	
	private String orderUuid;

	private MembershipPlan membershipPlan;
	
	private String memberUuid;
	
	private int remainingClasses;
	
	private boolean autoRenewal = false;
	
	private boolean cancelled = false;
	
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
		this.cancelled = isCancelled;
		this.activatedOn = startedOn;
		this.deactivatedOn = endedOn;
	}
}
