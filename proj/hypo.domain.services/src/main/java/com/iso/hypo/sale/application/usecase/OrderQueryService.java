package com.iso.hypo.sale.application.usecase;

import com.iso.hypo.common.application.dto.PageResultDto;
import com.iso.hypo.sale.application.dto.OrderDto;
import com.iso.hypo.sale.application.exception.OrderException;

public interface OrderQueryService {

	PageResultDto<OrderDto> list(String brandUuid, String memberUuid, int page, int pageSize, boolean includeInactive) throws OrderException;

	OrderDto findByUuid(String brandUuid, String memberUuid, String orderUuid) throws OrderException;
}
