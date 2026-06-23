package com.iso.hypo.admin.papi.dto.post;

import com.iso.hypo.admin.papi.dto.finance.BankAccountDto;
import com.iso.hypo.admin.papi.dto.finance.CreditCardDto;
import com.iso.hypo.common.application.dto.enumeration.FinancialInstrumentTypeEnumDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostFinancialInstrumentDto {

	@NotBlank
	private String brandUuid;
	
	@NotBlank
	private String memberUuid;
	
	private boolean preferredInstrument;

	private FinancialInstrumentTypeEnumDto type;

	private CreditCardDto creditCard;

	private BankAccountDto bankAccount;
}
