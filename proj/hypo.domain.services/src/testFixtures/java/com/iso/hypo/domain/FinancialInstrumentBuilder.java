package com.iso.hypo.domain;

import java.util.UUID;

import com.iso.hypo.domain.aggregate.FinancialInstrument;
import com.iso.hypo.domain.aggregate.Member;
import com.iso.hypo.domain.enumeration.FinancialInstrumentTypeEnum;
import com.iso.hypo.domain.financial.CreditCard;

public class FinancialInstrumentBuilder {
	
	public static FinancialInstrument build(String brandUuid, Member member) {
		FinancialInstrument entity = new FinancialInstrument(brandUuid, member.getUuid(), FinancialInstrumentTypeEnum.creditCard, buildCreditCard(member), null);
		entity.setUuid(UUID.randomUUID().toString());
		return entity;
	}
	
	private static CreditCard buildCreditCard(Member member) {
		return new CreditCard("1234567890123456", member.getPerson().getLastname(), "12/25", "123", null);
	}
}