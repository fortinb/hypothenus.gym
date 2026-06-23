package com.iso.hypo.admin.papi.dto.order;

import java.util.List;

import com.iso.hypo.common.application.dto.LocalizedStringDto;
import com.iso.hypo.common.application.dto.enumeration.BillingFrequencyEnumDto;
import com.iso.hypo.common.application.dto.enumeration.MembershipPlanPeriodEnumDto;
import com.iso.hypo.common.domain.model.finance.Cost;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MembershipPlanDto {

    private String uuid;
    
    private List<LocalizedStringDto> name;

    private int numberOfClasses;

    private MembershipPlanPeriodEnumDto period;

    private BillingFrequencyEnumDto billingFrequency;
    
	private Cost price;
}
