package com.iso.hypo.brand.infrastructure.persistence.repository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.iso.hypo.brand.domain.model.Course;
import com.iso.hypo.brand.domain.repository.CourseRepositoryCustom;
import com.mongodb.client.result.UpdateResult;

public class CourseRepositoryCustomImpl implements CourseRepositoryCustom {
	private final MongoTemplate mongoTemplate;

	public CourseRepositoryCustomImpl(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Override
	public Optional<Course> activate(String brandUuid, String courseUuid) {
		Query query = new Query(
	            Criteria.where("brandUuid").is(brandUuid)
	      		  .and("uuid").is(courseUuid));
		
		Update update = new Update()
					.set("active", true)
					.set("activatedOn", Instant.now().truncatedTo(ChronoUnit.DAYS))
					.set("deactivatedOn", null);

		Course course = mongoTemplate.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), Course.class);
		return course == null ? Optional.empty() : Optional.of(course);
	}

	@Override
	public Optional<Course> deactivate(String brandUuid, String courseUuid) {
		Query query = new Query(
	            Criteria.where("brandUuid").is(brandUuid)
		      		  .and("uuid").is(courseUuid));
		
		Update update = new Update()
					.set("active", false)
					.set("deactivatedOn", Instant.now().truncatedTo(ChronoUnit.DAYS));

		Course course = mongoTemplate.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), Course.class);
		return course == null ? Optional.empty() : Optional.of(course);
	}
	
	@Override
	public void delete(String brandUuid, String courseUuid, String deletedBy) {
		Query query = new Query(
				 Criteria.where("brandUuid").is(brandUuid).and("uuid").is(courseUuid));
		
		Update update = new Update()
					.set("deleted", true)
					.set("deletedOn", Instant.now().truncatedTo(ChronoUnit.DAYS))
					.set("deletedBy", deletedBy);

		mongoTemplate.updateFirst(query, update, Course.class);
	}

	@Override
	public long deleteAllByBrandUuid(String brandUuid, String deletedBy) {
		Query query = new Query(
				 Criteria.where("brandUuid").is(brandUuid));
		
		Update update = new Update()
					.set("deleted", true)
					.set("deletedOn", Instant.now().truncatedTo(ChronoUnit.DAYS))
					.set("deletedBy", deletedBy);

		UpdateResult result = mongoTemplate.updateMulti(query, update, Course.class);
		
		return result.getMatchedCount();
	}
}