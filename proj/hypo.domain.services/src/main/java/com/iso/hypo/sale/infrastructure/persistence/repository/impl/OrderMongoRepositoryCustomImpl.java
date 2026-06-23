package com.iso.hypo.sale.infrastructure.persistence.repository.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.iso.hypo.sale.infrastructure.persistence.entity.OrderDocument;
import com.iso.hypo.sale.infrastructure.persistence.repository.OrderMongoRepositoryCustom;
import com.mongodb.client.result.UpdateResult;

public class OrderMongoRepositoryCustomImpl implements OrderMongoRepositoryCustom {

	private final MongoTemplate mongoTemplate;

	public OrderMongoRepositoryCustomImpl(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Override
	public long deleteAllByBrandUuid(String brandUuid, String deletedBy) {
		Query query = new Query(Criteria.where("brandUuid").is(brandUuid));

		Update update = new Update().set("deleted", true).set("deletedOn", Instant.now().truncatedTo(ChronoUnit.DAYS))
				.set("deletedBy", deletedBy);

		UpdateResult result = mongoTemplate.updateMulti(query, update, OrderDocument.class);

		return result.getMatchedCount();
	}

	@Override
	public long deleteAllByMemberUuid(String brandUuid, String memberUuid, String deletedBy) {
		Query query = new Query(Criteria.where("brandUuid").is(brandUuid)
				.and("memberUuid").is(memberUuid));

		Update update = new Update().set("deleted", true).set("deletedOn", Instant.now().truncatedTo(ChronoUnit.DAYS))
				.set("deletedBy", deletedBy);

		UpdateResult result = mongoTemplate.updateMulti(query, update, OrderDocument.class);

		return result.getMatchedCount();
	}

	@Override
	public void deleteAll() {
		mongoTemplate.remove(new Query(), OrderDocument.class);
	}
}
