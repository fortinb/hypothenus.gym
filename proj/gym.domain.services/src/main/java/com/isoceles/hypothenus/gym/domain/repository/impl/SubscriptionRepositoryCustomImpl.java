package com.isoceles.hypothenus.gym.domain.repository.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.isoceles.hypothenus.gym.domain.model.aggregate.Subscription;
import com.isoceles.hypothenus.gym.domain.repository.SubscriptionRepositoryCustom;

public class SubscriptionRepositoryCustomImpl implements SubscriptionRepositoryCustom {
	private final MongoTemplate mongoTemplate;

	public SubscriptionRepositoryCustomImpl(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Override
	public Optional<Subscription> activate(String gymId, String id) {

		Query query = new Query(
	            Criteria.where("gymId").is(gymId)
	            		  .and("_id").is(id));
		
		Update update = new Update()
					.set("isActive", true)
					.set("activatedOn", Instant.now().truncatedTo(ChronoUnit.DAYS))
					.set("deactivatedOn", null);

		Subscription subscription = mongoTemplate.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), Subscription.class);
		return subscription == null ? Optional.empty() : Optional.of(subscription);
	}

	@Override
	public Optional<Subscription> deactivate(String gymId, String id) {
		Query query = new Query(
	            Criteria.where("gymId").is(gymId)
	            		  .and("_id").is(id));
		
		Update update = new Update()
					.set("isActive", false)
					.set("deactivatedOn", Instant.now().truncatedTo(ChronoUnit.DAYS));

		Subscription subscription = mongoTemplate.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), Subscription.class);
		return subscription == null ? Optional.empty() : Optional.of(subscription);
	}
}
