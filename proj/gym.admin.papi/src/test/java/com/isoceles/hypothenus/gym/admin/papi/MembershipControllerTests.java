package com.isoceles.hypothenus.gym.admin.papi;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Assertions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.isoceles.hypothenus.gym.admin.papi.dto.LocalizedStringDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.model.MembershipDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.model.MemberDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.patch.PatchMembershipDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.post.PostMembershipDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.post.PostMembershipPlanDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.pricing.OneTimeFeeDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.put.PutMembershipDto;
import com.isoceles.hypothenus.gym.domain.model.aggregate.Membership;
import com.isoceles.hypothenus.gym.domain.model.aggregate.Member;
import com.isoceles.hypothenus.gym.domain.model.aggregate.MembershipPlan;
import com.isoceles.hypothenus.gym.domain.model.contact.Person;
import com.isoceles.hypothenus.gym.domain.model.enumeration.LanguageEnum;
import com.isoceles.hypothenus.gym.domain.repository.MembershipRepository;
import com.isoceles.hypothenus.gym.domain.repository.MembershipPlanRepository;
import com.isoceles.hypothenus.tests.http.HttpUtils;
import com.isoceles.hypothenus.tests.model.MembershipPlanBuilder;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
class MembershipControllerTests {

	public static final String listURI = "/v1/brands/%s/memberships";
	public static final String postURI = "/v1/brands/%s/memberships";
	public static final String getURI = "/v1/brands/%s/memberships/%s";
	public static final String putURI = "/v1/brands/%s/memberships/%s";
	public static final String postActivateURI = "/v1/brands/%s/memberships/%s/activate";
	public static final String postDeactivateURI = "/v1/brands/%s/memberships/%s/deactivate";
	public static final String patchURI = "/v1/brands/%s/memberships/%s";
	public static final String pageNumber = "page";
	public static final String pageSize = "pageSize";
	public static final String includeInactive = "includeInactive";

	public static final String brandId_FitnessBoxing = "FitnessBoxing";
	public static final String brandId_CrossfitExtreme= "CrossfitExtreme";
	
	@LocalServerPort
	private int port;

	@Autowired
	MembershipRepository membershipRepository;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	ModelMapper modelMapper;

	private Faker faker = new Faker();

	private TestRestTemplate restTemplate = new TestRestTemplate();

	private Membership membership;
	private Membership membershipIsDeleted;
	private List<Membership> memberships = new ArrayList<Membership>();

	@BeforeAll
	void arrange() {
		restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());

		membershipRepository.deleteAll();

		MembershipPlan plan = MembershipPlanBuilder.build(brandId_FitnessBoxing);

		// create a member
		Person person = new Person(faker.name().firstName(), faker.name().lastName(), null, faker.internet().emailAddress(), null, null, null, null, LanguageEnum.en, null);
		Member member = new Member(brandId_FitnessBoxing, person, true, Instant.now(), null);

		membership = new Membership(brandId_FitnessBoxing, member, plan, true, false, true, Instant.now(), null);
		membershipRepository.save(membership);

		membershipIsDeleted = new Membership(brandId_FitnessBoxing, member, plan, true, false, true, Instant.now(), null);
		membershipIsDeleted.setDeleted(true);
		membershipIsDeleted = membershipRepository.save(membershipIsDeleted);

		for (int i = 0; i < 10; i++) {
			MembershipPlan p = MembershipPlanBuilder.build(brandId_FitnessBoxing);
			Person per = new Person(faker.name().firstName(), faker.name().lastName(), null, faker.internet().emailAddress(), null, null, null, null, LanguageEnum.en, null);
			Member mem = new Member(brandId_FitnessBoxing, per, true, Instant.now(), null);
			Membership item = new Membership(brandId_FitnessBoxing, mem, p, true, false, true, Instant.now(), null);
			membershipRepository.save(item);
			memberships.add(item);
		}

