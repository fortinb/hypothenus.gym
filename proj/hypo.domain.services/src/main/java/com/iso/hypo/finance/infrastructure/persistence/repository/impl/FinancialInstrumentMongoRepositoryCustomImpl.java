package com.iso.hypo.finance.infrastructure.persistence.repository.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.iso.hypo.finance.infrastructure.persistence.entity.FinancialInstrumentDocument;
import com.iso.hypo.finance.infrastructure.persistence.repository.FinancialInstrumentMongoRepositoryCustom;
import com.mongodb.client.result.UpdateResult;

public class FinancialInstrumentMongoRepositoryCustomImpl implements FinancialInstrumentMongoRepositoryCustom {
	private final MongoTemplate mongoTemplate;

    public FinancialInstrumentMongoRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

	@Override
	public long deleteAllByBrandUuid(String brandUuid, String deletedBy) {
		Query query = new Query(Criteria.where("brandUuid").is(brandUuid));

		Update update = new Update().set("deleted", true).set("deletedOn", Instant.now().truncatedTo(ChronoUnit.DAYS))
				.set("deletedBy", deletedBy);

		UpdateResult result = mongoTemplate.updateMulti(query, update, FinancialInstrumentDocument.class);

		return result.getMatchedCount();
	}
	
	@Override
	public long deleteAllByMemberUuid(String brandUuid, String memberUuid, String deletedBy) {
		Query query = new Query(Criteria.where("brandUuid").is(brandUuid)
							.and("memberUuid").is(memberUuid));

		Update update = new Update().set("deleted", true).set("deletedOn", Instant.now().truncatedTo(ChronoUnit.DAYS))
				.set("deletedBy", deletedBy);

		UpdateResult result = mongoTemplate.updateMulti(query, update, FinancialInstrumentDocument.class);

		return result.getMatchedCount();
	}

	@Override
	public void deleteAll() {
		   mongoTemplate.remove(new Query(), FinancialInstrumentDocument.class);
		
	}
}
