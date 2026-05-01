package com.iso.hypo.membership.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.iso.hypo.common.domain.model.BaseEntity;
import com.iso.hypo.common.domain.model.LocalizedString;
import com.iso.hypo.common.domain.model.finance.Cost;
import com.iso.hypo.membership.domain.model.enumeration.BillingFrequencyEnum;
import com.iso.hypo.membership.domain.model.enumeration.MembershipPlanPeriodEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MembershipPlan extends BaseEntity {

	private String id;

	private String uuid;

	private String brandUuid;

	private List<LocalizedString> name;

	private List<LocalizedString> title;

	private List<LocalizedString> description;

	private List<LocalizedString> termsOfUse;

	private int numberOfClasses;

	private MembershipPlanPeriodEnum period;
	// weekly,# classes per week - Monday to Sunday
	// monthly # classes per month - 1 to 31
	// days, # classes for x days
	// hours, # hours
	// classes, # classes

	private BillingFrequencyEnum billingFrequency;

	private Cost cost;

	private int durationInMonths;

	private boolean guestPrivilege;

	private boolean promotional;

	private boolean giftCard;
	
	private Date startDate;
	
	private Date endDate;

	private List<String> includedCourseUuids = new ArrayList<>();

	private List<String> includedGymUuids = new ArrayList<>();

	public MembershipPlan() {
		super();
	}

	public MembershipPlan(String brandUuid, List<LocalizedString> name, List<LocalizedString> title,
			List<LocalizedString> description, List<LocalizedString> termsOfUse, int numberOfClasses, MembershipPlanPeriodEnum period,
			BillingFrequencyEnum billingFrequency, Cost cost, int durationInMonths,
			List<String> includedGymUuids, List<String> includedCourseUuids, Date startDate, Date endDate, boolean guestPrivilege, boolean giftCard,
			boolean promotional, boolean active, Instant startedOn, Instant endedOn) {
		super(active);
		this.brandUuid = brandUuid;
		this.name = name;
		this.title = title;
		this.description = description;
		this.termsOfUse = termsOfUse;
		this.startDate = startDate;
		this.endDate = endDate;
		this.numberOfClasses = numberOfClasses;
		this.period = period;
		this.billingFrequency = billingFrequency;
		this.guestPrivilege = guestPrivilege;
		this.giftCard = giftCard;
		this.promotional = promotional;
		this.cost = cost;
		this.durationInMonths = durationInMonths;
		this.includedCourseUuids = includedCourseUuids;
		this.includedGymUuids = includedGymUuids;
		this.activatedOn = startedOn;
		this.deactivatedOn = endedOn;
	}
}
