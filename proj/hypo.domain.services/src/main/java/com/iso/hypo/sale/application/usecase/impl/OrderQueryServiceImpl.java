package com.iso.hypo.sale.application.usecase.impl;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iso.hypo.common.application.context.RequestContext;
import com.iso.hypo.common.application.dto.PageResultDto;
import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;
import com.iso.hypo.sale.application.dto.OrderDto;
import com.iso.hypo.sale.application.exception.OrderException;
import com.iso.hypo.sale.application.mapper.OrderDtoMapper;
import com.iso.hypo.sale.application.usecase.OrderQueryService;
import com.iso.hypo.sale.domain.model.Order;
import com.iso.hypo.sale.domain.repository.OrderRepository;

@Service
public class OrderQueryServiceImpl implements OrderQueryService {

	private final OrderRepository orderRepository;

	private final OrderDtoMapper orderMapper;

	private static final Logger logger = LoggerFactory.getLogger(OrderQueryServiceImpl.class);

	private final RequestContext requestContext;

	public OrderQueryServiceImpl(
			OrderDtoMapper orderMapper,
			OrderRepository orderRepository,
			RequestContext requestContext) {
		this.orderMapper = orderMapper;
		this.orderRepository = orderRepository;
		this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
	}

	@Override
	public PageResultDto<OrderDto> list(String brandUuid, String memberUuid, int page, int pageSize, boolean includeInactive) throws OrderException {
		try {
			PageRequest pageRequest = PageRequest.of(page, pageSize);
			PageResult<OrderDto> result = includeInactive
					? orderRepository.findAllByBrandUuidAndMemberUuidAndDeletedIsFalse(brandUuid, memberUuid, pageRequest)
							.map(orderMapper::toDto)
					: orderRepository.findAllByBrandUuidAndMemberUuidAndDeletedIsFalseAndActiveIsTrue(brandUuid, memberUuid, pageRequest)
							.map(orderMapper::toDto);
			return PageResultDto.from(result);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}", brandUuid, e);
			throw new OrderException(requestContext.getTrackingNumber(), OrderException.FIND_FAILED, e);
		}
	}

	@Override
	public OrderDto findByUuid(String brandUuid, String memberUuid, String orderUuid) throws OrderException {
		try {
			Optional<Order> entity = orderRepository.findByBrandUuidAndMemberUuidAndUuidAndDeletedIsFalse(brandUuid, memberUuid, orderUuid);
			if (entity.isEmpty()) {
				throw new OrderException(requestContext.getTrackingNumber(), OrderException.ORDER_NOT_FOUND, "Order not found");
			}
			return orderMapper.toDto(entity.get());
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, orderUuid={}", brandUuid, orderUuid, e);

			if (e instanceof OrderException) {
				throw (OrderException) e;
			}
			throw new OrderException(requestContext.getTrackingNumber(), OrderException.FIND_FAILED, e);
		}
	}
}
