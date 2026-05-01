package com.iso.hypo.finance.infrastructure.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.iso.hypo.common.infrastructure.persistence.entity.BaseDocument;
import com.iso.hypo.finance.domain.model.BankAccount;
import com.iso.hypo.finance.domain.model.CreditCard;
import com.iso.hypo.finance.domain.model.enumeration.FinancialInstrumentTypeEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document("financialinstrument")
public class FinancialInstrumentDocument extends BaseDocument {
	
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
	
	public FinancialInstrumentDocument() {
	}
	
	public FinancialInstrumentDocument(String brandUuid, String memberUuid, FinancialInstrumentTypeEnum type, CreditCard creditCard, BankAccount bankAccount) {
		this.brandUuid = brandUuid;
		this.memberUuid = memberUuid;
		this.type = type;
		this.creditCard = creditCard;
		this.bankAccount = bankAccount;
	}
}
