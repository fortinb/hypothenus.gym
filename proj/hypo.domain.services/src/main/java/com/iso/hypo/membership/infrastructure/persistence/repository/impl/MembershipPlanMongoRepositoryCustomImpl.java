package com.iso.hypo.membership.infrastructure.persistence.repository.impl;

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

import com.iso.hypo.membership.infrastructure.persistence.entity.MembershipPlanDocument;
import com.iso.hypo.membership.infrastructure.persistence.repository.MembershipPlanMongoRepositoryCustom;
import com.mongodb.client.result.UpdateResult;

public class MembershipPlanMongoRepositoryCustomImpl implements MembershipPlanMongoRepositoryCustom {
	private final MongoTemplate mongoTemplate;

	public MembershipPlanMongoRepositoryCustomImpl(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Override
	public long deleteAllByBrandUuid(String brandUuid, String deletedBy) {
		Query query = new Query(Criteria.where("brandUuid").is(brandUuid));

		Update update = new Update()
				.set("deleted", true)
				.set("deletedOn", Instant.now().truncatedTo(ChronoUnit.DAYS))
				.set("deletedBy", deletedBy);

		UpdateResult result = mongoTemplate.updateMulti(query, update, MembershipPlanDocument.class);

		return result.getMatchedCount();
		
	}

	@Override
	public long removeGymReferences(String brandUuid, String gymUuid) {
		Query query = new Query(Criteria.where("brandUuid").is(brandUuid)
				.and("includedGymUuids").in(gymUuid));

		Update update = new Update().pull("includedGymUuids", gymUuid);

		UpdateResult result = mongoTemplate.updateMulti(query, update, MembershipPlanDocument.class);

		return result.getModifiedCount();
	}

	@Override
	public long removeCourseReferences(String brandUuid, String courseUuid) {
		Query query = new Query(Criteria.where("brandUuid").is(brandUuid)
				.and("includedCourseUuids").in(courseUuid));

		Update update = new Update().pull("includedCourseUuids", courseUuid);

		UpdateResult result = mongoTemplate.updateMulti(query, update, MembershipPlanDocument.class);

		return result.getModifiedCount();
	}

	@Override
	public Page<MembershipPlanDocument> findActiveOnDate(String brandUuid, Date currentDate, Pageable pageable) {
		Criteria criteria = Criteria.where("brandUuid").is(brandUuid)
				.and("deleted").is(false)
				.and("active").is(true)
				.and("startDate").lte(currentDate)
				.andOperator(new Criteria().orOperator(
						Criteria.where("endDate").exists(false),
						Criteria.where("endDate").is(null),
						Criteria.where("endDate").gte(currentDate)));

		Query query = new Query(criteria).with(pageable);

		List<MembershipPlanDocument> results = mongoTemplate.find(query, MembershipPlanDocument.class);
		long total = mongoTemplate.count(new Query(criteria), MembershipPlanDocument.class);

		return new PageImpl<>(results, pageable, total);
	}
	
	@Override
	public void deleteAll() {
		mongoTemplate.remove(new Query(), MembershipPlanDocument.class);

	}
}