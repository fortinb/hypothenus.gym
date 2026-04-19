package com.iso.hypo.domain.aggregate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.iso.hypo.domain.BaseEntity;
import com.iso.hypo.domain.enumeration.FinancialInstrumentTypeEnum;
import com.iso.hypo.domain.financial.BankAccount;
import com.iso.hypo.domain.financial.CreditCard;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document("financialinstrument")
public class FinancialInstrument extends BaseEntity  {
	
	@Id
	private String id;
	
	@Indexed
	private String uuid;
	
	@Indexed
	private String brandUuid;
	
	@Indexed
	private String memberUuid;
	
	private FinancialInstrumentTypeEnum type;
	
	private CreditCard creditCard;
	
	private BankAccount bankAccount;
	
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
