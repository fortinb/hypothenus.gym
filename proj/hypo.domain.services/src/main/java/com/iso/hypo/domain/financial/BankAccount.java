package com.iso.hypo.domain.financial;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankAccount {
    
	private String institution;
	
	private String transit;
	
	private String accountNumber;
	
	private String accountHolderName;
	
	private String bankName;
	
	public BankAccount() {
	}
	
	public BankAccount(String institution, String transit, String accountNumber, String accountHolderName, String bankName) {
		this.institution = institution;
		this.transit = transit;
		this.accountNumber = accountNumber;
		this.accountHolderName = accountHolderName;
		this.bankName = bankName;
	}
}
