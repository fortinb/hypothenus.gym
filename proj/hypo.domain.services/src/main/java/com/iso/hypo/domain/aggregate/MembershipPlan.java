package com.iso.hypo.domain.aggregate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.iso.hypo.domain.BaseEntity;
import com.iso.hypo.domain.LocalizedString;
import com.iso.hypo.domain.enumeration.BillingFrequencyEnum;
import com.iso.hypo.domain.enumeration.MembershipPlanPeriodEnum;
import com.iso.hypo.domain.pricing.Cost;
import com.mongodb.lang.NonNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document("membershipplan")
public class MembershipPlan extends BaseEntity {

	@Id
	private String id;
	
	@Indexed
	private String uuid;
	
	@Indexed
	@NonNull
	private String brandUuid;

	private List<LocalizedString> name;

	private List<LocalizedString> title;

	private List<LocalizedString> description;

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

	private boolean isPromotional;

	private boolean isGiftCard;
	
	private Date startDate;
	
	private Date endDate;

	@DBRef
	private List<Course> includedCourses = new ArrayList<>();

	@DBRef
	private List<Gym> includedGyms = new ArrayList<>();

	public MembershipPlan() {
		super();
	}

	public MembershipPlan(String brandUuid, List<LocalizedString> name, List<LocalizedString> title,
			List<LocalizedString> description, int numberOfClasses, MembershipPlanPeriodEnum period,
			BillingFrequencyEnum billingFrequency, Cost cost, int durationInMonths,
			List<Gym> includedGyms,List<Course> includedCourses, Date startDate, Date endDate, boolean guestPrivilege, boolean isGiftCard,
			boolean isPromotional, boolean isActive, Instant startedOn, Instant endedOn) {
		super(isActive);
		this.brandUuid = brandUuid;
		this.name = name;
		this.title = title;
		this.description = description;
		this.startDate = startDate;
		this.endDate = endDate;
		this.numberOfClasses = numberOfClasses;
		this.period = period;
		this.billingFrequency = billingFrequency;
		this.guestPrivilege = guestPrivilege;
		this.isGiftCard = isGiftCard;
		this.isPromotional = isPromotional;
		this.cost = cost;
		this.durationInMonths = durationInMonths;
		this.includedCourses = includedCourses;
		this.includedGyms = includedGyms;
		this.activatedOn = startedOn;
		this.deactivatedOn = endedOn;
	}
}
