package com.iso.hypo.coach.repository.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.iso.hypo.coach.domain.aggregate.Coach;
import com.iso.hypo.coach.repository.CoachRepositoryCustom;

public class CoachRepositoryCustomImpl implements CoachRepositoryCustom {
	private final MongoTemplate mongoTemplate;

	public CoachRepositoryCustomImpl(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Override
	public Optional<Coach> activate(String brandId, String gymId, String id) {
		
		Query query = new Query(
	            Criteria.where("brandId").is(brandId)
	            		  .and("gymId").is(gymId)
	            		  .and("_id").is(id));
		
		Update update = new Update()
					.set("isActive", true)
					.set("activatedOn", Instant.now().truncatedTo(ChronoUnit.DAYS))
					.set("deactivatedOn", null);

		Coach coach = mongoTemplate.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), Coach.class);
		return coach == null ? Optional.empty() : Optional.of(coach);
	}

	@Override
	public Optional<Coach> deactivate(String brandId, String gymId, String id) {
		Query query = new Query(
				  Criteria.where("brandId").is(brandId)
        		  .and("gymId").is(gymId)
        		  .and("_id").is(id));
		
		Update update = new Update()
					.set("isActive", false)
					.set("deactivatedOn", Instant.now().truncatedTo(ChronoUnit.DAYS));

		Coach coach = mongoTemplate.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), Coach.class);
		return coach == null ? Optional.empty() : Optional.of(coach);
	}
}
