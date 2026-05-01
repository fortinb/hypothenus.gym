package com.iso.hypo.brand.infrastructure.persistence.repository.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.iso.hypo.brand.infrastructure.persistence.entity.CourseDocument;
import com.iso.hypo.brand.infrastructure.persistence.repository.CourseMongoRepositoryCustom;
import com.mongodb.client.result.UpdateResult;

public class CourseMongoRepositoryCustomImpl implements CourseMongoRepositoryCustom {
	private final MongoTemplate mongoTemplate;

	public CourseMongoRepositoryCustomImpl(MongoTemplate mongoTemplate) {
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

		UpdateResult result = mongoTemplate.updateMulti(query, update, CourseDocument.class);
		
		return result.getMatchedCount();
	}

	@Override
	public void deleteAll() {
	   mongoTemplate.remove(new Query(), CourseDocument.class);
	}
}