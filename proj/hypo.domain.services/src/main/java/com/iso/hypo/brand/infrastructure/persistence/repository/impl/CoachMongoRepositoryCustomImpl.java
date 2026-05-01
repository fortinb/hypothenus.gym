package com.iso.hypo.brand.infrastructure.persistence.repository.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.iso.hypo.brand.infrastructure.persistence.entity.CoachDocument;
import com.iso.hypo.brand.infrastructure.persistence.repository.CoachMongoRepositoryCustom;
import com.mongodb.client.result.UpdateResult;

public class CoachMongoRepositoryCustomImpl implements CoachMongoRepositoryCustom {
	private final MongoTemplate mongoTemplate;

	public CoachMongoRepositoryCustomImpl(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Override
	public long deleteAllByBrandUuid(String brandUuid, String deletedBy) {
		Query query = new Query(
				 Criteria.where("brandUuid").is(brandUuid));
		
		Update update = new Update()
					.set("deleted", true)
					.set("deletedOn", Instant.now().truncatedTo(ChronoUnit.DAYS))
					.set("deletedBy", deletedBy);

		UpdateResult result = mongoTemplate.updateMulti(query, update, CoachDocument.class);
		
		return result.getMatchedCount();
	}

	@Override
	public void deleteAll() {
		   mongoTemplate.remove(new Query(), CoachDocument.class);
	}

}
