package com.iso.hypo.model.repository.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.iso.hypo.model.aggregate.Membership;
import com.iso.hypo.model.repository.MembershipRepositoryCustom;

public class MembershipRepositoryCustomImpl implements MembershipRepositoryCustom {
	private final MongoTemplate mongoTemplate;

	public MembershipRepositoryCustomImpl(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Override
	public Optional<Membership> activate(String brandUuid, String membershipUuid) {

		Query query = new Query(
	            Criteria.where("brandUuid").is(brandUuid)
	            		  .and("uuid").is(membershipUuid));
		
		Update update = new Update()
				.set("isActive", true)
				.set("activatedOn", Instant.now().truncatedTo(ChronoUnit.DAYS))
				.set("deactivatedOn", null);

		Membership membership = mongoTemplate.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), Membership.class);
		return membership == null ? Optional.empty() : Optional.of(membership);
	}

	@Override
	public Optional<Membership> deactivate(String brandUuid, String membershipUuid) {
		Query query = new Query(
	            Criteria.where("brandUuid").is(brandUuid)
	            		  .and("uuid").is(membershipUuid));
		
		Update update = new Update()
				.set("isActive", false)
				.set("deactivatedOn", Instant.now().truncatedTo(ChronoUnit.DAYS));

		Membership membership = mongoTemplate.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), Membership.class);
		return membership == null ? Optional.empty() : Optional.of(membership);
	}
}