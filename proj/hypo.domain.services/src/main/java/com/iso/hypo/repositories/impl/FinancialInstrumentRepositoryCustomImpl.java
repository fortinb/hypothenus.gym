package com.iso.hypo.repositories.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.iso.hypo.domain.aggregate.FinancialInstrument;
import com.iso.hypo.domain.aggregate.Member;
import com.iso.hypo.repositories.FinancialInstrumentRepositoryCustom;
import com.mongodb.client.result.UpdateResult;

public class FinancialInstrumentRepositoryCustomImpl implements FinancialInstrumentRepositoryCustom {
	private final MongoTemplate mongoTemplate;

    public FinancialInstrumentRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
    
	@Override
	public Optional<FinancialInstrument> activate(String brandUuid, String memberUuid, String financialInstrumentUuid) {

		Query query = new Query(Criteria.where("brandUuid").is(brandUuid)
				.and("memberUuid").is(memberUuid)
				.and("uuid").is(financialInstrumentUuid));

		Update update = new Update().set("active", true).set("activatedOn", Instant.now().truncatedTo(ChronoUnit.DAYS))
				.set("deactivatedOn", null);

		FinancialInstrument financialInstrument = mongoTemplate.findAndModify(query, update,
				FindAndModifyOptions.options().returnNew(true), FinancialInstrument.class);
		return financialInstrument == null ? Optional.empty() : Optional.of(financialInstrument);
	}

	@Override
	public Optional<FinancialInstrument> deactivate(String brandUuid, String memberUuid,
			String financialInstrumentUuid) {
		Query query = new Query(Criteria.where("brandUuid").is(brandUuid)
				.and("memberUuid").is(memberUuid)
				.and("uuid").is(financialInstrumentUuid));

		Update update = new Update().set("active", false).set("deactivatedOn",
				Instant.now().truncatedTo(ChronoUnit.DAYS));

		FinancialInstrument financialInstrument = mongoTemplate.findAndModify(query, update,
				FindAndModifyOptions.options().returnNew(true), FinancialInstrument.class);
		return financialInstrument == null ? Optional.empty() : Optional.of(financialInstrument);
	}

	@Override
	public void delete(String brandUuid, String memberUuid, String financialInstrumentUuid, String deletedBy) {
		Query query = new Query(Criteria.where("brandUuid").is(brandUuid)
				.and("memberUuid").is(memberUuid)
				.and("uuid").is(financialInstrumentUuid));

		Update update = new Update().set("deleted", true).set("deletedOn", Instant.now().truncatedTo(ChronoUnit.DAYS))
				.set("deletedBy", deletedBy);

		mongoTemplate.updateFirst(query, update, Member.class);
	}

	@Override
	public long deleteAllByBrandUuid(String brandUuid, String deletedBy) {
		Query query = new Query(Criteria.where("brandUuid").is(brandUuid));

		Update update = new Update().set("deleted", true).set("deletedOn", Instant.now().truncatedTo(ChronoUnit.DAYS))
				.set("deletedBy", deletedBy);

		UpdateResult result = mongoTemplate.updateMulti(query, update, Member.class);

		return result.getMatchedCount();
	}
	
	@Override
	public long deleteAllByMemberUuid(String brandUuid, String memberUuid, String deletedBy) {
		Query query = new Query(Criteria.where("brandUuid").is(brandUuid)
							.and("memberUuid").is(memberUuid));

		Update update = new Update().set("deleted", true).set("deletedOn", Instant.now().truncatedTo(ChronoUnit.DAYS))
				.set("deletedBy", deletedBy);

		UpdateResult result = mongoTemplate.updateMulti(query, update, Member.class);

		return result.getMatchedCount();
	}
}
