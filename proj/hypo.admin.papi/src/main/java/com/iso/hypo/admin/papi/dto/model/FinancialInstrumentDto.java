package com.iso.hypo.admin.papi.dto.model;

import com.iso.hypo.admin.papi.dto.BaseDto;
import com.iso.hypo.admin.papi.dto.finance.BankAccountDto;
import com.iso.hypo.admin.papi.dto.finance.CreditCardDto;
import com.iso.hypo.common.application.dto.enumeration.FinancialInstrumentTypeEnumDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FinancialInstrumentDto extends BaseDto {

	private String brandUuid;
	
	private String memberUuid;

	private String uuid;

	private boolean preferredInstrument;

	private FinancialInstrumentTypeEnumDto type;

	private CreditCardDto creditCard;

	private BankAccountDto bankAccount;

	public FinancialInstrumentDto() {
	}

	public FinancialInstrumentDto(String uuid, boolean preferredInstrument, FinancialInstrumentTypeEnumDto type,
			CreditCardDto creditCard, BankAccountDto bankAccount) {
		this.uuid = uuid;
		this.preferredInstrument = preferredInstrument;
		this.type = type;
		this.creditCard = creditCard;
		this.bankAccount = bankAccount;
	}
}
