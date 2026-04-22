package com.iso.hypo.finance.application.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class BankAccountDto {

	private String institution;

	private String transit;

	private String accountNumber;

	private String accountHolderName;

	private String bankName;

	public BankAccountDto() {
	}

	public BankAccountDto(String institution, String transit, String accountNumber, String accountHolderName, String bankName) {
		this.institution = institution;
		this.transit = transit;
		this.accountNumber = accountNumber;
		this.accountHolderName = accountHolderName;
		this.bankName = bankName;
	}
}
