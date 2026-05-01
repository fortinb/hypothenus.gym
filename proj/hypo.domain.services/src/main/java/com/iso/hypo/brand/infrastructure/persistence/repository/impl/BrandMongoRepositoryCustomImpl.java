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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import com.iso.hypo.brand.application.dto.search.BrandSearchDto;
import com.iso.hypo.brand.infrastructure.persistence.entity.BrandDocument;
import com.iso.hypo.brand.infrastructure.persistence.repository.BrandMongoRepositoryCustom;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Sorts;

class BrandMongoRepositoryCustomImpl implements BrandMongoRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Value("${spring.data.mongodb.search.index.limit}")
    private int searchLimit;

    @Value("${spring.data.mongodb.search.index.brand.name}")
    private String indexName;

    public BrandMongoRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<BrandSearchDto> searchAutocomplete(String criteria, Pageable pageable, boolean includeInactive) {

        MongoCollection<Document> collection = mongoTemplate.getCollection("brand");

        ArrayList<Boolean> activeValues = new ArrayList<Boolean>();
        activeValues.add(true);
        if (includeInactive) {
            activeValues.add(false);
        }

        Document searchStage = new Document().append("$search", new Document()
                .append("index", indexName)
                .append("compound", new Document()
                        .append("filter",
                                Arrays.asList(new Document()
                                                    .append("equals",
                                                            new Document()
                                                                .append("value", false)
                                                                .append("path", "deleted")),
                                              new Document()
                                                    .append("in",
                                                            new Document()
                                                                .append("value", activeValues)
                                                                .append("path", "active"))))
                        .append("must",
                                new Document().append("compound", new Document()
                                        .append("should",
                                                Arrays.asList(
                                                        new Document("autocomplete", new Document().append("query", criteria).append("path", "code")),
                                                        new Document("autocomplete", new Document().append("query", criteria).append("path", "name")),
                                                        new Document("autocomplete", new Document().append("query", criteria).append("path", "email")),
                                                        new Document("autocomplete", new Document().append("query", criteria).append("path", "address.streetName")),
                                                        new Document("autocomplete", new Document().append("query", criteria).append("path", "address.city")),
                                                        new Document("text",         new Document().append("query", criteria).append("path", "address.state")),
                                                        new Document("autocomplete", new Document().append("query", criteria).append("path", "address.zipCode")))))))
                .append("returnStoredSource", true));

        AggregateIterable<BrandSearchDto> aggregationResults = collection.aggregate(
                Arrays.asList(searchStage,
                        project(fields(excludeId(), include("uuid", "code", "name", "address", "email", "active"),
                                metaSearchScore("score"),
                                meta("scoreDetails", "searchScoreDetails"))),
                        sort(Sorts.ascending("name")),
                        skip(pageable.getPageNumber() * pageable.getPageSize()),
                        limit(searchLimit)),
                BrandSearchDto.class);

        List<BrandSearchDto> searchResults = StreamSupport.stream(aggregationResults.spliterator(), false).collect(Collectors.toList());
        return new PageImpl<BrandSearchDto>(searchResults, pageable, searchResults.size());
    }

    @Override
    public void deleteAll() {
        mongoTemplate.remove(new Query(), BrandDocument.class);
    }
}
