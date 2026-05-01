package com.iso.hypo.membership.infrastructure.persistence.repository.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.iso.hypo.membership.infrastructure.persistence.entity.MembershipDocument;
import com.iso.hypo.membership.infrastructure.persistence.repository.MembershipMongoRepositoryCustom;
import com.mongodb.client.result.UpdateResult;

public class MembershipMongoRepositoryCustomImpl implements MembershipMongoRepositoryCustom {
	private final MongoTemplate mongoTemplate;

	public MembershipMongoRepositoryCustomImpl(MongoTemplate mongoTemplate) {
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

		UpdateResult result = mongoTemplate.updateMulti(query, update, MembershipDocument.class);
		
		return result.getMatchedCount();
	}
	
	@Override
	public void deleteAll() {
		mongoTemplate.remove(new Query(), MembershipDocument.class);
	}
}


