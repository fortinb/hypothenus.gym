package com.iso.hypo.admin.papi.dto.model;

import com.iso.hypo.admin.papi.dto.BaseDto;
import com.iso.hypo.admin.papi.dto.enumeration.FinancialInstrumentTypeEnum;
import com.iso.hypo.admin.papi.dto.financial.BankAccountDto;
import com.iso.hypo.admin.papi.dto.financial.CreditCardDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FinancialInstrumentDto extends BaseDto {

	private String brandUuid;
	
	private String memberUuid;

	private String uuid;

	private boolean preferredInstrument;

	private FinancialInstrumentTypeEnum type;

	private CreditCardDto creditCard;

	private BankAccountDto bankAccount;

	public FinancialInstrumentDto() {
	}

	public FinancialInstrumentDto(String uuid, boolean preferredInstrument, FinancialInstrumentTypeEnum type,
			CreditCardDto creditCard, BankAccountDto bankAccount) {
		this.uuid = uuid;
		this.preferredInstrument = preferredInstrument;
		this.type = type;
		this.creditCard = creditCard;
		this.bankAccount = bankAccount;
	}
}
