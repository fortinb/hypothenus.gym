package com.isoceles.hypothenus.gym.domain.repository.impl;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.isoceles.hypothenus.gym.domain.model.GymSearchResult;
import com.isoceles.hypothenus.gym.domain.repository.GymQueries;
import com.mongodb.client.AggregateIterable;
import static com.mongodb.client.model.Aggregates.limit;
import static com.mongodb.client.model.Aggregates.project;
import static com.mongodb.client.model.Aggregates.search;
import com.mongodb.client.MongoCollection;

import static com.mongodb.client.model.search.SearchOperator.compound;
import static com.mongodb.client.model.search.SearchOperator.autocomplete;
import static com.mongodb.client.model.search.SearchOptions.searchOptions;

import static com.mongodb.client.model.search.SearchPath.fieldPath;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Projections.meta;
import static com.mongodb.client.model.Projections.metaSearchScore;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class GymQueriesImpl implements GymQueries {
	private final MongoTemplate mongoTemplate;

	@Value("${spring.data.mongodb.search.index.limit}")
	private int searchLimit;

	@Value("${spring.data.mongodb.search.index.name}")
	private String indexName;

	public GymQueriesImpl(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Override
	public List<GymSearchResult> searchAutocomplete(String criteria) {

		MongoCollection<Document> collection = mongoTemplate.getCollection("gym");

		//.fuzzy(FuzzySearchOptions.fuzzySearchOptions().maxEdits(1).prefixLength(0).maxExpansions(5))
		Bson searchStage = search(
				compound().should(Arrays.asList(
						autocomplete(fieldPath("address.city"), criteria),						
						autocomplete(fieldPath("address.state"), criteria),
						autocomplete(fieldPath("address.streetName"), criteria),
						autocomplete(fieldPath("address.zipCode"), criteria),
						autocomplete(fieldPath("email"), criteria), 
						autocomplete(fieldPath("name"), criteria))),
				searchOptions().index(indexName).returnStoredSource(true)
		);
		
		// Create a pipeline that searches, projects, and limits the number of results
		// returned.
		AggregateIterable<GymSearchResult> aggregationResults = collection 
				.aggregate(Arrays.asList(
						searchStage, 
						project(fields(excludeId(), include("gymId", "name"),
								metaSearchScore("score"), meta("scoreDetails", "searchScoreDetails"))),
						limit(searchLimit)), GymSearchResult.class);
	
		return StreamSupport
			    .stream(aggregationResults.spliterator(), false)
			    .collect(Collectors.toList());
	}
}
