package com.isoceles.hypothenus.gym.domain.repository.impl;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.isoceles.hypothenus.gym.domain.model.GymSearchResult;
import com.isoceles.hypothenus.gym.domain.repository.GymQueries;
import com.mongodb.client.AggregateIterable;
import static com.mongodb.client.model.Aggregates.limit;
import static com.mongodb.client.model.Aggregates.project;
import com.mongodb.client.MongoCollection;

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

//		[
//		  {
//		    $search: {
//		      index: "Gym_SearchIndex",
//		      compound: {
//		        filter: [
//		          {
//		            equals: {
//		              value: false,
//		              path: "isDeleted",
//		            },
//		          },
//		        ],
//		        must: [
//		          {
//		            compound: {
//		              should: [
//		                {
//		                  autocomplete: {
//		                    query: "Bednar",
//		                    path: "name",
//		                  },
//		                },
//		              ],
//		            },
//		          },
//		        ],
//		      },
//		    },
//		  },
//		]

		Document searchStage = new Document().append("$search", new Document()
				.append("index", indexName)
				.append("compound", new Document()
						.append("filter",
								Arrays.asList(new Document()
										.append("equals",
												new Document()
													.append("value", false)
													.append("path", "isDeleted"))))
						.append("must",
								new Document().append("compound", new Document()
										.append("should",
												Arrays.asList(
														new Document("autocomplete",
																new Document()
																	.append("query", criteria)
																	.append("path", "name")),
														new Document("autocomplete",
																new Document()
																	.append("query", criteria)
																	.append("path", "email")),
														new Document("autocomplete",
																new Document()
																	.append("query", criteria)
																	.append("path", "address.streetName")),
														new Document("autocomplete",
																new Document()
																	.append("query", criteria)
																	.append("path", "address.city")),
														new Document("autocomplete",
																new Document()
																	.append("query", criteria)
																	.append("path", "address.state")),	
														new Document("autocomplete",
																new Document()
																	.append("query", criteria)
																	.append("path", "address.zipCode"))														
												)))))
				.append("returnStoredSource", true));
		//String query = searchStage.toJson();
		// Create a pipeline that searches, projects, and limits the number of results returned.
		AggregateIterable<GymSearchResult> aggregationResults = collection.aggregate(
				Arrays.asList(searchStage,
						project(fields(excludeId(), include("gymId", "name", "address"), metaSearchScore("score"),
								meta("scoreDetails", "searchScoreDetails"))),
						limit(searchLimit)),
				GymSearchResult.class);

		return StreamSupport.stream(aggregationResults.spliterator(), false).collect(Collectors.toList());
	}
}
