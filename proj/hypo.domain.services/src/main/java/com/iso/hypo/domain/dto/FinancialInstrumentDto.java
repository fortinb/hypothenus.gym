package com.iso.hypo.domain.dto;

import com.iso.hypo.common.dto.BaseEntityDto;
import com.iso.hypo.domain.enumeration.FinancialInstrumentTypeEnum;

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
