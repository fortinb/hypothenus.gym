package com.iso.hypo.sale.infrastructure.persistence.repository;

public interface OrderMongoRepositoryCustom {

	long deleteAllByBrandUuid(String brandUuid, String deletedBy);

	long deleteAllByMemberUuid(String brandUuid, String memberUuid, String deletedBy);

	void deleteAll();
}
