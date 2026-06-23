package com.iso.hypo.finance.application.usecase;

import java.util.Date;

import com.iso.hypo.common.application.dto.PageResultDto;
import com.iso.hypo.finance.application.dto.PaymentDto;
import com.iso.hypo.finance.application.exception.PaymentException;

public interface PaymentQueryService {

	PageResultDto<PaymentDto> list(String brandUuid, String memberUuid, Date startDate, Date endDate, int page, int pageSize) throws PaymentException;
}