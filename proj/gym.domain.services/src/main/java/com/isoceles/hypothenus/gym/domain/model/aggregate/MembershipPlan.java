package com.isoceles.hypothenus.gym.domain.model.aggregate;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.isoceles.hypothenus.gym.domain.model.LocalizedString;
import com.isoceles.hypothenus.gym.domain.model.MembershipPlanPaymentOptionEnum;
import com.isoceles.hypothenus.gym.domain.model.MembershipPlanPeriodEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document("membershipplans")
public class MembershipPlan extends BaseEntity {

	@Id
	private String id;

	@Indexed
	private String brandId;

	private String code;

	private List<LocalizedString> name;

	private List<LocalizedString> description;

	private Integer maxNumberOfClassesPerPeriod;
	
	private MembershipPlanPeriodEnum period;
	
	private MembershipPlanPaymentOptionEnum paymentOption;
	
	private Integer price;
	
	private Integer durationInMonths;
	
	@DBRef
	private List<Course> courses;
	
	public MembershipPlan() {
		
	}

	public MembershipPlan(String brandId, String code, List<LocalizedString> name, List<LocalizedString> description,
			Integer maxNumberOfClassesPerPeriod, MembershipPlanPeriodEnum period, MembershipPlanPaymentOptionEnum paymentOption, Integer price,
			Integer durationInMonths, List<Course> courses, boolean isActive, Instant startedOn, Instant endedOn) {
		super(isActive);
		this.brandId = brandId;
		this.code = code;
		this.name = name;
		this.description = description;
		this.maxNumberOfClassesPerPeriod = maxNumberOfClassesPerPeriod;
		this.period = period;
		this.paymentOption = paymentOption;
		this.price = price;
		this.durationInMonths = durationInMonths;
		this.courses = courses;
		this.activatedOn = startedOn;
		this.deactivatedOn = endedOn;
	}
}
