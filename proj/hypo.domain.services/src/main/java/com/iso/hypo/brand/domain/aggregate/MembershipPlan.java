package com.iso.hypo.brand.domain.aggregate;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.iso.hypo.brand.domain.enumeration.BillingFrequencyEnum;
import com.iso.hypo.brand.domain.enumeration.MembershipPlanPeriodEnum;
import com.iso.hypo.common.domain.BaseEntity;
import com.iso.hypo.common.domain.LocalizedString;
import com.iso.hypo.common.domain.pricing.Cost;
import com.iso.hypo.common.domain.pricing.OneTimeFee;
import com.iso.hypo.gym.domain.aggregate.Course;
import com.iso.hypo.gym.domain.aggregate.Gym;
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
	private String brandId;

	@NonNull
	private String code;

	private List<LocalizedString> name;

	private List<LocalizedString> description;

	private Integer numberOfClasses;

	private MembershipPlanPeriodEnum period;
	// weekly,# classes per week - Monday to Sunday
	// monthly # classes per month - 1 to 31
	// days, # classes for x days
	// hours, # hours
	// classes, # classes

	private BillingFrequencyEnum billingFrequency;

	private Cost cost;

	private List<OneTimeFee> oneTimeFees;

	private Integer durationInMonths;

	private boolean guestPrivilege;

	private boolean isPromotional;

	private boolean isGiftCard;

	@DBRef
	private List<Course> courses;

	@DBRef
	private List<Gym> includedGyms;

	public MembershipPlan() {

	}

	public MembershipPlan(String brandId, String code, List<LocalizedString> name, List<LocalizedString> description,
			Integer numberOfClasses, MembershipPlanPeriodEnum period, BillingFrequencyEnum billingFrequency, Cost cost,
			List<OneTimeFee> oneTimeFees, Integer durationInMonths, List<Course> courses, List<Gym> includedGyms,
			boolean guestPrivilege, boolean isGiftCard, boolean isPromotional, boolean isActive, Instant startedOn,
			Instant endedOn) {
		super(isActive);
		this.brandId = brandId;
		this.code = code;
		this.name = name;
		this.description = description;
		this.numberOfClasses = numberOfClasses;
		this.period = period;
		this.billingFrequency = billingFrequency;
		this.oneTimeFees = oneTimeFees;
		this.guestPrivilege = guestPrivilege;
		this.isGiftCard = isGiftCard;
		this.isPromotional = isPromotional;
		this.cost = cost;
		this.durationInMonths = durationInMonths;
		this.courses = courses;
		this.includedGyms = includedGyms;
		this.activatedOn = startedOn;
		this.deactivatedOn = endedOn;
	}
}
