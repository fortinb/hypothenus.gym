package com.iso.hypo.domain;

import java.util.UUID;

import com.iso.hypo.finance.domain.model.CreditCard;
import com.iso.hypo.finance.domain.model.FinancialInstrument;
import com.iso.hypo.finance.domain.model.enumeration.FinancialInstrumentTypeEnum;
import com.iso.hypo.membership.domain.model.Member;

public class FinancialInstrumentBuilder {
	
	public static FinancialInstrument build(String brandUuid, Member member) {
		FinancialInstrument entity = new FinancialInstrument(brandUuid, member.getUuid(), FinancialInstrumentTypeEnum.creditCard, buildCreditCard(member), null);
		entity.setUuid(UUID.randomUUID().toString());
		return entity;
	}
	
	private static CreditCard buildCreditCard(Member member) {
		return new CreditCard("4761739012345728", "John Doe", "1227",  null);
	}
}