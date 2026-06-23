package com.iso.hypo.sale.domain.model;

import java.util.List;

import com.iso.hypo.common.application.dto.LocalizedStringDto;
import com.iso.hypo.common.application.dto.enumeration.MembershipPlanPeriodEnumDto;
import com.iso.hypo.common.domain.model.finance.Cost;
import com.iso.hypo.membership.domain.model.enumeration.BillingFrequencyEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MembershipPlan {

    private String uuid;
    
    private List<LocalizedStringDto> name;
    
    private int numberOfClasses;

    private MembershipPlanPeriodEnumDto period;

    private BillingFrequencyEnum billingFrequency;
    
	private Cost price;
}
