package com.iso.hypo.finance.application.dto;

import com.iso.hypo.common.application.dto.BaseEntityDto;
import com.iso.hypo.finance.domain.model.enumeration.FinancialInstrumentTypeEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FinancialInstrumentDto extends BaseEntityDto {

	private String uuid;

	private String brandUuid;
	
	private String memberUuid;
	
	private FinancialInstrumentTypeEnum type;

	private CreditCardDto creditCard;

	private BankAccountDto bankAccount;

	public FinancialInstrumentDto() {
	}

	public FinancialInstrumentDto(String brandUuid, String memberUuid, String uuid, FinancialInstrumentTypeEnum type,
			CreditCardDto creditCard, BankAccountDto bankAccount) {
		this.brandUuid = brandUuid;
		this.memberUuid = memberUuid;
		this.uuid = uuid;
		this.type = type;
		this.creditCard = creditCard;
		this.bankAccount = bankAccount;
	}
}
