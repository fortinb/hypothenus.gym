package com.iso.hypo.admin.papi;

import java.net.MalformedURLException;
import java.net.URI;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iso.hypo.admin.papi.dto.ErrorDto;
import com.iso.hypo.admin.papi.dto.LocalizedStringDto;
import com.iso.hypo.admin.papi.dto.model.MembershipPlanDto;
import com.iso.hypo.admin.papi.dto.patch.PatchMembershipPlanDto;
import com.iso.hypo.admin.papi.dto.post.PostMembershipPlanDto;
import com.iso.hypo.admin.papi.dto.put.PutCoachDto;
import com.iso.hypo.admin.papi.dto.put.PutCourseDto;
import com.iso.hypo.admin.papi.dto.put.PutGymDto;
import com.iso.hypo.admin.papi.dto.put.PutMembershipPlanDto;
import com.iso.hypo.brand.domain.model.Brand;
import com.iso.hypo.brand.domain.model.Course;
import com.iso.hypo.brand.domain.model.Gym;
import com.iso.hypo.brand.domain.repository.BrandRepository;
import com.iso.hypo.brand.domain.repository.CourseRepository;
import com.iso.hypo.brand.domain.repository.GymRepository;
import com.iso.hypo.common.application.dto.PageResultDto;
import com.iso.hypo.common.application.security.Roles;
import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;
import com.iso.hypo.domain.BrandBuilder;
import com.iso.hypo.domain.CourseBuilder;
import com.iso.hypo.domain.GymBuilder;
import com.iso.hypo.domain.MembershipPlanBuilder;
import com.iso.hypo.membership.application.exception.MembershipPlanException;
import com.iso.hypo.membership.domain.model.MembershipPlan;
import com.iso.hypo.membership.domain.repository.MembershipPlanRepository;
import com.iso.hypo.tests.http.HttpUtils;
import com.iso.hypo.tests.security.Users;
import com.iso.hypo.tests.utils.TestResponseUtils;

