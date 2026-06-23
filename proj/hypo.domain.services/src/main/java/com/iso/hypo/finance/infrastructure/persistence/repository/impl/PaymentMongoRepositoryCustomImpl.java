package com.iso.hypo.finance.infrastructure.persistence.repository.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.iso.hypo.finance.infrastructure.persistence.entity.PaymentDocument;
import com.iso.hypo.finance.infrastructure.persistence.repository.PaymentMongoRepositoryCustom;
import com.mongodb.client.result.UpdateResult;

public class PaymentMongoRepositoryCustomImpl implements PaymentMongoRepositoryCustom {
	private final MongoTemplate mongoTemplate;

    public PaymentMongoRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

	@Override
	public long deleteAllByBrandUuid(String brandUuid, String deletedBy) {
		Query query = new Query(Criteria.where("brandUuid").is(brandUuid));

		Update update = new Update().set("deleted", true).set("deletedOn", Instant.now().truncatedTo(ChronoUnit.DAYS))
				.set("deletedBy", deletedBy);

		UpdateResult result = mongoTemplate.updateMulti(query, update, PaymentDocument.class);

		return result.getMatchedCount();
	}
	
	@Override
	public long deleteAllByMemberUuid(String brandUuid, String memberUuid, String deletedBy) {
		Query query = new Query(Criteria.where("brandUuid").is(brandUuid)
							.and("memberUuid").is(memberUuid));

		Update update = new Update().set("deleted", true).set("deletedOn", Instant.now().truncatedTo(ChronoUnit.DAYS))
				.set("deletedBy", deletedBy);

		UpdateResult result = mongoTemplate.updateMulti(query, update, PaymentDocument.class);

		return result.getMatchedCount();
	}

	@Override
	public void deleteAll() {
		   mongoTemplate.remove(new Query(), PaymentDocument.class);
		
	}

	@Override
	public Page<PaymentDocument> findByDates(String brandUuid, Date startDate, Date endDate, Pageable pageable) {
		Criteria criteria = Criteria.where("brandUuid").is(brandUuid)
				.and("deleted").is(false)
				.and("active").is(true)
				.and("paidOn").gte(startDate)
				.and("paidOn").lte(endDate);

		Query query = new Query(criteria).with(pageable);

		List<PaymentDocument> results = mongoTemplate.find(query, PaymentDocument.class);
		long total = mongoTemplate.count(new Query(criteria), PaymentDocument.class);

		return new PageImpl<>(results, pageable, total);
	}
}
