package com.iso.hypo.gym.domain.model.aggregate;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

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
	@NonNull
	private String brandId;

	private MembershipPlan membershipPlan;
	
//	@DBRef
	private Member member;
	
	private Integer remainingClasses;
	
	private boolean autoRenewal = true;
	
	private boolean isCancelled = false;
	
	private Instant cancelledOn;
	
	public Membership() {
		
	}

	public Membership(String brandId, Member member, MembershipPlan membershipPlan, boolean autoRenewal, boolean isCancelled, boolean isActive, Instant startedOn, Instant endedOn) {
		super(isActive);
		this.brandId = brandId;
		this.member = member;
		this.membershipPlan = membershipPlan;
		this.autoRenewal = autoRenewal;
		this.isCancelled = isCancelled;
		this.activatedOn = startedOn;
		this.deactivatedOn = endedOn;
	}
}
