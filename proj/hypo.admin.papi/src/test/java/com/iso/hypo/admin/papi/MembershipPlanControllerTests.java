package com.iso.hypo.admin.papi;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.Instant;
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iso.hypo.admin.papi.dto.ErrorDto;
import com.iso.hypo.admin.papi.dto.LocalizedStringDto;
import com.iso.hypo.admin.papi.dto.model.MembershipPlanDto;
import com.iso.hypo.admin.papi.dto.patch.PatchMembershipPlanDto;
import com.iso.hypo.admin.papi.dto.post.PostMembershipPlanDto;
import com.iso.hypo.admin.papi.dto.pricing.OneTimeFeeDto;
import com.iso.hypo.admin.papi.dto.put.PutMembershipPlanDto;
import com.iso.hypo.domain.BrandBuilder;
import com.iso.hypo.domain.MembershipPlanBuilder;
import com.iso.hypo.domain.aggregate.Brand;
import com.iso.hypo.domain.aggregate.MembershipPlan;
import com.iso.hypo.repositories.BrandRepository;
import com.iso.hypo.repositories.MembershipPlanRepository;
import com.iso.hypo.services.exception.MembershipPlanException;
import com.iso.hypo.tests.http.HttpUtils;
import com.iso.hypo.tests.security.Roles;
import com.iso.hypo.tests.security.Users;
import com.iso.hypo.tests.utils.TestResponseUtils;

import net.datafaker.Faker;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
class MembershipPlanControllerTests {

	public static final String listURI = "/v1/brands/%s/membership/plans";
	public static final String postURI = "/v1/brands/%s/membership/plans";
	public static final String getURI = "/v1/brands/%s/membership/plans/%s";
	public static final String putURI = "/v1/brands/%s/membership/plans/%s";
	public static final String postActivateURI = "/v1/brands/%s/membership/plans/%s/activate";
	public static final String postDeactivateURI = "/v1/brands/%s/membership/plans/%s/deactivate";
	public static final String patchURI = "/v1/brands/%s/membership/plans/%s";
	public static final String pageNumber = "page";
	public static final String pageSize = "pageSize";
	public static final String includeInactive = "includeInactive";

	public static final String brandCode_FitnessBoxing = "MPFitnessBoxing";
	public static final String brandCode_CrossfitExtreme = "MPCrossfitExtreme";
	
	@LocalServerPort
	private int port;

	@Autowired
	BrandRepository brandRepository;
	
	@Autowired
	MembershipPlanRepository membershipPlanRepository;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	ModelMapper modelMapper;

	private Faker faker = new Faker();

	private TestRestTemplate testRestTemplate = new TestRestTemplate();

	private MembershipPlan membershipPlan;
	private MembershipPlan membershipPlanIsDeleted;
	private Brand brand_FitnessBoxing;
	private Brand brand_CrossfitExtreme;
	
	private List<MembershipPlan> membershipPlans = new ArrayList<MembershipPlan>();

	@BeforeAll
	void arrange() {
		testRestTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());

		membershipPlanRepository.deleteAll();

		brand_FitnessBoxing = BrandBuilder.build(brandCode_FitnessBoxing, "Fitness Boxing");
		brandRepository.save(brand_FitnessBoxing);
		
		brand_CrossfitExtreme = BrandBuilder.build(brandCode_CrossfitExtreme, "Crossfit Extremeg");
		brandRepository.save(brand_FitnessBoxing);
		
