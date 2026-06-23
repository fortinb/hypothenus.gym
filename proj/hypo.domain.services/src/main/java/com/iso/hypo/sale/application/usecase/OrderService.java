package com.iso.hypo.sale.application.usecase;

import com.iso.hypo.sale.application.dto.OrderDto;
import com.iso.hypo.sale.application.exception.OrderException;

public interface OrderService {

	OrderDto create(OrderDto orderDto) throws OrderException;
	
	OrderDto update(OrderDto orderDto) throws OrderException;
	
	OrderDto patch(OrderDto orderDto) throws OrderException;

	OrderDto submit(String brandUuid, String memberUuid, String orderUuid, OrderDto orderDto) throws OrderException;

	OrderDto cancel(String brandUuid, String memberUuid, String orderUuid) throws OrderException;

	void delete(String brandUuid, String memberUuid, String orderUuid) throws OrderException;

	void deleteAllByBrandUuid(String brandUuid) throws OrderException;

	void deleteAllByMemberUuid(String brandUuid, String memberUuid) throws OrderException;
}
