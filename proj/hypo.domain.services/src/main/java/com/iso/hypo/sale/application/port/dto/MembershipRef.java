package com.iso.hypo.sale.application.port.dto;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MembershipRef {

	private String uuid;

	private String brandUuid;
	
	private String orderUuid;
	
	private String memberUuid;

	private MembershipPlanRef membershipPlan;

	private boolean cancelled = false;

	private Instant cancelledOn;
}
