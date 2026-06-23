package com.iso.hypo.sale.infrastructure.persistence.adapter;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;
import com.iso.hypo.common.infrastructure.persistence.adapter.BaseAdapter;
import com.iso.hypo.sale.domain.model.Order;
import com.iso.hypo.sale.domain.repository.OrderRepository;
import com.iso.hypo.sale.infrastructure.persistence.entity.OrderDocument;
import com.iso.hypo.sale.infrastructure.persistence.mapper.OrderDocumentMapper;
import com.iso.hypo.sale.infrastructure.persistence.repository.OrderMongoRepository;

@Repository
public class OrderRepositoryAdapter extends BaseAdapter implements OrderRepository {

	private final OrderMongoRepository mongoRepository;
	private final OrderDocumentMapper orderMapper;

	public OrderRepositoryAdapter(OrderMongoRepository mongoRepository, OrderDocumentMapper orderMapper) {
		this.mongoRepository = mongoRepository;
		this.orderMapper = orderMapper;
	}

	@Override
	public long deleteAllByBrandUuid(String brandUuid, String deletedBy) {
		return mongoRepository.deleteAllByBrandUuid(brandUuid, deletedBy);
	}

	@Override
	public Optional<Order> findByBrandUuidAndMemberUuidAndUuidAndDeletedIsFalse(String brandUuid,
			String memberUuid, String orderUuid) {
		return mongoRepository.findByBrandUuidAndMemberUuidAndUuidAndDeletedIsFalse(brandUuid, memberUuid, orderUuid)
				.map(orderMapper::toEntity);
	}

	@Override
	public PageResult<Order> findAllByBrandUuidAndMemberUuidAndDeletedIsFalse(String brandUuid,
			String memberUuid, PageRequest pageRequest) {
		Page<OrderDocument> page = mongoRepository.findAllByBrandUuidAndMemberUuidAndDeletedIsFalse(brandUuid, memberUuid,
				toSpringPageable(pageRequest, Sort.by("createdOn").descending()));
		return toPageResult(page, pageRequest).map(orderMapper::toEntity);
	}

	@Override
	public PageResult<Order> findAllByBrandUuidAndMemberUuidAndDeletedIsFalseAndActiveIsTrue(
			String brandUuid, String memberUuid, PageRequest pageRequest) {
		Page<OrderDocument> page = mongoRepository.findAllByBrandUuidAndMemberUuidAndDeletedIsFalseAndActiveIsTrue(brandUuid, memberUuid,
				toSpringPageable(pageRequest, Sort.by("createdOn").descending()));
		return toPageResult(page, pageRequest).map(orderMapper::toEntity);
	}

	@Override
	public long deleteAllByMemberUuid(String brandUuid, String memberUuid, String deletedBy) {
		return mongoRepository.deleteAllByMemberUuid(brandUuid, memberUuid, deletedBy);
	}

	@Override
	public Order save(Order order) {
		OrderDocument document = orderMapper.toDocument(order);
		OrderDocument saved = mongoRepository.save(document);
		return orderMapper.toEntity(saved);
	}

	@Override
	public void delete(Order order) {
		mongoRepository.delete(orderMapper.toDocument(order));
	}

	@Override
	public void deleteAll() {
		mongoRepository.deleteAll();
	}
}
