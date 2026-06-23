package com.iso.hypo.sale.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.iso.hypo.sale.infrastructure.persistence.entity.OrderDocument;

public interface OrderMongoRepository extends PagingAndSortingRepository<OrderDocument, String>, CrudRepository<OrderDocument, String>, OrderMongoRepositoryCustom {

	Optional<OrderDocument> findByBrandUuidAndMemberUuidAndUuidAndDeletedIsFalse(String brandUuid, String memberUuid, String orderUuid);

	Page<OrderDocument> findAllByBrandUuidAndMemberUuidAndDeletedIsFalse(String brandUuid, String memberUuid, Pageable pageable);

	Page<OrderDocument> findAllByBrandUuidAndMemberUuidAndDeletedIsFalseAndActiveIsTrue(String brandUuid, String memberUuid, Pageable pageable);
}