import net.datafaker.Faker;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "app.test.run=true")
@TestInstance(Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class MembershipPlanControllerTests {

	public static final String listURI = "/v1/brands/%s/membership/plans";
	public static final String listActiveURI = "/v1/brands/%s/membership/plans/active";
	public static final String postURI = "/v1/brands/%s/membership/plans";
	public static final String getURI = "/v1/brands/%s/membership/plans/%s";
	public static final String putURI = "/v1/brands/%s/membership/plans/%s";
	public static final String postActivateURI = "/v1/brands/%s/membership/plans/%s/activate";
	public static final String postDeactivateURI = "/v1/brands/%s/membership/plans/%s/deactivate";
	public static final String patchURI = "/v1/brands/%s/membership/plans/%s";
	public static final String deleteURI = "/v1/brands/%s/membership/plans/%s";

	public static final String deleteGymURI = "/v1/brands/%s/gyms/%s";
	public static final String deleteCourseURI = "/v1/brands/%s/courses/%s";

	public static final String pageNumber = "page";
	public static final String pageSize = "pageSize";
	public static final String includeInactive = "includeInactive";
	public static final String currentDateParam = "currentDate";

	public static final String brandCode_FitnessBoxing = "MPFitnessBoxing";
	public static final String brandCode_CrossfitExtreme = "MPCrossfitExtreme";
	public static final String brandCode_ActiveDateBrand = "MPActiveDateBrand";

	@LocalServerPort
	private int port;

	@Autowired
	BrandRepository brandRepository;

	@Autowired
	MembershipPlanRepository membershipPlanRepository;

	@Autowired
	CourseRepository courseRepository;

	@Autowired
	GymRepository gymRepository;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	ModelMapper modelMapper;

	private Faker faker = new Faker();

	private TestRestTemplate testRestTemplate = new TestRestTemplate();

	private MembershipPlan membershipPlan;
	private MembershipPlan membershipPlanDeleted;
	private Brand brand_FitnessBoxing;
	private Brand brand_CrossfitExtreme;

	private List<MembershipPlan> membershipPlans = new ArrayList<MembershipPlan>();
	private List<String> courseUuids = new ArrayList<>();
	private List<String> gymUuids = new ArrayList<>();

	@BeforeAll
	void arrange() {
		testRestTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
		membershipPlanRepository.deleteAll();

		brand_FitnessBoxing = BrandBuilder.build(brandCode_FitnessBoxing, "Fitness Boxing");
		brandRepository.save(brand_FitnessBoxing);

		brand_CrossfitExtreme = BrandBuilder.build(brandCode_CrossfitExtreme, "Crossfit Extreme");
		brandRepository.save(brand_CrossfitExtreme);

		for (int i = 0; i < 5; i++) {
			Course item = CourseBuilder.build(brand_FitnessBoxing.getUuid());
			item = courseRepository.save(item);
			courseUuids.add(item.getUuid());
		}

		for (int i = 0; i < 5; i++) {
			Gym item = GymBuilder.build(brand_FitnessBoxing.getUuid(), faker.code().isbn10(),
					faker.address().cityName(), null);
			item = gymRepository.save(item);
			gymUuids.add(item.getUuid());
		}

		membershipPlan = MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid(), gymUuids.subList(0, 2),
				courseUuids.subList(0, 2));
		membershipPlanRepository.save(membershipPlan);

		membershipPlanDeleted = MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid(), null, null);
		membershipPlanDeleted.setDeleted(true);
		membershipPlanDeleted = membershipPlanRepository.save(membershipPlanDeleted);

		for (int i = 0; i < 10; i++) {
			MembershipPlan item = MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid(), gymUuids, courseUuids);
			membershipPlanRepository.save(item);
			membershipPlans.add(item);
		}

		for (int i = 0; i < 4; i++) {
			MembershipPlan item = MembershipPlanBuilder.build(brand_CrossfitExtreme.getUuid(), null, null);
			membershipPlanRepository.save(item);
			membershipPlans.add(item);
		}

		MembershipPlan item = MembershipPlanBuilder.build(brand_CrossfitExtreme.getUuid(), null, null);
		item.setActive(false);
		membershipPlanRepository.save(item);
	}

	@AfterAll
	void cleanup() {
		// Cleanup
		// membershipPlanRepository.deleteAll();
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testListActiveSuccess(String role, String user)
			throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		HttpEntity<String> httpEntity = HttpUtils.createHttpEntity(role, user, null);

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "0");
		params.add(pageSize, "5");
		params.add(includeInactive, "false");

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(listURI, brand_CrossfitExtreme.getUuid())), port, params),
				HttpMethod.GET, httpEntity, JsonNode.class);

		// Assert
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		PageResultDto<MembershipPlanDto> page = TestResponseUtils.toPage(response,
				new TypeReference<PageResultDto<MembershipPlanDto>>() {
				}, objectMapper);

		// Assert
		Assertions.assertEquals(0, page.getPageNumber(),
				String.format("Membership Plan list first page number invalid: %d", page.getPageNumber()));
		Assertions.assertEquals(4, page.getContent().size(), String
				.format("Membership Plan list first page number of elements invalid: %d", page.getContent().size()));

		page.getContent().forEach(membershipPlan -> Assertions.assertTrue(membershipPlan.isActive()));
		page.getContent().forEach(membershipPlan -> Assertions.assertTrue(membershipPlan.isDeleted() == false));
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testListFirstPageSuccess(String role, String user)
			throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		HttpEntity<String> httpEntity = HttpUtils.createHttpEntity(role, user, null);

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "0");
		params.add(pageSize, "5");
		params.add(includeInactive, "true");

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(listURI, brand_CrossfitExtreme.getUuid())), port, params),
				HttpMethod.GET, httpEntity, JsonNode.class);

		// Assert
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		PageResultDto<MembershipPlanDto> page = TestResponseUtils.toPage(response,
				new TypeReference<PageResultDto<MembershipPlanDto>>() {
				}, objectMapper);

		// Assert
		Assertions.assertEquals(0, page.getPageNumber(),
				String.format("Membership Plan list first page number invalid: %d", page.getPageNumber()));
		Assertions.assertEquals(5, page.getContent().size(), String
				.format("Membership Plan list first page number of elements invalid: %d", page.getContent().size()));
		Assertions.assertEquals(5, page.getTotalElements(),
				String.format("Membership Plan total number of elements invalid: %d", page.getTotalElements()));

		page.getContent().forEach(membershipPlan -> Assertions.assertTrue(membershipPlan.isDeleted() == false));
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testListSecondPageSuccess(String role, String user)
			throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		HttpEntity<String> httpEntity = HttpUtils.createHttpEntity(role, user, "");

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "1");
		params.add(pageSize, "2");

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(listURI, brand_CrossfitExtreme.getUuid())), port, params),
				HttpMethod.GET, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		PageResultDto<MembershipPlanDto> page = TestResponseUtils.toPage(response,
				new TypeReference<PageResultDto<MembershipPlanDto>>() {
				}, objectMapper);

		// Assert
		Assertions.assertEquals(1, page.getPageNumber(),
				String.format("Membership Plan list second page number invalid: %d", page.getPageNumber()));
		Assertions.assertEquals(2, page.getContent().size(), String
				.format("Membership Plan list second page number of elements invalid: %d", page.getContent().size()));

		page.getContent().forEach(membershipPlan -> Assertions.assertTrue(membershipPlan.isActive()));
		page.getContent().forEach(membershipPlan -> Assertions.assertTrue(membershipPlan.isDeleted() == false));
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPostSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostMembershipPlanDto postDto = modelMapper.map(
				MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid(), gymUuids, courseUuids),
				PostMembershipPlanDto.class);

		HttpEntity<PostMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, postDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postURI, brand_FitnessBoxing.getUuid())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Create error: %s", response.getStatusCode()));

		MembershipPlanDto createdDto = TestResponseUtils.toDto(response, MembershipPlanDto.class, objectMapper);
		assertMembershipPlan(modelMapper.map(postDto, MembershipPlanDto.class), createdDto);
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPostFailureForbiddenBrandMismatch(String role, String user)
			throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostMembershipPlanDto postDto = modelMapper.map(
				MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid(), null, null), PostMembershipPlanDto.class);
		HttpEntity<PostMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, postDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postURI, faker.code().isbn10())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testGetSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostMembershipPlanDto postDto = modelMapper.map(
				MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid(), gymUuids, courseUuids),
				PostMembershipPlanDto.class);

		HttpEntity<PostMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, postDto);

		ResponseEntity<JsonNode> responsePost = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postURI, brand_FitnessBoxing.getUuid())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.CREATED, responsePost.getStatusCode(),
				String.format("Post error: %s", responsePost.getStatusCode()));

		// Act
		httpEntity = HttpUtils.createHttpEntity(role, user, null);
		MembershipPlanDto createdDto = TestResponseUtils.toDto(responsePost, MembershipPlanDto.class, objectMapper);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(
				URI.create(String.format(getURI, brand_FitnessBoxing.getUuid(), createdDto.getUuid())), port, null),
				HttpMethod.GET, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));

		MembershipPlanDto fetchedDto = TestResponseUtils.toDto(response, MembershipPlanDto.class, objectMapper);

		assertMembershipPlan(modelMapper.map(postDto, MembershipPlanDto.class), fetchedDto);
	}

	@Test
	void testGetFailureNotFound() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		HttpEntity<Object> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, null);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(
				URI.create(String.format(getURI, brand_FitnessBoxing.getUuid(), faker.code().isbn10())), port, null),
				HttpMethod.GET, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPutSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		MembershipPlan membershipPlanToUpdate = MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid(),
				gymUuids.subList(0, 2), courseUuids.subList(0, 2));
		membershipPlanToUpdate = membershipPlanRepository.save(membershipPlanToUpdate);

		PutMembershipPlanDto putDto = modelMapper.map(membershipPlanToUpdate, PutMembershipPlanDto.class);
		putDto.setUuid(membershipPlanToUpdate.getUuid());

		// Mutate mutable fields (keep uuid/code/dates)
		if (putDto.getName() != null && !putDto.getName().isEmpty()) {
			putDto.getName().get(0).setText(putDto.getName().get(0).getText() + " - updated");
		}
		if (putDto.getDescription() != null && !putDto.getDescription().isEmpty()) {
			putDto.getDescription().get(0).setText(putDto.getDescription().get(0).getText() + " - updated");
		}
		if (putDto.getIncludedCourseUuids() != null && putDto.getIncludedCourseUuids().size() > 0) {
			putDto.getIncludedCourseUuids().remove(0);
		}
		if (putDto.getIncludedGymUuids() != null && putDto.getIncludedGymUuids().size() > 0) {
			putDto.getIncludedGymUuids().remove(0);
		}

		// Act
		HttpEntity<PutMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, putDto);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, brand_FitnessBoxing.getUuid(), putDto.getUuid())),
						port, null),
				HttpMethod.PUT, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put error: %s", response.getStatusCode()));

		MembershipPlanDto updatedDto = TestResponseUtils.toDto(response, MembershipPlanDto.class, objectMapper);
		assertMembershipPlan(modelMapper.map(putDto, MembershipPlanDto.class), updatedDto);
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPutNullSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		MembershipPlan membershipPlanToUpdate = MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid(), null, null);
		membershipPlanToUpdate.setActive(false);
		membershipPlanToUpdate.setActivatedOn(null);
		membershipPlanToUpdate.setDeactivatedOn(null);
		membershipPlanToUpdate = membershipPlanRepository.save(membershipPlanToUpdate);

		PutMembershipPlanDto putDto = modelMapper.map(membershipPlanToUpdate, PutMembershipPlanDto.class);
		putDto.setDescription(null);
		putDto.setName(null);
		putDto.setTitle(null);

		membershipPlanToUpdate.setDescription(null);
		membershipPlanToUpdate.setName(null);
		membershipPlanToUpdate.setTitle(null);

		// Act
		HttpEntity<PutMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, putDto);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, brand_FitnessBoxing.getUuid(), putDto.getUuid())),
						port, null),
				HttpMethod.PUT, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put null error: %s", response.getStatusCode()));

		MembershipPlanDto updatedDto = TestResponseUtils.toDto(response, MembershipPlanDto.class, objectMapper);
		assertMembershipPlan(modelMapper.map(membershipPlanToUpdate, MembershipPlanDto.class), updatedDto);
	}

	@Test
	void testPutFailureNotFound() throws MalformedURLException, JsonProcessingException, Exception {
		PutMembershipPlanDto putDto = modelMapper.map(
				MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid(), null, null), PutMembershipPlanDto.class);
		HttpEntity<PutMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, putDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, brand_FitnessBoxing.getUuid(), putDto.getUuid())),
						port, null),
				HttpMethod.PUT, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPutFailureForbiddenBrandMismatch(String role, String user)
			throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PutMembershipPlanDto putDto = modelMapper.map(
				MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid(), null, null), PutMembershipPlanDto.class);
		HttpEntity<PutMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, putDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(putURI, faker.code().isbn10(), putDto.getUuid())), port, null),
				HttpMethod.PUT, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPutFailureForbiddenMembershipPlanMismatch(String role, String user)
			throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PutMembershipPlanDto putDto = modelMapper.map(
				MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid(), null, null), PutMembershipPlanDto.class);
		HttpEntity<PutMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, putDto);
		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(
				URI.create(String.format(putURI, brand_FitnessBoxing.getUuid(), faker.code().isbn10())), port, null),
				HttpMethod.PUT, httpEntity, JsonNode.class);
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testActivateSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		MembershipPlan membershipPlanToActivate = MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid(), null,
				null);
		membershipPlanToActivate.setActive(false);
		membershipPlanToActivate.setActivatedOn(null);
		membershipPlanToActivate.setDeactivatedOn(null);
		membershipPlanToActivate = membershipPlanRepository.save(membershipPlanToActivate);

		membershipPlanToActivate.setActive(true);
		membershipPlanToActivate.setActivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		membershipPlanToActivate.setDeactivatedOn(null);

		// Act
		HttpEntity<PutMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<JsonNode> response = testRestTemplate
				.exchange(
						HttpUtils.createURL(URI.create(String.format(postActivateURI, brand_FitnessBoxing.getUuid(),
								membershipPlanToActivate.getUuid())), port, null),
						HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Membership Plan activation error: %s", response.getStatusCode()));

		MembershipPlanDto activatedDto = TestResponseUtils.toDto(response, MembershipPlanDto.class, objectMapper);
		assertMembershipPlan(modelMapper.map(membershipPlanToActivate, MembershipPlanDto.class), activatedDto);
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testActivateFailureNotFound(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange

		// Act
		HttpEntity<PutMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(
				URI.create(String.format(postActivateURI, brand_FitnessBoxing.getUuid(), faker.code().isbn10())), port,
				null), HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Membership Plan activation error: %s", response.getStatusCode()));

		if (response.getBody() != null && response.getBody().size() > 0) {
			ErrorDto err = TestResponseUtils.toError(response, objectMapper);
			Assertions.assertEquals(MembershipPlanException.MEMBERSHIPPLAN_NOT_FOUND, err.getCode());
		}
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testDeactivateSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		MembershipPlan membershipPlanToDeactivate = MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid(), null,
				null);
		membershipPlanToDeactivate.setActive(true);
		membershipPlanToDeactivate.setActivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		membershipPlanToDeactivate = membershipPlanRepository.save(membershipPlanToDeactivate);

		membershipPlanToDeactivate.setActive(false);
		membershipPlanToDeactivate.setDeactivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));

		// Act
		HttpEntity<PutMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postDeactivateURI, brand_FitnessBoxing.getUuid(),
						membershipPlanToDeactivate.getUuid())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Membership Plan deactivation error: %s", response.getStatusCode()));

		MembershipPlanDto deactivatedDto = TestResponseUtils.toDto(response, MembershipPlanDto.class, objectMapper);
		assertMembershipPlan(modelMapper.map(membershipPlanToDeactivate, MembershipPlanDto.class), deactivatedDto);
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testDeactivateFailureNotFound(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange

		// Act
		HttpEntity<PutMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(
				URI.create(String.format(postDeactivateURI, brand_FitnessBoxing.getUuid(), faker.code().ean13())), port,
				null), HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Membership Plan activation error: %s", response.getStatusCode()));

		if (response.getBody() != null && response.getBody().size() > 0) {
			ErrorDto err = TestResponseUtils.toError(response, objectMapper);
			Assertions.assertEquals(MembershipPlanException.MEMBERSHIPPLAN_NOT_FOUND, err.getCode());
		}
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPatchSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		MembershipPlan membershipPlanToPatch = MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid(), null, null);
		membershipPlanToPatch = membershipPlanRepository.save(membershipPlanToPatch);

		PatchMembershipPlanDto patchDto = modelMapper.map(membershipPlanToPatch, PatchMembershipPlanDto.class);
		patchDto.setUuid(membershipPlanToPatch.getUuid());
		patchDto.setStartDate(Date.from(Instant.now().plus(5, ChronoUnit.DAYS)));
		patchDto.setDescription(null);
		patchDto.setName(null);

		membershipPlanToPatch.setStartDate(patchDto.getStartDate());

		// Act
		HttpEntity<PatchMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, patchDto);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(
				URI.create(String.format(patchURI, brand_FitnessBoxing.getUuid(), patchDto.getUuid())), port, null),
				HttpMethod.PATCH, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));

		MembershipPlanDto patchedDto = TestResponseUtils.toDto(response, MembershipPlanDto.class, objectMapper);
		assertMembershipPlan(modelMapper.map(membershipPlanToPatch, MembershipPlanDto.class), patchedDto);
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPatchFailureNotFound(String role, String user)
			throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		MembershipPlan patchTarget = MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid(), null, null);
		PatchMembershipPlanDto patchDto = modelMapper.map(patchTarget, PatchMembershipPlanDto.class);

		HttpEntity<PatchMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, patchDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(
				URI.create(String.format(patchURI, brand_FitnessBoxing.getUuid(), patchDto.getUuid())), port, null),
				HttpMethod.PATCH, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Patch error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPatchFailureForbiddenBrandMismatch(String role, String user)
			throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PutMembershipPlanDto putDto = modelMapper.map(
				MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid(), null, null), PutMembershipPlanDto.class);
		HttpEntity<PutMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, putDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(putURI, faker.code().isbn10(), putDto.getUuid())), port, null),
				HttpMethod.PATCH, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPatchFailureForbiddenMembershipPlanMismatch(String role, String user)
			throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PutMembershipPlanDto putDto = modelMapper.map(
				MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid(), null, null), PutMembershipPlanDto.class);
		HttpEntity<PutMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, putDto);
		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(
				URI.create(String.format(putURI, brand_FitnessBoxing.getUuid(), faker.code().isbn10())), port, null),
				HttpMethod.PATCH, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
	}

	@Test
	void testDeleteSuccess() throws JsonProcessingException, MalformedURLException {
		// Arrange
		MembershipPlan membershipPlanToDelete = MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid(), gymUuids,
				courseUuids);
		membershipPlanToDelete = membershipPlanRepository.save(membershipPlanToDelete);

		// Act
		HttpEntity<PutCoachDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, null);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(
				URI.create(String.format(deleteURI, brand_FitnessBoxing.getUuid(), membershipPlanToDelete.getUuid())),
				port, null), HttpMethod.DELETE, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.ACCEPTED, response.getStatusCode(),
				String.format("Coach activation error: %s", response.getStatusCode()));

		httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, null);
		response = testRestTemplate.exchange(HttpUtils.createURL(
				URI.create(String.format(getURI, brand_FitnessBoxing.getUuid(), membershipPlanToDelete.getUuid())),
				port, null), HttpMethod.GET, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));
	}

	@Test
	void testDeleteReferencesSuccess() throws JsonProcessingException, MalformedURLException {
		// DELETE REFERENCES SUCCESS: when a course or gym is deleted, it should be
		// removed from all membership plans that reference it.
		// Arrange
		List<String> courseUuids = new ArrayList<>();
		List<String> gymUuids = new ArrayList<>();

		for (int i = 0; i < 2; i++) {
			Course item = CourseBuilder.build(brand_FitnessBoxing.getUuid());
			item = courseRepository.save(item);
			courseUuids.add(item.getUuid());
		}

		for (int i = 0; i < 2; i++) {
			Gym item = GymBuilder.build(brand_FitnessBoxing.getUuid(), faker.code().isbn10(),
					faker.address().cityName(), null);
			item = gymRepository.save(item);
			gymUuids.add(item.getUuid());
		}

		MembershipPlan membershipPlanReferences1 = MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid(), gymUuids,
				courseUuids);
		membershipPlanRepository.save(membershipPlanReferences1);

		MembershipPlan membershipPlanReferences2 = MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid(), gymUuids,
				courseUuids);
		membershipPlanRepository.save(membershipPlanReferences2);

		HttpEntity<PutGymDto> httpGymEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, null);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(
				URI.create(String.format(deleteGymURI, brand_FitnessBoxing.getUuid(), gymUuids.getFirst())), port,
				null), HttpMethod.DELETE, httpGymEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.ACCEPTED, response.getStatusCode(),
				String.format("Gym delete error: %s", response.getStatusCode()));

		HttpEntity<PutCourseDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, null);
		response = testRestTemplate.exchange(HttpUtils.createURL(
				URI.create(String.format(deleteCourseURI, brand_FitnessBoxing.getUuid(), courseUuids.getFirst())), port,
				null), HttpMethod.DELETE, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.ACCEPTED, response.getStatusCode(),
				String.format("Course delete error: %s", response.getStatusCode()));

		PageResult<MembershipPlan> pageMembershipPlan = membershipPlanRepository
				.findAllByBrandUuidAndDeletedIsFalse(brand_FitnessBoxing.getUuid(), PageRequest.of(0, 1000));

		pageMembershipPlan.getContent().forEach(membershipPlan -> {
			if (membershipPlan.getIncludedCourseUuids() != null) {
				Assertions.assertFalse(membershipPlan.getIncludedCourseUuids().stream()
						.filter(courseUuid -> courseUuid.equals(courseUuids.getFirst())).findFirst().isPresent(),
						String.format("Deleted course %s still present in membership plan %s", courseUuids.getFirst(),
								membershipPlan.getUuid()));
			}

			if (membershipPlan.getIncludedGymUuids() != null) {
				Assertions.assertFalse(
						membershipPlan.getIncludedGymUuids().stream()
								.filter(gymUuid -> gymUuid.equals(gymUuids.getFirst())).findFirst().isPresent(),
						String.format("Deleted gym %s still present in membership plan %s", gymUuids.getFirst(),
								membershipPlan.getUuid()));
			}
		});

		membershipPlanReferences1 = membershipPlanRepository.findByBrandUuidAndUuidAndDeletedIsFalse(
				brand_FitnessBoxing.getUuid(), membershipPlanReferences1.getUuid()).get();
		if (membershipPlan.getIncludedCourseUuids() != null) {
			Assertions.assertTrue(
					membershipPlanReferences1.getIncludedGymUuids().size() == 1
							&& membershipPlanReferences1.getIncludedGymUuids().get(0).equals(gymUuids.getLast()),
					String.format("Non deleted gym %s not found in membership plan %s", gymUuids.getLast(),
							membershipPlan.getUuid()));
		}
		if (membershipPlan.getIncludedGymUuids() != null) {
			Assertions.assertTrue(
					membershipPlanReferences1.getIncludedCourseUuids().size() == 1
							&& membershipPlanReferences1.getIncludedCourseUuids().get(0).equals(courseUuids.getLast()),
					String.format("Non deleted course %s not found in membership plan %s", courseUuids.getLast(),
							membershipPlan.getUuid()));
		}

		membershipPlanReferences2 = membershipPlanRepository.findByBrandUuidAndUuidAndDeletedIsFalse(
				brand_FitnessBoxing.getUuid(), membershipPlanReferences2.getUuid()).get();
		if (membershipPlan.getIncludedCourseUuids() != null) {
			Assertions.assertTrue(
					membershipPlanReferences2.getIncludedGymUuids().size() == 1
							&& membershipPlanReferences2.getIncludedGymUuids().get(0).equals(gymUuids.getLast()),
					String.format("Non deleted gym %s not found in membership plan %s", gymUuids.getLast(),
							membershipPlan.getUuid()));
		}
		if (membershipPlan.getIncludedGymUuids() != null) {
			Assertions.assertTrue(
				membershipPlanReferences2.getIncludedCourseUuids().size() == 1
						&& membershipPlanReferences2.getIncludedCourseUuids().get(0).equals(courseUuids.getLast()),
				String.format("Non deleted course %s not found in membership plan %s", courseUuids.getLast(),
						membershipPlan.getUuid()));
		}
	}

	@Test
	void testListActiveMembershipPlansOnDateSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		Brand brand_ActiveDateBrand = BrandBuilder.build(brandCode_ActiveDateBrand, "Active Date Brand");
		brandRepository.save(brand_ActiveDateBrand);

		// Active plan: started in the past, no end date
		MembershipPlan activePlanNoEndDate = MembershipPlanBuilder.build(brand_ActiveDateBrand.getUuid(), null, null);
		activePlanNoEndDate.setStartDate(Date.from(Instant.now().minus(30, ChronoUnit.DAYS)));
		activePlanNoEndDate.setEndDate(null);
		activePlanNoEndDate.setActive(true);
		activePlanNoEndDate.setDeleted(false);
		membershipPlanRepository.save(activePlanNoEndDate);

		// Active plan: started in the past, ends in the future
		MembershipPlan activePlanWithEndDate = MembershipPlanBuilder.build(brand_ActiveDateBrand.getUuid(), null, null);
		activePlanWithEndDate.setStartDate(Date.from(Instant.now().minus(30, ChronoUnit.DAYS)));
		activePlanWithEndDate.setEndDate(Date.from(Instant.now().plus(30, ChronoUnit.DAYS)));
		activePlanWithEndDate.setActive(true);
		activePlanWithEndDate.setDeleted(false);
		membershipPlanRepository.save(activePlanWithEndDate);

		// Future plan: starts in the future — should NOT be returned
		MembershipPlan futurePlan = MembershipPlanBuilder.build(brand_ActiveDateBrand.getUuid(), null, null);
		futurePlan.setStartDate(Date.from(Instant.now().plus(10, ChronoUnit.DAYS)));
		futurePlan.setEndDate(null);
		futurePlan.setActive(true);
		futurePlan.setDeleted(false);
		membershipPlanRepository.save(futurePlan);

		// Expired plan: started in the past, ended in the past — should NOT be returned
		MembershipPlan expiredPlan = MembershipPlanBuilder.build(brand_ActiveDateBrand.getUuid(), null, null);
		expiredPlan.setStartDate(Date.from(Instant.now().minus(60, ChronoUnit.DAYS)));
		expiredPlan.setEndDate(Date.from(Instant.now().minus(10, ChronoUnit.DAYS)));
		expiredPlan.setActive(true);
		expiredPlan.setDeleted(false);
		membershipPlanRepository.save(expiredPlan);

		// Deleted plan that would otherwise match — should NOT be returned
		MembershipPlan deletedActivePlan = MembershipPlanBuilder.build(brand_ActiveDateBrand.getUuid(), null, null);
		deletedActivePlan.setStartDate(Date.from(Instant.now().minus(30, ChronoUnit.DAYS)));
		deletedActivePlan.setEndDate(null);
		deletedActivePlan.setActive(true);
		deletedActivePlan.setDeleted(true);
		membershipPlanRepository.save(deletedActivePlan);

		// Inactive plan that would otherwise match — should NOT be returned
		MembershipPlan inactivePlan = MembershipPlanBuilder.build(brand_ActiveDateBrand.getUuid(), null, null);
		inactivePlan.setStartDate(Date.from(Instant.now().minus(30, ChronoUnit.DAYS)));
		inactivePlan.setEndDate(null);
		inactivePlan.setActive(false);
		inactivePlan.setDeleted(false);
		membershipPlanRepository.save(inactivePlan);

		java.util.Date currentDate = Date.from(Instant.now().truncatedTo(ChronoUnit.DAYS));
		String currentDateStr = currentDate.toInstant().toString();

		HttpEntity<String> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, null);

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(pageNumber, "0");
		params.add(pageSize, "100");
		params.add(currentDateParam, currentDateStr);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(listActiveURI, brand_ActiveDateBrand.getUuid())), port, params),
				HttpMethod.GET, httpEntity, JsonNode.class);

		// Assert — HTTP status
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List active error: %s", response.getStatusCode()));

		PageResultDto<MembershipPlanDto> page = TestResponseUtils.toPage(response,
				new TypeReference<PageResultDto<MembershipPlanDto>>() {
				}, objectMapper);

		// Assert — only the 2 active plans are returned (activePlanNoEndDate +
		// activePlanWithEndDate)
		Assertions.assertEquals(2, page.getTotalElements(),
				String.format("Expected 2 active membership plans but got: %d", page.getTotalElements()));
		Assertions.assertEquals(2, page.getContent().size(),
				String.format("Expected 2 elements on page but got: %d", page.getContent().size()));

		// Assert — every returned plan is not deleted, is active
		page.getContent().forEach(plan -> {
			Assertions.assertFalse(plan.isDeleted(), String.format("Plan %s should not be deleted", plan.getUuid()));
			Assertions.assertTrue(plan.isActive(), String.format("Plan %s should be active", plan.getUuid()));
		});

		// Assert — every returned plan has startDate <= currentDate
		page.getContent().forEach(plan -> {
			Assertions.assertNotNull(plan.getStartDate(),
					String.format("Plan %s startDate must not be null", plan.getUuid()));
			Assertions.assertFalse(plan.getStartDate().after(currentDate), String.format(
					"Plan %s startDate %s is after currentDate %s", plan.getUuid(), plan.getStartDate(), currentDate));
		});

		// Assert — every returned plan has endDate == null or endDate >= currentDate
		page.getContent().forEach(plan -> {
			if (plan.getEndDate() != null) {
				Assertions.assertFalse(plan.getEndDate().before(currentDate), String.format(
						"Plan %s endDate %s is before currentDate %s", plan.getUuid(), plan.getEndDate(), currentDate));
			}
		});

		// Assert — the two expected plans are present
		Assertions.assertTrue(
				page.getContent().stream().anyMatch(p -> p.getUuid().equals(activePlanNoEndDate.getUuid())),
				"Expected activePlanNoEndDate to be present in results");
		Assertions.assertTrue(
				page.getContent().stream().anyMatch(p -> p.getUuid().equals(activePlanWithEndDate.getUuid())),
				"Expected activePlanWithEndDate to be present in results");

		// Assert — excluded plans are NOT present
		Assertions.assertFalse(page.getContent().stream().anyMatch(p -> p.getUuid().equals(futurePlan.getUuid())),
				"futurePlan should not be returned");
		Assertions.assertFalse(page.getContent().stream().anyMatch(p -> p.getUuid().equals(expiredPlan.getUuid())),
				"expiredPlan should not be returned");
		Assertions.assertFalse(
				page.getContent().stream().anyMatch(p -> p.getUuid().equals(deletedActivePlan.getUuid())),
				"deletedActivePlan should not be returned");
		Assertions.assertFalse(page.getContent().stream().anyMatch(p -> p.getUuid().equals(inactivePlan.getUuid())),
				"inactivePlan should not be returned");
	}

	public static final void assertMembershipPlan(MembershipPlanDto expected, MembershipPlanDto result) {
		if (expected.getUuid() != null) {
			Assertions.assertEquals(expected.getUuid(), result.getUuid());
		}

		Assertions.assertEquals(expected.getBrandUuid(), result.getBrandUuid());
		Assertions.assertEquals(expected.getDurationInMonths(), result.getDurationInMonths());
		Assertions.assertEquals(expected.getNumberOfClasses(), result.getNumberOfClasses());
		Assertions.assertEquals(expected.isGuestPrivilege(), result.isGuestPrivilege());
		Assertions.assertEquals(expected.isPromotional(), result.isPromotional());
		Assertions.assertEquals(expected.isGiftCard(), result.isGiftCard());

		Assertions.assertEquals(expected.getPeriod(), result.getPeriod());
		Assertions.assertEquals(expected.getBillingFrequency(), result.getBillingFrequency());

		Assertions.assertEquals(expected.isActive(), result.isActive());

		if (expected.getActivatedOn() != null) {
			Assertions.assertNotNull(result.getActivatedOn());
			Assertions.assertTrue(expected.getActivatedOn().truncatedTo(ChronoUnit.DAYS)
					.equals(result.getActivatedOn().truncatedTo(ChronoUnit.DAYS)));
		}

		if (expected.getDeactivatedOn() != null) {
			Assertions.assertNotNull(result.getDeactivatedOn());
			Assertions.assertTrue(expected.getDeactivatedOn().truncatedTo(ChronoUnit.DAYS)
					.equals(result.getDeactivatedOn().truncatedTo(ChronoUnit.DAYS)));
		}

		if (expected.getName() != null) {
			Assertions.assertNotNull(result.getName());

			Assertions.assertEquals(expected.getName().size(), result.getName().size());
			expected.getName().forEach(name -> {
				Optional<LocalizedStringDto> previous = result.getName().stream()
						.filter(item -> item.getLanguage().equals(name.getLanguage())).findFirst();
				Assertions.assertTrue(previous.isPresent());
				Assertions.assertEquals(previous.get().getText(), name.getText());
			});
		}

		if (expected.getName() == null) {
			Assertions.assertNull(result.getName());
		}

		if (expected.getTitle() != null) {
			Assertions.assertNotNull(result.getTitle());

			Assertions.assertEquals(expected.getTitle().size(), result.getTitle().size());
			expected.getTitle().forEach(title -> {
				Optional<LocalizedStringDto> previous = result.getTitle().stream()
						.filter(item -> item.getLanguage().equals(title.getLanguage())).findFirst();
				Assertions.assertTrue(previous.isPresent());
				Assertions.assertEquals(previous.get().getText(), title.getText());
			});
		}

		if (expected.getTitle() == null) {
			Assertions.assertNull(result.getTitle());
		}

		if (expected.getDescription() != null) {
			Assertions.assertNotNull(result.getDescription());

			Assertions.assertEquals(expected.getDescription().size(), result.getDescription().size());
			expected.getDescription().forEach(description -> {
				Optional<LocalizedStringDto> previous = result.getDescription().stream()
						.filter(item -> item.getLanguage().equals(description.getLanguage())).findFirst();
				Assertions.assertTrue(previous.isPresent());
				Assertions.assertEquals(previous.get().getText(), description.getText());
			});
		}

		if (expected.getDescription() == null) {
			Assertions.assertNull(result.getDescription());
		}

		if (expected.getTermsOfUse() != null) {
			Assertions.assertNotNull(result.getTermsOfUse());

			Assertions.assertEquals(expected.getTermsOfUse().size(), result.getTermsOfUse().size());
			expected.getTermsOfUse().forEach(detail -> {
				Optional<LocalizedStringDto> previous = result.getTermsOfUse().stream()
						.filter(item -> item.getLanguage().equals(detail.getLanguage())).findFirst();
				Assertions.assertTrue(previous.isPresent());
				Assertions.assertEquals(previous.get().getText(), detail.getText());
			});
		}

		if (expected.getTermsOfUse() == null) {
			Assertions.assertNull(result.getTermsOfUse());
		}

		if (expected.getPrice() == null) {
			Assertions.assertNull(result.getPrice());
		}

		if (expected.getPrice() != null) {
			Assertions.assertNotNull(result.getPrice());

			Assertions.assertEquals(expected.getPrice().getAmount(), result.getPrice().getAmount());
			Assertions.assertNotNull(result.getPrice().getCurrency());
			Assertions.assertEquals(expected.getPrice().getCurrency().getCode(),
					result.getPrice().getCurrency().getCode());
			Assertions.assertEquals(expected.getPrice().getCurrency().getName(),
					result.getPrice().getCurrency().getName());
			Assertions.assertEquals(expected.getPrice().getCurrency().getSymbol(),
					result.getPrice().getCurrency().getSymbol());
		}

		if (expected.getPrice() == null) {
			Assertions.assertNull(result.getPrice());
		}

		if (expected.getStartDate() != null) {
			LocalDate expecteStart = expected.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			LocalDate resultStart = result.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			Assertions.assertEquals(expecteStart, resultStart);
		}

		if (expected.getEndDate() != null) {
			LocalDate expecteEnd = expected.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			LocalDate resultEnd = result.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			Assertions.assertEquals(expecteEnd, resultEnd);
		}

		if (expected.getIncludedGymUuids() != null) {
			for (String gymUuid : expected.getIncludedGymUuids()) {
				Assertions.assertTrue(result.getIncludedGymUuids().stream().filter(item -> item.equals(gymUuid))
						.findFirst().isPresent(), String.format("Gym %s is missing in result", gymUuid));
			}
		}

		if (expected.getIncludedCourseUuids() != null) {
			for (String courseUuid : expected.getIncludedCourseUuids()) {
				Assertions.assertTrue(result.getIncludedCourseUuids().stream().filter(item -> item.equals(courseUuid))
						.findFirst().isPresent(), String.format("Course %s is missing in result", courseUuid));
			}
		}
	}
}
