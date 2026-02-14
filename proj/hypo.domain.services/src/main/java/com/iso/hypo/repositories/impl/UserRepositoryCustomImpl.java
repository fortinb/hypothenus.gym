package com.iso.hypo.repositories.impl;

import static com.mongodb.client.model.Aggregates.limit;
import static com.mongodb.client.model.Aggregates.project;
import static com.mongodb.client.model.Aggregates.skip;
import static com.mongodb.client.model.Aggregates.sort;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Projections.meta;
import static com.mongodb.client.model.Projections.metaSearchScore;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.iso.hypo.domain.aggregate.User;
import com.iso.hypo.domain.dto.search.UserSearchDto;
import com.iso.hypo.repositories.UserRepositoryCustom;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.UpdateResult;

public class UserRepositoryCustomImpl implements UserRepositoryCustom {
    private final MongoTemplate mongoTemplate;

	@Value("${spring.data.mongodb.search.index.limit}")
	private int searchLimit;

	@Value("${spring.data.mongodb.search.index.user.name}")
	private String indexName;
	
    public UserRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
	public Page<UserSearchDto> searchAutocomplete(String criteria, Pageable pageable, boolean includeInactive) {

		MongoCollection<Document> collection = mongoTemplate.getCollection("user");

		ArrayList<Boolean> isActiveValues = new ArrayList<Boolean>();
		isActiveValues.add(true);
		if (includeInactive) {
			isActiveValues.add(false);
		}
		
		Document searchStage = new Document().append("$search", new Document()
				.append("index", indexName)
				.append("compound", new Document()
						.append("filter",
								Arrays.asList(new Document()
													.append("equals",
															new Document()
																.append("value", false)
																.append("path", "isDeleted")),
											  new Document()
													.append("in",
															new Document()
																.append("value", isActiveValues)
																.append("path", "isActive")
																)))
						.append("must",
								new Document().append("compound", new Document()
										.append("should",
												Arrays.asList(
														new Document("autocomplete",
																new Document()
																	.append("query", criteria)
																	.append("path", "firstname")),
														new Document("autocomplete",
																new Document()
																	.append("query", criteria)
																	.append("path", "lastname")),
														new Document("autocomplete",
																new Document()
																	.append("query", criteria)
																	.append("path", "email"))
				
												)))))
				.append("returnStoredSource", true));
		
		// String query = searchStage.toJson();
		// Create a pipeline that searches, projects, and limits the number of results returned.
		AggregateIterable<UserSearchDto> aggregationResults = collection.aggregate(
				Arrays.asList(searchStage,
						project(fields(excludeId(), include("uuid", "firstname", "lastname","email", "isActive"),
								metaSearchScore("score"),
								meta("scoreDetails", "searchScoreDetails"))),
						sort(Sorts.ascending("name")),
						skip(pageable.getPageNumber() * pageable.getPageSize()),
						limit(searchLimit)),
				UserSearchDto.class);
		
		List<UserSearchDto> searchResults = StreamSupport.stream(aggregationResults.spliterator(), false).collect(Collectors.toList());
		return new PageImpl<UserSearchDto>(searchResults, pageable, searchResults.size());
	}
    
    @Override
    public Optional<User> activate(String userUuid) {
        Query query = new Query(
                Criteria.where("uuid").is(userUuid));

        Update update = new Update()
                .set("isActive", true)
                .set("activatedOn", Instant.now().truncatedTo(ChronoUnit.DAYS))
                .set("deactivatedOn", null);

        User user = mongoTemplate.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), User.class);
        return user == null ? Optional.empty() : Optional.of(user);
    }

    @Override
    public Optional<User> deactivate(String userUuid) {
    	Query query = new Query(
                Criteria.where("uuid").is(userUuid));

        Update update = new Update()
                .set("isActive", false)
                .set("deactivatedOn", Instant.now().truncatedTo(ChronoUnit.DAYS));

        User user = mongoTemplate.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), User.class);
        return user == null ? Optional.empty() : Optional.of(user);
    }

    @Override
    public void delete(String userUuid, String deletedBy) {
        Query query = new Query(
                 Criteria.where("uuid").is(userUuid));

        Update update = new Update()
                    .set("isDeleted", true)
                    .set("deletedOn", Instant.now().truncatedTo(ChronoUnit.DAYS))
                    .set("deletedBy", deletedBy);

        mongoTemplate.updateFirst(query, update, User.class);

    }

    @Override
    public long deleteAll(String deletedBy) {
        Query query = new Query();

        Update update = new Update()
                    .set("isDeleted", true)
                    .set("deletedOn", Instant.now().truncatedTo(ChronoUnit.DAYS))
                    .set("deletedBy", deletedBy);

        UpdateResult result = mongoTemplate.updateMulti(query, update, User.class);

        return result.getMatchedCount();
    }
}
