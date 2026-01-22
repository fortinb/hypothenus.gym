package com.iso.hypo.repositories.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.iso.hypo.domain.aggregate.Course;
import com.iso.hypo.repositories.CourseRepositoryCustom;

public class CourseRepositoryCustomImpl implements CourseRepositoryCustom {
	private final MongoTemplate mongoTemplate;

	public CourseRepositoryCustomImpl(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Override
	public Optional<Course> activate(String brandUuid, String gymUuid, String courseUuid) {

		Query query = new Query(
	            Criteria.where("brandUuid").is(brandUuid)
	      		  .and("gymUuid").is(gymUuid)
	      		  .and("uuid").is(courseUuid));
		
		Update update = new Update()
					.set("isActive", true)
					.set("activatedOn", Instant.now().truncatedTo(ChronoUnit.DAYS))
					.set("deactivatedOn", null);

		Course course = mongoTemplate.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), Course.class);
		return course == null ? Optional.empty() : Optional.of(course);
	}

	@Override
	public Optional<Course> deactivate(String brandUuid, String gymUuid, String courseUuid) {
		Query query = new Query(
	            Criteria.where("brandUuid").is(brandUuid)
		      		  .and("gymUuid").is(gymUuid)
		      		  .and("uuid").is(courseUuid));
		
		Update update = new Update()
					.set("isActive", false)
					.set("deactivatedOn", Instant.now().truncatedTo(ChronoUnit.DAYS));

		Course course = mongoTemplate.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), Course.class);
		return course == null ? Optional.empty() : Optional.of(course);
	}
}


