package com.iso.hypo.sale.application.port.dto;

import com.iso.hypo.common.domain.model.finance.Currency;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BrandRef {

	private String uuid;

	private String code;

	private String name;
	
	private Currency currency;
}
