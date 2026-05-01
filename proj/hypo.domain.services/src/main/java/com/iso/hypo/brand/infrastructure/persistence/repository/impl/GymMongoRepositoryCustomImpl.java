package com.iso.hypo.brand.infrastructure.persistence.repository.impl;

import static com.mongodb.client.model.Aggregates.limit;
import static com.mongodb.client.model.Aggregates.project;
import static com.mongodb.client.model.Aggregates.skip;
import static com.mongodb.client.model.Aggregates.sort;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Projections.meta;
import static com.mongodb.client.model.Projections.metaSearchScore;
//import static com.mongodb.client.model.search.SearchOperator.autocomplete;
//import static com.mongodb.client.model.search.SearchOperator.compound;
//import static com.mongodb.client.model.search.SearchOptions.searchOptions;
//import static com.mongodb.client.model.search.SearchPath.fieldPath;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.iso.hypo.brand.application.dto.search.GymSearchDto;
import com.iso.hypo.brand.domain.model.Gym;
import com.iso.hypo.brand.infrastructure.persistence.entity.CoachDocument;
import com.iso.hypo.brand.infrastructure.persistence.entity.GymDocument;
import com.iso.hypo.brand.infrastructure.persistence.repository.GymMongoRepositoryCustom;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.UpdateResult;

public class GymMongoRepositoryCustomImpl implements GymMongoRepositoryCustom {
	private final MongoTemplate mongoTemplate;

	@Value("${spring.data.mongodb.search.index.limit}")
	private int searchLimit;

	@Value("${spring.data.mongodb.search.index.gym.name}")
	private String indexName;

	public GymMongoRepositoryCustomImpl(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Override
	public Page<GymSearchDto> searchAutocomplete(String criteria, Pageable pageable, boolean includeInactive) {

		MongoCollection<Document> collection = mongoTemplate.getCollection("gym");

//		Bson searchStage = search(
//				compound()
//					.must(Arrays.asList(
//							compound()
//								.should(Arrays.asList(
//										autocomplete(fieldPath("address.city"), criteria),						
//										autocomplete(fieldPath("address.state"), criteria),
//										autocomplete(fieldPath("address.streetName"), criteria),
//										autocomplete(fieldPath("address.zipCode"), criteria),
//										autocomplete(fieldPath("email"), criteria), 
//										autocomplete(fieldPath("name"), criteria))))),
//				searchOptions().index(indexName).returnStoredSource(true)
//		);
		ArrayList<Boolean> activeValues = new ArrayList<Boolean>();
		activeValues.add(true);
		if (includeInactive) {
			activeValues.add(false);
		}

		Document searchStage = new Document().append("$search",
				new Document().append("index", indexName)
						.append("compound", new Document()
								.append("filter",
										Arrays.asList(
												new Document().append("equals",
														new Document().append("value", false).append("path",
																"deleted")),
												new Document().append("in",
														new Document()
																.append("value", activeValues)
																.append("path", "active"))))
								.append("must",
										new Document().append("compound",
												new Document().append("should",
														Arrays.asList(
																new Document("autocomplete",
																		new Document().append("query", criteria)
																				.append("path", "code")),
																new Document("autocomplete",
																		new Document().append("query", criteria)
																				.append("path", "name")),
																new Document("autocomplete",
																		new Document().append("query", criteria)
																				.append("path", "email")),
																new Document("autocomplete",
																		new Document().append("query", criteria)
																				.append("path", "address.streetName")),
																new Document("autocomplete",
																		new Document().append("query", criteria)
																				.append("path", "address.city")),
																new Document("text",
																		new Document().append("query", criteria)
																				.append("path", "address.state")),
																new Document("autocomplete",
																		new Document().append("query", criteria)
																				.append("path", "address.zipCode")))))))
						.append("returnStoredSource", true));

		// String query = searchStage.toJson();
		// Create a pipeline that searches, projects, and limits the number of results
		// returned.
		AggregateIterable<GymSearchDto> aggregationResults = collection.aggregate(Arrays.asList(searchStage,
				project(fields(excludeId(), include("brandUuid", "uuid", "code", "name", "address", "email", "active"),
						metaSearchScore("score"), meta("scoreDetails", "searchScoreDetails"))),
				sort(Sorts.ascending("name")), skip(pageable.getPageNumber() * pageable.getPageSize()),
				limit(searchLimit)), GymSearchDto.class);

		List<GymSearchDto> searchResults = StreamSupport.stream(aggregationResults.spliterator(), false)
				.collect(Collectors.toList());
		return new PageImpl<GymSearchDto>(searchResults, pageable, searchResults.size());
	}

	@Override
	public long deleteAllByBrandUuid(String brandUuid, String deletedBy) {
		Query query = new Query(Criteria.where("brandUuid").is(brandUuid));

		Update update = new Update().set("deleted", true).set("deletedOn", Instant.now().truncatedTo(ChronoUnit.DAYS))
				.set("deletedBy", deletedBy);

		UpdateResult result = mongoTemplate.updateMulti(query, update, GymDocument.class);

		return result.getMatchedCount();
	}

	@Override
	public long removeCoachReferences(String brandUuid, String coachUuid) {
		Query query = new Query(Criteria
					.where("brandUuid").is(brandUuid)
					.and("uuid").is(coachUuid));
		
		CoachDocument deletedCoach = mongoTemplate.findOne(query, CoachDocument.class);
		if (deletedCoach == null) {
			return 0;
		}
		
		ObjectId objectId = new ObjectId(deletedCoach.getId());

		query = new Query(Criteria.where("coachs.$id").is(objectId));

		Document dbRef = new Document("$ref", "coach").append("$id", objectId);
		Update update = new Update().pull("coachs", dbRef);

		UpdateResult result = mongoTemplate.updateMulti(query, update, Gym.class);

		return result.getModifiedCount();
	}

	@Override
	public void deleteAll() {
		mongoTemplate.remove(new Query(), GymDocument.class);
	}
}
