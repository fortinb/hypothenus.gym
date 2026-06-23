package com.iso.hypo.sale.domain.repository;

import java.util.Optional;

import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;
import com.iso.hypo.sale.domain.model.Order;

public interface OrderRepository {

	Optional<Order> findByBrandUuidAndMemberUuidAndUuidAndDeletedIsFalse(String brandUuid, String memberUuid, String orderUuid);

	PageResult<Order> findAllByBrandUuidAndMemberUuidAndDeletedIsFalse(String brandUuid, String memberUuid, PageRequest pageRequest);

	PageResult<Order> findAllByBrandUuidAndMemberUuidAndDeletedIsFalseAndActiveIsTrue(String brandUuid, String memberUuid, PageRequest pageRequest);

	Order save(Order order);

	void delete(Order order);

	void deleteAll();

	long deleteAllByBrandUuid(String brandUuid, String deletedBy);

	long deleteAllByMemberUuid(String brandUuid, String memberUuid, String deletedBy);
}
