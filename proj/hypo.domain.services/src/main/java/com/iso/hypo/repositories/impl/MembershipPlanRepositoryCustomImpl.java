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
import com.mongodb.client.result.UpdateResult;

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

	@Override
	public void delete(String brandUuid, String membershipPlanUuid, String deletedBy) {
		Query query = new Query(
				 Criteria.where("brandUuid").is(brandUuid).and("uuid").is(membershipPlanUuid));
		
		Update update = new Update()
					.set("isDeleted", true)
					.set("deletedOn", Instant.now().truncatedTo(ChronoUnit.DAYS))
					.set("deletedBy", deletedBy);

		mongoTemplate.updateFirst(query, update, MembershipPlan.class);
	}

	@Override
	public long deleteAllByBrandUuid(String brandUuid, String deletedBy) {
		Query query = new Query(
				 Criteria.where("brandUuid").is(brandUuid));
		
		Update update = new Update()
					.set("isDeleted", true)
					.set("deletedOn", Instant.now().truncatedTo(ChronoUnit.DAYS))
					.set("deletedBy", deletedBy);

		UpdateResult result = mongoTemplate.updateMulti(query, update, MembershipPlan.class);
		
		return result.getMatchedCount();
	}
}


