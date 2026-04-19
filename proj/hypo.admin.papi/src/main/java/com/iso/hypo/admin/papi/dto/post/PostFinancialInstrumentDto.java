package com.iso.hypo.admin.papi.dto.post;

import com.iso.hypo.admin.papi.dto.enumeration.FinancialInstrumentTypeEnum;
import com.iso.hypo.admin.papi.dto.financial.BankAccountDto;
import com.iso.hypo.admin.papi.dto.financial.CreditCardDto;

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

	private FinancialInstrumentTypeEnum type;

	private CreditCardDto creditCard;

	private BankAccountDto bankAccount;
}