		membershipPlan = MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid());
		membershipPlanRepository.save(membershipPlan);

		membershipPlanIsDeleted = MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid());
		membershipPlanIsDeleted.setDeleted(true);
		membershipPlanIsDeleted = membershipPlanRepository.save(membershipPlanIsDeleted);

		for (int i = 0; i < 10; i++) {
			MembershipPlan item = MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid());
			membershipPlanRepository.save(item);
			membershipPlans.add(item);
		}

		for (int i = 0; i < 4; i++) {
			MembershipPlan item = MembershipPlanBuilder.build(brand_CrossfitExtreme.getUuid());
			membershipPlanRepository.save(item);
			membershipPlans.add(item);
		}

		MembershipPlan item = MembershipPlanBuilder.build(brand_CrossfitExtreme.getUuid());
		item.setActive(false);
		membershipPlanRepository.save(item);
	}

	@AfterAll
	void cleanup() {
		// Cleanup
		// membershipPlanRepository.deleteAll();
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
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(listURI, brand_CrossfitExtreme.getUuid())), port, params), HttpMethod.GET,
				httpEntity, JsonNode.class);

		// Assert
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		Page<MembershipPlanDto> page = TestResponseUtils.toPage(response, new TypeReference<Page<MembershipPlanDto>>() {}, objectMapper);

		// Assert
		Assertions.assertEquals(0, page.getPageable().getPageNumber(),
				String.format("Membership Plan list first page number invalid: %d", page.getPageable().getPageNumber()));
		Assertions.assertEquals(4, page.getNumberOfElements(),
				String.format("Membership Plan list first page number of elements invalid: %d", page.getNumberOfElements()));
		Assertions.assertEquals(4, page.getTotalElements(),
				String.format("Membership Plan total number of elements invalid: %d", page.getTotalElements()));

		page.get().forEach(membershipPlan -> Assertions.assertTrue(membershipPlan.isActive()));
		page.get().forEach(membershipPlan -> Assertions.assertTrue(membershipPlan.isDeleted() == false));
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
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(listURI, brand_CrossfitExtreme.getUuid())), port, params), HttpMethod.GET,
				httpEntity, JsonNode.class);

		// Assert
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		Page<MembershipPlanDto> page = TestResponseUtils.toPage(response, new TypeReference<Page<MembershipPlanDto>>() {}, objectMapper);

		// Assert
		Assertions.assertEquals(0, page.getPageable().getPageNumber(),
				String.format("Membership Plan list first page number invalid: %d", page.getPageable().getPageNumber()));
		Assertions.assertEquals(5, page.getNumberOfElements(),
				String.format("Membership Plan list first page number of elements invalid: %d", page.getNumberOfElements()));
		Assertions.assertEquals(5, page.getTotalElements(),
				String.format("Membership Plan total number of elements invalid: %d", page.getTotalElements()));

		page.get().forEach(membershipPlan -> Assertions.assertTrue(membershipPlan.isDeleted() == false));
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
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(listURI, brand_CrossfitExtreme.getUuid())), port, params), HttpMethod.GET,
				httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		Page<MembershipPlanDto> page = TestResponseUtils.toPage(response, new TypeReference<Page<MembershipPlanDto>>() {}, objectMapper);

		// Assert
		Assertions.assertEquals(1, page.getPageable().getPageNumber(),
				String.format("Membership Plan list second page number invalid: %d", page.getPageable().getPageNumber()));
		Assertions.assertEquals(2, page.getNumberOfElements(),
				String.format("Membership Plan list second page number of elements invalid: %d", page.getNumberOfElements()));
		
		page.get().forEach(membershipPlan -> Assertions.assertTrue(membershipPlan.isActive()));
		page.get().forEach(membershipPlan -> Assertions.assertTrue(membershipPlan.isDeleted() == false));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPostSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostMembershipPlanDto postDto = modelMapper.map(MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid()), PostMembershipPlanDto.class);

		HttpEntity<PostMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, postDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postURI, brand_FitnessBoxing.getUuid())), port, null), HttpMethod.POST,
				httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));
		MembershipPlanDto createdDto = TestResponseUtils.toDto(response, MembershipPlanDto.class, objectMapper);
		assertMembershipPlan(modelMapper.map(postDto, MembershipPlanDto.class), createdDto);
	}
	
	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPostFailureForbiddenBrandMismatch(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostMembershipPlanDto postDto = modelMapper.map(MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid()), PostMembershipPlanDto.class);
		HttpEntity<PostMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, postDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(String.format(postURI, faker.code().isbn10())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testGetSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostMembershipPlanDto postDto = modelMapper.map(MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid()), PostMembershipPlanDto.class);

		HttpEntity<PostMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, postDto);

		ResponseEntity<JsonNode> responsePost = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postURI, brand_FitnessBoxing.getUuid())), port, null), HttpMethod.POST,
				httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.CREATED, responsePost.getStatusCode(),
				String.format("Post error: %s", responsePost.getStatusCode()));

		// Act
		httpEntity = HttpUtils.createHttpEntity(role, user, null);
		MembershipPlanDto createdDto = TestResponseUtils.toDto(responsePost, MembershipPlanDto.class, objectMapper);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(getURI, brand_FitnessBoxing.getUuid(), createdDto.getUuid())), port, null),
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
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(getURI, brand_FitnessBoxing.getUuid(), faker.code().isbn10())), port, null),
				HttpMethod.GET, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPutSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		MembershipPlan membershipPlanToUpdate = MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid());
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
		if (putDto.getOneTimeFees() != null && putDto.getOneTimeFees().size() > 0) {
			putDto.getOneTimeFees().remove(0);
		}
		if (putDto.getCourses() != null && putDto.getCourses().size() > 0) {
			putDto.getCourses().remove(0);
		}

		// Act
		HttpEntity<PutMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, putDto);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, brand_FitnessBoxing.getUuid(), putDto.getUuid())), port, null),
				HttpMethod.PUT, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put error: %s", response.getStatusCode()));

		MembershipPlanDto updatedDto = TestResponseUtils.toDto(response, MembershipPlanDto.class, objectMapper);
		assertMembershipPlan(modelMapper.map(putDto, MembershipPlanDto.class), updatedDto);
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPutNullSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		MembershipPlan membershipPlanToUpdate = MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid());
		membershipPlanToUpdate.setActive(false);
		membershipPlanToUpdate.setActivatedOn(null);
		membershipPlanToUpdate.setDeactivatedOn(null);
		membershipPlanToUpdate = membershipPlanRepository.save(membershipPlanToUpdate);

		PutMembershipPlanDto putDto = modelMapper.map(membershipPlanToUpdate, PutMembershipPlanDto.class);
		putDto.setDescription(null);
		putDto.setName(null);
		
		membershipPlanToUpdate.setDescription(null);
		membershipPlanToUpdate.setName(null);

		// Act
		HttpEntity<PutMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, putDto);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, brand_FitnessBoxing.getUuid(), putDto.getUuid())), port, null),
				HttpMethod.PUT, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put null error: %s", response.getStatusCode()));

		MembershipPlanDto updatedDto = TestResponseUtils.toDto(response, MembershipPlanDto.class, objectMapper);
		assertMembershipPlan(modelMapper.map(membershipPlanToUpdate, MembershipPlanDto.class), updatedDto);
	}
	
	@Test
	void testPutFailureNotFound() throws MalformedURLException, JsonProcessingException, Exception {
		PutMembershipPlanDto putDto = modelMapper.map(MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid()), PutMembershipPlanDto.class);
		HttpEntity<PutMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, putDto);
		
		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, brand_FitnessBoxing.getUuid(), putDto.getUuid())), port, null),
				HttpMethod.PUT, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));
	}
	
	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPutFailureForbiddenBrandMismatch(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PutMembershipPlanDto putDto = modelMapper.map(MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid()), PutMembershipPlanDto.class);
		HttpEntity<PutMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, putDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, faker.code().isbn10(), putDto.getUuid())), port, null),
				HttpMethod.PUT, httpEntity, JsonNode.class);
		
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
	}
	
	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPutFailureForbiddenMembershipPlanMismatch(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PutMembershipPlanDto putDto = modelMapper.map(MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid()), PutMembershipPlanDto.class);
		HttpEntity<PutMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, putDto);
		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, brand_FitnessBoxing.getUuid(), faker.code().isbn10())), port, null),
				HttpMethod.PUT, httpEntity, JsonNode.class);
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testActivateSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		MembershipPlan membershipPlanToActivate = MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid());
		membershipPlanToActivate.setActive(false);
		membershipPlanToActivate.setActivatedOn(null);
		membershipPlanToActivate.setDeactivatedOn(null);
		membershipPlanToActivate = membershipPlanRepository.save(membershipPlanToActivate);

		membershipPlanToActivate.setActive(true);
		membershipPlanToActivate.setActivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		membershipPlanToActivate.setDeactivatedOn(null);

		// Act
		HttpEntity<PutMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postActivateURI, brand_FitnessBoxing.getUuid(), membershipPlanToActivate.getUuid())),
						port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Membership Plan activation error: %s", response.getStatusCode()));

		MembershipPlanDto activatedDto = TestResponseUtils.toDto(response, MembershipPlanDto.class, objectMapper);
		assertMembershipPlan(modelMapper.map(membershipPlanToActivate, MembershipPlanDto.class), activatedDto);
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testActivateFailureNotFound(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange

		// Act
		HttpEntity<PutMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(postActivateURI, brand_FitnessBoxing.getUuid(), faker.code().isbn10())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);
																								
		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Membership Plan activation error: %s", response.getStatusCode()));

		if (response.getBody() != null && response.getBody().size() > 0) {
			ErrorDto err = TestResponseUtils.toError(response, objectMapper);
			Assertions.assertEquals(MembershipPlanException.MEMBERSHIPPLAN_NOT_FOUND, err.getCode());
		}
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testDeactivateSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		MembershipPlan membershipPlanToDeactivate = MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid());
		membershipPlanToDeactivate.setActive(true);
		membershipPlanToDeactivate.setActivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		membershipPlanToDeactivate = membershipPlanRepository.save(membershipPlanToDeactivate);

		membershipPlanToDeactivate.setActive(false);
		membershipPlanToDeactivate.setDeactivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));

		// Act
		HttpEntity<PutMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(
				URI.create(String.format(postDeactivateURI, brand_FitnessBoxing.getUuid(), membershipPlanToDeactivate.getUuid())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Membership Plan deactivation error: %s", response.getStatusCode()));

		MembershipPlanDto deactivatedDto = TestResponseUtils.toDto(response, MembershipPlanDto.class, objectMapper);
		assertMembershipPlan(modelMapper.map(membershipPlanToDeactivate, MembershipPlanDto.class), deactivatedDto);
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testDeactivateFailureNotFound(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange

		// Act
		HttpEntity<PutMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(postDeactivateURI, brand_FitnessBoxing.getUuid(), faker.code().ean13())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Membership Plan activation error: %s", response.getStatusCode()));

		if (response.getBody() != null && response.getBody().size() > 0) {
			ErrorDto err = TestResponseUtils.toError(response, objectMapper);
			Assertions.assertEquals(MembershipPlanException.MEMBERSHIPPLAN_NOT_FOUND, err.getCode());
		}
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPatchSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		MembershipPlan membershipPlanToPatch = MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid());
		membershipPlanToPatch = membershipPlanRepository.save(membershipPlanToPatch);

		PatchMembershipPlanDto patchDto = modelMapper.map(membershipPlanToPatch, PatchMembershipPlanDto.class);
		patchDto.setUuid(membershipPlanToPatch.getUuid());
		patchDto.setDescription(null);
		patchDto.setName(null);

		// Act
		HttpEntity<PatchMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, patchDto);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(patchURI, brand_FitnessBoxing.getUuid(), patchDto.getUuid())), port, null),
				HttpMethod.PATCH, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));

		membershipPlanToPatch.setCode(patchDto.getCode());
		MembershipPlanDto patchedDto = TestResponseUtils.toDto(response, MembershipPlanDto.class, objectMapper);
		assertMembershipPlan(modelMapper.map(membershipPlanToPatch, MembershipPlanDto.class), patchedDto);
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPatchFailureNotFound(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		MembershipPlan patchTarget = MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid());
		PatchMembershipPlanDto patchDto = modelMapper.map(patchTarget, PatchMembershipPlanDto.class);
		
		HttpEntity<PatchMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, patchDto);
		
		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(patchURI, brand_FitnessBoxing.getUuid(), patchDto.getUuid())), port, null),
				HttpMethod.PATCH, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Patch error: %s", response.getStatusCode()));
	}
	
	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPatchFailureForbiddenBrandMismatch(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PutMembershipPlanDto putDto = modelMapper.map(MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid()), PutMembershipPlanDto.class);
		HttpEntity<PutMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, putDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, faker.code().isbn10(), putDto.getUuid())), port, null),
				HttpMethod.PATCH, httpEntity, JsonNode.class);
		
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
	}
	
	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPatchFailureForbiddenMembershipPlanMismatch(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PutMembershipPlanDto putDto = modelMapper.map(MembershipPlanBuilder.build(brand_FitnessBoxing.getUuid()), PutMembershipPlanDto.class);
		HttpEntity<PutMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, putDto);
		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, brand_FitnessBoxing.getUuid(), faker.code().isbn10())), port, null),
				HttpMethod.PATCH, httpEntity, JsonNode.class);
		
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
	}

	public static final void assertMembershipPlan(MembershipPlanDto expected, MembershipPlanDto result) {
		if (expected.getUuid() != null) {
			Assertions.assertEquals(expected.getUuid(), result.getUuid());
		}
		
		Assertions.assertEquals(expected.getBrandUuid(), result.getBrandUuid());
		Assertions.assertEquals(expected.getCode(), result.getCode());
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
		
		if (expected.getCost() == null) {
			Assertions.assertNull(result.getCost());
		}
		
		if (expected.getCost() != null) {
			Assertions.assertNotNull(result.getCost());
			
			Assertions.assertEquals(expected.getCost().getCost(), result.getCost().getCost());
			Assertions.assertNotNull(result.getCost().getCurrency());
			Assertions.assertEquals(expected.getCost().getCurrency().getCode(), result.getCost().getCurrency().getCode());
			Assertions.assertEquals(expected.getCost().getCurrency().getName(), result.getCost().getCurrency().getName());
			Assertions.assertEquals(expected.getCost().getCurrency().getSymbol(), result.getCost().getCurrency().getSymbol());
		}
		
		if (expected.getCost() == null) {
			Assertions.assertNull(result.getCost());
		}
		
		if (expected.getOneTimeFees() != null) {
			Assertions.assertNotNull(result.getOneTimeFees());

			Assertions.assertEquals(expected.getOneTimeFees().size(), result.getOneTimeFees().size());
			
			expected.getOneTimeFees().forEach(oneTimeFee -> {
				Optional<OneTimeFeeDto> previous = result.getOneTimeFees().stream()
				.filter(item -> item.getCode().equals(oneTimeFee.getCode())).findFirst();
				Assertions.assertEquals(previous.get().getCost().getCost(), oneTimeFee.getCost().getCost());
				
				Assertions.assertNotNull(previous.get().getCost().getCurrency());
				Assertions.assertEquals(previous.get().getCost().getCurrency().getCode(), oneTimeFee.getCost().getCurrency().getCode());
				Assertions.assertEquals(previous.get().getCost().getCurrency().getName(), oneTimeFee.getCost().getCurrency().getName());
				Assertions.assertEquals(previous.get().getCost().getCurrency().getSymbol(), oneTimeFee.getCost().getCurrency().getSymbol());

			
				oneTimeFee.getDescription().forEach(description -> {
					Optional<LocalizedStringDto> previousDescription = previous.get().getDescription().stream()
							.filter(item -> item.getLanguage().equals(description.getLanguage())).findFirst();
					Assertions.assertTrue(previousDescription.isPresent());
					Assertions.assertEquals(previousDescription.get().getText(), description.getText());
				});
			});
		}
	}
}
