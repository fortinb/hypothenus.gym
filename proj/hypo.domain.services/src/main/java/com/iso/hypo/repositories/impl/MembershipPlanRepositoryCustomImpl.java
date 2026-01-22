package com.iso.hypo.repositories.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.iso.hypo.domain.aggregate.MembershipPlan;
import com.iso.hypo.repositories.MembershipPlanRepositoryCustom;

public class MembershipPlanRepositoryCustomImpl implements MembershipPlanRepositoryCustom {
	private final MongoTemplate mongoTemplate;

	public MembershipPlanRepositoryCustomImpl(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Override
	public Optional<MembershipPlan> activate(String brandUuid, String membershipPlanUuid) {

		Query query = new Query(
	            Criteria.where("brandUuid").is(brandUuid)
	            		  .and("uuid").is(membershipPlanUuid));
		
		Update update = new Update()
					.set("isActive", true)
					.set("activatedOn", Instant.now().truncatedTo(ChronoUnit.DAYS))
					.set("deactivatedOn", null);

		MembershipPlan entity = mongoTemplate.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), MembershipPlan.class);
		return entity == null ? Optional.empty() : Optional.of(entity);
	}

	@Override
	public Optional<MembershipPlan> deactivate(String brandUuid, String membershipPlanUuid) {
		Query query = new Query(
	            Criteria.where("brandUuid").is(brandUuid)
	            		  .and("uuid").is(membershipPlanUuid));
		
		Update update = new Update()
					.set("isActive", false)
					.set("deactivatedOn", Instant.now().truncatedTo(ChronoUnit.DAYS));

		MembershipPlan entity = mongoTemplate.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), MembershipPlan.class);
		return entity == null ? Optional.empty() : Optional.of(entity);
	}
}