		for (int i = 0; i < 4; i++) {
			MembershipPlan p = MembershipPlanBuilder.build(brandId_CrossfitExtreme);
			Person per = new Person(faker.name().firstName(), faker.name().lastName(), null, faker.internet().emailAddress(), null, null, null, null, LanguageEnum.en, null);
			Member mem = new Member(brandId_CrossfitExtreme, per, true, Instant.now(), null);
			Membership item = new Membership(brandId_CrossfitExtreme, mem, p, true, false, true, Instant.now(), null);
			membershipRepository.save(item);
			memberships.add(item);
		}

		MembershipPlan p = MembershipPlanBuilder.build(brandId_CrossfitExtreme);
		//Person person = new Person(faker.name().firstName(), faker.name().lastName(), null, faker.internet().emailAddress(), null, null, null, null, LanguageEnum.en, null);
		Member m = new Member(brandId_CrossfitExtreme, person, false, Instant.now(), null);
		Membership item = new Membership(brandId_CrossfitExtreme, m, p, true, false, false, Instant.now(), null);
		membershipRepository.save(item);
	}

	@AfterAll
	void cleanup() {
		// optional cleanup
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testListActiveSuccess(String role, String user)
			throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		HttpEntity<String> httpEntity = HttpUtils.createHttpEntity(role, user, null);

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "0");
		params.add(pageSize, "5");
		params.add(includeInactive, "false");

		// Act
		ResponseEntity<String> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(listURI, brandId_CrossfitExtreme)), port, params), HttpMethod.GET,
				httpEntity, new ParameterizedTypeReference<String>() {
			});

		// Assert
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		Page<MembershipDto> page = objectMapper.readValue(response.getBody(), new TypeReference<Page<MembershipDto>>() {
		});

		// Assert
		Assertions.assertEquals(0, page.getPageable().getPageNumber(),
				String.format("Membership list first page number invalid: %d", page.getPageable().getPageNumber()));
		Assertions.assertEquals(4, page.getNumberOfElements(),
				String.format("Membership list first page number of elements invalid: %d", page.getNumberOfElements()));
		Assertions.assertEquals(4, page.getTotalElements(),
				String.format("Membership total number of elements invalid: %d", page.getTotalElements()));

		page.get().forEach(membership -> Assertions.assertTrue(membership.isActive()));
		page.get().forEach(membership -> Assertions.assertTrue(membership.isDeleted() == false));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testListFirstPageSuccess(String role, String user)
			throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		HttpEntity<String> httpEntity = HttpUtils.createHttpEntity(role, user, null);

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "0");
		params.add(pageSize, "5");
		params.add(includeInactive, "true");

		// Act
		ResponseEntity<String> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(listURI, brandId_CrossfitExtreme)), port, params), HttpMethod.GET,
				httpEntity, new ParameterizedTypeReference<String>() {
			});

		// Assert
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		Page<MembershipDto> page = objectMapper.readValue(response.getBody(), new TypeReference<Page<MembershipDto>>() {
		});

		// Assert
		Assertions.assertEquals(0, page.getPageable().getPageNumber(),
				String.format("Membership list first page number invalid: %d", page.getPageable().getPageNumber()));
		Assertions.assertEquals(5, page.getNumberOfElements(),
				String.format("Membership list first page number of elements invalid: %d", page.getNumberOfElements()));
		Assertions.assertEquals(5, page.getTotalElements(),
				String.format("Membership total number of elements invalid: %d", page.getTotalElements()));

		page.get().forEach(membership -> Assertions.assertTrue(membership.isDeleted() == false));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testListSecondPageSuccess(String role, String user)
			throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		HttpEntity<String> httpEntity = HttpUtils.createHttpEntity(role, user, "");

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "1");
		params.add(pageSize, "2");

		// Act
		ResponseEntity<String> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(listURI, brandId_CrossfitExtreme)), port, params), HttpMethod.GET,
				httpEntity, new ParameterizedTypeReference<String>() {
			});

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		Page<MembershipDto> page = objectMapper.readValue(response.getBody(), new TypeReference<Page<MembershipDto>>() {
		});

		// Assert
		Assertions.assertEquals(1, page.getPageable().getPageNumber(),
				String.format("Membership list second page number invalid: %d", page.getPageable().getPageNumber()));
		Assertions.assertEquals(2, page.getNumberOfElements(),
				String.format("Membership list second page number of elements invalid: %d", page.getNumberOfElements()));
		
		page.get().forEach(membership -> Assertions.assertTrue(membership.isActive()));
		page.get().forEach(membership -> Assertions.assertTrue(membership.isDeleted() == false));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPostSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		MembershipPlan plan = MembershipPlanBuilder.build(brandId_FitnessBoxing);
		Person person = new Person(faker.name().firstName(), faker.name().lastName(), null, faker.internet().emailAddress(), null, null, null, null, LanguageEnum.en, null);
		Member member = new Member(brandId_FitnessBoxing, person, true, Instant.now(), null);

		PostMembershipDto postMembership = new PostMembershipDto();
		postMembership.setBrandId(brandId_FitnessBoxing);
		postMembership.setMembershipPlan(modelMapper.map(plan, com.isoceles.hypothenus.gym.admin.papi.dto.model.MembershipPlanDto.class));
		postMembership.setMember(modelMapper.map(member, MemberDto.class));

		HttpEntity<PostMembershipDto> httpEntity = HttpUtils.createHttpEntity(role, user, postMembership);

		// Act
		ResponseEntity<MembershipDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postURI, brandId_FitnessBoxing)), port, null), HttpMethod.POST,
				httpEntity, MembershipDto.class);

			String.format("Post error: %s", response.getStatusCode());

		assertMembership(modelMapper.map(postMembership, MembershipDto.class), response.getBody());
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testGetSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		MembershipPlan plan = MembershipPlanBuilder.build(brandId_FitnessBoxing);
		Person person = new Person(faker.name().firstName(), faker.name().lastName(), null, faker.internet().emailAddress(), null, null, null, null, LanguageEnum.en, null);
		Member member = new Member(brandId_FitnessBoxing, person, true, Instant.now(), null);

		PostMembershipDto postMembership = new PostMembershipDto();
		postMembership.setBrandId(brandId_FitnessBoxing);
		postMembership.setMembershipPlan(modelMapper.map(plan, com.isoceles.hypothenus.gym.admin.papi.dto.model.MembershipPlanDto.class));
		postMembership.setMember(modelMapper.map(member, MemberDto.class));

		HttpEntity<PostMembershipDto> httpEntity = HttpUtils.createHttpEntity(role, user, postMembership);

		ResponseEntity<MembershipDto> responsePost = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postURI, brandId_FitnessBoxing)), port, null), HttpMethod.POST,
				httpEntity, MembershipDto.class);

		Assertions.assertEquals(HttpStatus.CREATED, responsePost.getStatusCode(),
				String.format("Post error: %s", responsePost.getStatusCode()));

		// Act
		httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<MembershipDto> response = restTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(getURI, brandId_FitnessBoxing, responsePost.getBody().getId())), port, null),
				HttpMethod.GET, httpEntity, MembershipDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));

		assertMembership(modelMapper.map(postMembership, MembershipDto.class), response.getBody());
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPutSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		MembershipPlan plan = MembershipPlanBuilder.build(brandId_FitnessBoxing);
		Person person = new Person(faker.name().firstName(), faker.name().lastName(), null, faker.internet().emailAddress(), null, null, null, null, LanguageEnum.en, null);
		Member member = new Member(brandId_FitnessBoxing, person, true, Instant.now(), null);

		Membership membershipToUpdate = new Membership(brandId_FitnessBoxing, member, plan, false, false, false, null, null);
		membershipToUpdate = membershipRepository.save(membershipToUpdate);

		Membership updatedMembership = new Membership(brandId_FitnessBoxing, member, plan, false, false, false, null, null);
		updatedMembership.setId(membershipToUpdate.getId());

		PutMembershipDto putMembership = modelMapper.map(updatedMembership, PutMembershipDto.class);
		putMembership.setId(membershipToUpdate.getId());

		// Act
		HttpEntity<PutMembershipDto> httpEntity = HttpUtils.createHttpEntity(role, user, putMembership);
		ResponseEntity<MembershipDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, brandId_FitnessBoxing, putMembership.getId())), port, null),
				HttpMethod.PUT, httpEntity, MembershipDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put error: %s", response.getStatusCode()));

		assertMembership(modelMapper.map(updatedMembership, MembershipDto.class), response.getBody());
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPutNullSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		MembershipPlan plan = MembershipPlanBuilder.build(brandId_FitnessBoxing);
		Person person = new Person(faker.name().firstName(), faker.name().lastName(), null, faker.internet().emailAddress(), null, null, null, null, LanguageEnum.en, null);
		Member member = new Member(brandId_FitnessBoxing, person, true, Instant.now(), null);

		Membership membershipToUpdate = new Membership(brandId_FitnessBoxing, member, plan, false, false, false, null, null);
		membershipToUpdate = membershipRepository.save(membershipToUpdate);

		Membership updatedMembership = new Membership(brandId_FitnessBoxing, member, plan, false, false, false, null, null);
		updatedMembership.setId(membershipToUpdate.getId());

		PutMembershipDto putMembership = modelMapper.map(updatedMembership, PutMembershipDto.class);
		putMembership.setId(membershipToUpdate.getId());
		putMembership.setMembershipPlan(null);
		putMembership.setMember(null);

		// Act
		HttpEntity<PutMembershipDto> httpEntity = HttpUtils.createHttpEntity(role, user, putMembership);
		ResponseEntity<MembershipDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, brandId_FitnessBoxing, putMembership.getId())), port, null),
				HttpMethod.PUT, httpEntity, MembershipDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put null error: %s", response.getStatusCode()));

		updatedMembership.setMembershipPlan(null);
		updatedMembership.setMember(null);

		assertMembership(modelMapper.map(updatedMembership, MembershipDto.class), response.getBody());
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testActivateSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Membership membershipToActivate = new Membership(brandId_FitnessBoxing, new Member(brandId_FitnessBoxing, new Person(faker.name().firstName(), faker.name().lastName(), null, faker.internet().emailAddress(), null, null, null, null, LanguageEnum.en, null), true, null, null), MembershipPlanBuilder.build(brandId_FitnessBoxing), true, false, false, null, null);
		membershipToActivate = membershipRepository.save(membershipToActivate);

		membershipToActivate.setActive(true);
		membershipToActivate.setActivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		membershipToActivate.setDeactivatedOn(null);

		// Act
		HttpEntity<PutMembershipDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<MembershipDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postActivateURI, brandId_FitnessBoxing, membershipToActivate.getId())),
					port, null),
				HttpMethod.POST, httpEntity, MembershipDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Membership activation error: %s", response.getStatusCode()));

		assertMembership(modelMapper.map(membershipToActivate, MembershipDto.class), response.getBody());
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testActivateFailure(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange

		// Act
		HttpEntity<PutMembershipDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<Object> response = restTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(postActivateURI, brandId_FitnessBoxing, faker.code().isbn10())), port, null), HttpMethod.POST, httpEntity,
				Object.class);
						
		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Membership activation error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testDeactivateSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Membership membershipToDeactivate = new Membership(brandId_FitnessBoxing, new Member(brandId_FitnessBoxing, new Person(faker.name().firstName(), faker.name().lastName(), null, faker.internet().emailAddress(), null, null, null, null, LanguageEnum.en, null), true, Instant.now().truncatedTo(ChronoUnit.DAYS), null), MembershipPlanBuilder.build(brandId_FitnessBoxing), true, false, true, Instant.now().truncatedTo(ChronoUnit.DAYS), null);
		membershipToDeactivate = membershipRepository.save(membershipToDeactivate);

		membershipToDeactivate.setActive(false);
		membershipToDeactivate.setDeactivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));

		// Act
		HttpEntity<PutMembershipDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<MembershipDto> response = restTemplate.exchange(HttpUtils.createURL(
				URI.create(String.format(postDeactivateURI, brandId_FitnessBoxing, membershipToDeactivate.getId())), port, null), HttpMethod.POST, httpEntity,
				MembershipDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Membership deactivation error: %s", response.getStatusCode()));

		assertMembership(modelMapper.map(membershipToDeactivate, MembershipDto.class), response.getBody());
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testDeactivateFailure(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange

		// Act
		HttpEntity<PutMembershipDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<Object> response = restTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(postDeactivateURI, brandId_FitnessBoxing, faker.code().ean13())), port, null), HttpMethod.POST, httpEntity,
				Object.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Membership deactivation error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPatchSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Membership membershipToPatch = new Membership(brandId_FitnessBoxing, new Member(brandId_FitnessBoxing, new Person(faker.name().firstName(), faker.name().lastName(), null, faker.internet().emailAddress(), null, null, null, null, LanguageEnum.en, null), true, Instant.now(), null), MembershipPlanBuilder.build(brandId_FitnessBoxing), true, false, true, Instant.now(), null);
		membershipToPatch = membershipRepository.save(membershipToPatch);

		PatchMembershipDto patchMembership = modelMapper.map(membershipToPatch, PatchMembershipDto.class);
		patchMembership.setId(membershipToPatch.getId());
		patchMembership.setMembershipPlan(null);
		patchMembership.setMember(null);

		// Act
		HttpEntity<PatchMembershipDto> httpEntity = HttpUtils.createHttpEntity(role, user, patchMembership);
		ResponseEntity<MembershipDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(patchURI, brandId_FitnessBoxing, patchMembership.getId())), port, null), HttpMethod.PATCH, httpEntity,
				MembershipDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));

		membershipToPatch.setRemainingClasses(patchMembership.getRemainingClasses());

		assertMembership(modelMapper.map(membershipToPatch, MembershipDto.class), response.getBody());
	}

	public static final void assertMembership(MembershipDto expected, MembershipDto result) {
		if (expected.getId() != null) {
			Assertions.assertEquals(expected.getId(), result.getId());
		}
		
		Assertions.assertEquals(expected.getBrandId(), result.getBrandId());
		Assertions.assertEquals(expected.isActive(), result.isActive());

		if (expected.getActivatedOn() != null) {
			Assertions.assertNotNull(result.getActivatedOn());
			Assertions.assertTrue(expected.getActivatedOn().truncatedTo(ChronoUnit.DAYS)
					.equals(result.getActivatedOn().truncatedTo(ChronoUnit.DAYS)));
		}
		if (expected.getActivatedOn() == null) {
			Assertions.assertNull(result.getActivatedOn());
		}

		if (expected.getDeactivatedOn() != null) {
			Assertions.assertNotNull(result.getDeactivatedOn());
			Assertions.assertTrue(expected.getDeactivatedOn().truncatedTo(ChronoUnit.DAYS)
					.equals(result.getDeactivatedOn().truncatedTo(ChronoUnit.DAYS)));
		}
		if (expected.getDeactivatedOn() == null) {
			Assertions.assertNull(result.getDeactivatedOn());
		}

		// Validate membershipPlan if present
		if (expected.getMembershipPlan() != null) {
			Assertions.assertNotNull(result.getMembershipPlan());
			Assertions.assertEquals(expected.getMembershipPlan().getId(), result.getMembershipPlan().getId());
			Assertions.assertEquals(expected.getMembershipPlan().getBrandId(), result.getMembershipPlan().getBrandId());
		}

		// Validate member if present
		if (expected.getMember() != null) {
			Assertions.assertNotNull(result.getMember());
			Assertions.assertEquals(expected.getMember().getBrandId(), result.getMember().getBrandId());
			if (expected.getMember().getPerson() != null) {
				Assertions.assertNotNull(result.getMember().getPerson());
				Assertions.assertEquals(expected.getMember().getPerson().getFirstname(), result.getMember().getPerson().getFirstname());
				Assertions.assertEquals(expected.getMember().getPerson().getLastname(), result.getMember().getPerson().getLastname());
				Assertions.assertEquals(expected.getMember().getPerson().getEmail(), result.getMember().getPerson().getEmail());
			}
		}

		if (expected.getMembershipPlan() == null) {
			Assertions.assertNull(result.getMembershipPlan());
		}

		if (expected.getMember() == null) {
			Assertions.assertNull(result.getMember());
		}

		if (expected.getRemainingClasses() != null) {
			Assertions.assertEquals(expected.getRemainingClasses(), result.getRemainingClasses());
		}
	}
}
