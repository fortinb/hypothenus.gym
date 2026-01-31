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

import com.iso.hypo.domain.aggregate.Member;
import com.iso.hypo.domain.contact.PhoneNumber;
import com.iso.hypo.domain.dto.search.MemberSearchDto;
import com.iso.hypo.repositories.MemberRepositoryCustom;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.UpdateResult;

public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {
    private final MongoTemplate mongoTemplate;

	@Value("${spring.data.mongodb.search.index.limit}")
	private int searchLimit;

	@Value("${spring.data.mongodb.search.index.member.name}")
	private String indexName;


    public MemberRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<MemberSearchDto> searchAutocomplete(String criteria, Pageable pageable, boolean includeInactive) {
		MongoCollection<Document> collection = mongoTemplate.getCollection("member");

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
													.append("path", "person.firstname")),
											new Document("autocomplete",
												new Document()
													.append("query", criteria)
													.append("path", "person.lastname")),												
											new Document("autocomplete",
												new Document()
													.append("query", criteria)
													.append("path", "person.email")),
											new Document("autocomplete",
												new Document()
													.append("query", criteria)
													.append("path", "person.phoneNumbers.number")),
											new Document("autocomplete",
												new Document()
													.append("query", criteria)
													.append("path", "person.address.zipCode"))													
										)))))
				.append("returnStoredSource", true));
		
		// Execute aggregation and defensively map raw Documents to MemberSearchDto so schema mismatches don't drop results
		AggregateIterable<Document> aggregationResults = collection.aggregate(
				Arrays.asList(searchStage,
						project(fields(excludeId(), include("brandUuid","uuid", "person.firstname", "person.lastname", "person.email", "person.phoneNumbers", "person.address.zipCode", "isActive"),
								metaSearchScore("score"),
								meta("scoreDetails", "searchScoreDetails"))),
				sort(Sorts.ascending("lastname", "firstname")),
				skip(pageable.getPageNumber() * pageable.getPageSize()),
				limit(searchLimit)),
				Document.class);
		
		List<Document> documents = StreamSupport.stream(aggregationResults.spliterator(), false).collect(Collectors.toList());
		
		List<MemberSearchDto> searchResults = new ArrayList<>();
		for (Document doc : documents) {
			MemberSearchDto dto = new MemberSearchDto();

			dto.setBrandUuid(getStringSafe(doc, "brandUuid"));
			dto.setUuid(getStringSafe(doc, "uuid"));
			dto.setActive((Boolean) doc.get("isActive"));
			
			Object personObj = doc.get("person");
				Document personDoc = (Document) personObj;
				
				dto.setFirstname(getStringSafe(personDoc, "firstname"));
				dto.setLastname(getStringSafe(personDoc, "lastname"));
				dto.setEmail(getStringSafe(personDoc, "email"));

				Object pnObj = personDoc.get("phoneNumbers");
					List<?> pnList = (List<?>) pnObj;
					List<PhoneNumber> phoneNumbers = new ArrayList<>();
					for (Object p : pnList) {
						if (p instanceof Document) {
							Document pdoc = (Document) p;
							PhoneNumber phone = new PhoneNumber();
							phone.setNumber(getStringSafe(pdoc, "number"));
							phoneNumbers.add(phone);
						}
					}
					dto.setPhoneNumbers(phoneNumbers);

				Object addrObj = personDoc.get("address");
				dto.setZipcode(getStringSafe((Document) addrObj, "zipCode"));

				searchResults.add(dto);
		}
		
		return new PageImpl<MemberSearchDto>(searchResults, pageable, searchResults.size());
    }

    @Override
    public Optional<Member> activate(String brandUuid, String memberUuid) {

        Query query = new Query(
                Criteria.where("brandUuid").is(brandUuid)
                          .and("uuid").is(memberUuid));

        Update update = new Update()
                .set("isActive", true)
                .set("activatedOn", Instant.now().truncatedTo(ChronoUnit.DAYS))
                .set("deactivatedOn", null);

        Member member = mongoTemplate.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), Member.class);
        return member == null ? Optional.empty() : Optional.of(member);
    }

    @Override
    public Optional<Member> deactivate(String brandUuid, String memberUuid) {
        Query query = new Query(
                Criteria.where("brandUuid").is(brandUuid)
                          .and("uuid").is(memberUuid));

        Update update = new Update()
                .set("isActive", false)
                .set("deactivatedOn", Instant.now().truncatedTo(ChronoUnit.DAYS));

        Member member = mongoTemplate.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), Member.class);
        return member == null ? Optional.empty() : Optional.of(member);
    }

    @Override
    public void delete(String brandUuid, String memberUuid, String deletedBy) {
        Query query = new Query(
                 Criteria.where("brandUuid").is(brandUuid).and("uuid").is(memberUuid));

        Update update = new Update()
                    .set("isDeleted", true)
                    .set("deletedOn", Instant.now().truncatedTo(ChronoUnit.DAYS))
                    .set("deletedBy", deletedBy);

        mongoTemplate.updateFirst(query, update, Member.class);

    }

    @Override
    public long deleteAllByBrandUuid(String brandUuid, String deletedBy) {
        Query query = new Query(
                 Criteria.where("brandUuid").is(brandUuid));

        Update update = new Update()
                    .set("isDeleted", true)
                    .set("deletedOn", Instant.now().truncatedTo(ChronoUnit.DAYS))
                    .set("deletedBy", deletedBy);

        UpdateResult result = mongoTemplate.updateMulti(query, update, Member.class);

        return result.getMatchedCount();
    }

    // helper to safely read strings from Documents
    private String getStringSafe(Document doc, String key) {
        if (doc == null || key == null) return null;
        Object val = doc.get(key);
        return val == null ? null : val.toString();
    }
}
