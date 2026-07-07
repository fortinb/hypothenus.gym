package com.iso.hypo.finance.domain.model;

import java.util.List;

import com.iso.hypo.common.domain.model.BaseEntity;
import com.iso.hypo.finance.domain.model.enumeration.FinancialInstrumentTypeEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FinancialInstrument extends BaseEntity  {

	private String id;
	
	private String uuid;

	private String brandUuid;

	private String memberUuid;
	
	private FinancialInstrumentTypeEnum type;
	
	private CreditCard creditCard;
	
	private BankAccount bankAccount;
	
	private List<Object> paymentServiceProviderRawResponse;
	
	public FinancialInstrument() {
	}
	
	public FinancialInstrument(String brandUuid, String memberUuid, FinancialInstrumentTypeEnum type, CreditCard creditCard, BankAccount bankAccount) {
		this.brandUuid = brandUuid;
		this.memberUuid = memberUuid;
		this.type = type;
		this.creditCard = creditCard;
		this.bankAccount = bankAccount;
	}
}
