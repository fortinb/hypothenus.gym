package com.iso.hypo.brand.repository.impl;

import static com.mongodb.client.model.Aggregates.limit;
import static com.mongodb.client.model.Aggregates.sort;
import static com.mongodb.client.model.Aggregates.skip;
import static com.mongodb.client.model.Aggregates.project;
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

import com.iso.hypo.brand.dto.BrandSearchResult;
import com.iso.hypo.brand.domain.aggregate.Brand;
import com.iso.hypo.brand.repository.BrandRepositoryCustom;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Sorts;

public class BrandRepositoryCustomImpl implements BrandRepositoryCustom {
	private final MongoTemplate mongoTemplate;

	@Value("${spring.data.mongodb.search.index.limit}")
	private int searchLimit;

	@Value("${spring.data.mongodb.search.index.brand.name}")
	private String indexName;

	public BrandRepositoryCustomImpl(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Override
	public Page<BrandSearchResult> searchAutocomplete(String criteria, Pageable pageable, boolean includeInactive) {

		MongoCollection<Document> collection = mongoTemplate.getCollection("brand");

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
																	.append("path", "brandId")),
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
														new Document("text",
																new Document()
																	.append("query", criteria)
																	.append("path", "address.state")),	
														new Document("autocomplete",
																new Document()
																	.append("query", criteria)
																	.append("path", "address.zipCode"))														
												)))))
				.append("returnStoredSource", true));
		
		// String query = searchStage.toJson();
		// Create a pipeline that searches, projects, and limits the number of results returned.
		AggregateIterable<BrandSearchResult> aggregationResults = collection.aggregate(
				Arrays.asList(searchStage,
						project(fields(excludeId(), include("brandId", "name", "address", "email", "isActive"),
								metaSearchScore("score"),
								meta("scoreDetails", "searchScoreDetails"))),
						sort(Sorts.ascending("name")),
						skip(pageable.getPageNumber() * pageable.getPageSize()),
						limit(searchLimit)),
				BrandSearchResult.class);
		
		List<BrandSearchResult> searchResults = StreamSupport.stream(aggregationResults.spliterator(), false).collect(Collectors.toList());
		return new PageImpl<BrandSearchResult>(searchResults, pageable, searchResults.size());
	}
	
	@Override
	public Optional<Brand> activate(String brandId) {

		Query query = new Query(
	            Criteria.where("brandId").is(brandId));
		
		Update update = new Update()
					.set("isActive", true)
					.set("activatedOn", Instant.now().truncatedTo(ChronoUnit.DAYS))
					.set("deactivatedOn", null);

		Brand gym = mongoTemplate.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), Brand.class);
		return gym == null ? Optional.empty() : Optional.of(gym);
	}

	@Override
	public Optional<Brand> deactivate(String brandId) {
		Query query = new Query(
	            Criteria.where("brandId").is(brandId));
		
		Update update = new Update()
					.set("isActive", false)
					.set("deactivatedOn", Instant.now().truncatedTo(ChronoUnit.DAYS));

		Brand gym = mongoTemplate.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), Brand.class);
		return gym == null ? Optional.empty() : Optional.of(gym);
	}
	

}
