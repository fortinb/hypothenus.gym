package com.iso.hypo.repositories.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.iso.hypo.domain.aggregate.Coach;
import com.iso.hypo.repositories.CoachRepositoryCustom;
import com.mongodb.client.result.UpdateResult;

public class CoachRepositoryCustomImpl implements CoachRepositoryCustom {
	private final MongoTemplate mongoTemplate;

	public CoachRepositoryCustomImpl(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Override
	public Optional<Coach> activate(String brandUuid, String gymUuid, String coachUuid) {
		
		Query query = new Query(
	            Criteria.where("brandUuid").is(brandUuid)
	            		  .and("gymUuid").is(gymUuid)
	            		  .and("uuid").is(coachUuid));
		
		Update update = new Update()
					.set("isActive", true)
					.set("activatedOn", Instant.now().truncatedTo(ChronoUnit.DAYS))
					.set("deactivatedOn", null);

		Coach coach = mongoTemplate.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), Coach.class);
		return coach == null ? Optional.empty() : Optional.of(coach);
	}

	@Override
	public Optional<Coach> deactivate(String brandUuid, String gymUuid, String coachUuid) {
		Query query = new Query(
				  Criteria.where("brandUuid").is(brandUuid)
        		  .and("gymUuid").is(gymUuid)
        		  .and("uuid").is(coachUuid));
		
		Update update = new Update()
					.set("isActive", false)
					.set("deactivatedOn", Instant.now().truncatedTo(ChronoUnit.DAYS));

		Coach coach = mongoTemplate.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), Coach.class);
		return coach == null ? Optional.empty() : Optional.of(coach);
	}

	@Override
	public void delete(String brandUuid, String gymUuid, String coachUuid, String deletedBy) {
		Query query = new Query(
				 Criteria.where("brandUuid").is(brandUuid).and("gymUuid").is(gymUuid).and("uuid").is(coachUuid));
		
		Update update = new Update()
					.set("isDeleted", true)
					.set("deletedOn", Instant.now().truncatedTo(ChronoUnit.DAYS))
					.set("deletedBy", deletedBy);

		mongoTemplate.updateFirst(query, update, Coach.class);
	}

	@Override
	public long deleteAllByGymUuid(String brandUuid, String gymUuid, String deletedBy) {
		Query query = new Query(
				 Criteria.where("brandUuid").is(brandUuid).and("gymUuid").is(gymUuid));
		
		Update update = new Update()
					.set("isDeleted", true)
					.set("deletedOn", Instant.now().truncatedTo(ChronoUnit.DAYS))
					.set("deletedBy", deletedBy);

		UpdateResult result = mongoTemplate.updateMulti(query, update, Coach.class);
		
		return result.getMatchedCount();
	}

	@Override
	public long deleteAllByBrandUuid(String brandUuid, String deletedBy) {
		Query query = new Query(
				 Criteria.where("brandUuid").is(brandUuid));
		
		Update update = new Update()
					.set("isDeleted", true)
					.set("deletedOn", Instant.now().truncatedTo(ChronoUnit.DAYS))
					.set("deletedBy", deletedBy);

		UpdateResult result = mongoTemplate.updateMulti(query, update, Coach.class);
		
		return result.getMatchedCount();
	}

}


