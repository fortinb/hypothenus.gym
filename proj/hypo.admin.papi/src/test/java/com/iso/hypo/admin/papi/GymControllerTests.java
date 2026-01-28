package com.iso.hypo.admin.papi;

import static org.awaitility.Awaitility.await;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iso.hypo.admin.papi.dto.ErrorDto;
import com.iso.hypo.admin.papi.dto.contact.ContactDto;
import com.iso.hypo.admin.papi.dto.contact.PhoneNumberDto;
import com.iso.hypo.admin.papi.dto.model.GymDto;
import com.iso.hypo.admin.papi.dto.patch.PatchGymDto;
import com.iso.hypo.admin.papi.dto.post.PostGymDto;
import com.iso.hypo.admin.papi.dto.put.PutBrandDto;
import com.iso.hypo.admin.papi.dto.put.PutGymDto;
import com.iso.hypo.admin.papi.dto.search.GymSearchDto;
import com.iso.hypo.domain.BrandBuilder;
import com.iso.hypo.domain.GymBuilder;
import com.iso.hypo.domain.aggregate.Brand;
import com.iso.hypo.domain.aggregate.Coach;
import com.iso.hypo.domain.aggregate.Course;
import com.iso.hypo.domain.aggregate.Gym;
import com.iso.hypo.repositories.BrandRepository;
import com.iso.hypo.repositories.CoachRepository;
import com.iso.hypo.repositories.CourseRepository;
import com.iso.hypo.repositories.GymRepository;
import com.iso.hypo.repositories.MembershipPlanRepository;
import com.iso.hypo.services.exception.GymException;
import com.iso.hypo.tests.data.Populator;
import com.iso.hypo.tests.http.HttpUtils;
import com.iso.hypo.tests.security.Roles;
import com.iso.hypo.tests.security.Users;
import com.iso.hypo.tests.utils.StringUtils;
import com.iso.hypo.tests.utils.TestResponseUtils;

import net.datafaker.Faker;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
class GymControllerTests {

	public static final String searchURI = "/v1/brands/%s/gyms/search";
	public static final String listURI = "/v1/brands/%s/gyms";
	public static final String postURI = "/v1/brands/%s/gyms";
	public static final String getURI = "/v1/brands/%s/gyms/%s";
	public static final String putURI = "/v1/brands/%s/gyms/%s";
	public static final String patchURI = "/v1/brands/%s/gyms/%s";
	public static final String deleteURI = "/v1/brands/%s/gyms/%s";
	public static final String postActivateURI = "/v1/brands/%s/gyms/%s/activate";
	public static final String postDeactivateURI = "/v1/brands/%s/gyms/%s/deactivate";
	public static final String searchCriteria = "criteria";
	public static final String pageNumber = "page";
	public static final String pageSize = "pageSize";

	public static final String brandCode = "GymBrand1";
		
	public static final String gymCode_1 = "Gym1";
	public static final String gymCode_2 = "Gym2";
	
	@LocalServerPort
	private int port;

	@Autowired
	BrandRepository brandRepository;
	@Autowired
	GymRepository gymRepository;
	@Autowired
	CoachRepository coachRepository;
	@Autowired
	CourseRepository courseRepository;
	@Autowired
	MembershipPlanRepository membershipPlanRepository;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	ModelMapper modelMapper;

	private Faker faker = new Faker();
	
	private RestTemplateBuilder restTemplateBuilder;
	private TestRestTemplate testRestTemplate;
	private Gym gym;
	private Gym gymIsDeleted;
	private Brand brand;
	private List<Gym> gyms = new ArrayList<Gym>();

	@BeforeAll
	void arrange() {
		restTemplateBuilder = new RestTemplateBuilder()
					.additionalMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper));
					//.requestFactory(new HttpComponentsClientHttpRequestFactory());
				    //.build();
		
		testRestTemplate = new TestRestTemplate(restTemplateBuilder);
		//testRestTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
		gymRepository.deleteAll();

		brand = BrandBuilder.build(brandCode, faker.company().name());
		brandRepository.save(brand);
		
		gym = GymBuilder.build(brand.getUuid(), gymCode_1, faker.address().cityName());
		gymRepository.save(gym);
		
		gym = GymBuilder.build(brand.getUuid(), gymCode_2, faker.address().cityName());
		gymRepository.save(gym);
		
		gymIsDeleted = GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.code().isbn10());
		gymIsDeleted.setDeleted(true);
		gymIsDeleted = gymRepository.save(gymIsDeleted);

		for (int i = 0; i < 10; i++) {
			Gym item = GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.company().name());
			
			gymRepository.save(item);
			gyms.add(item);
		}
		
		for (int i = 0; i < 5; i++) {
			Gym item = GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.company().name());
			item.setActive(false);
			gymRepository.save(item);
			gyms.add(item);
		}
	}

	@AfterAll
	void cleanup() {
		// Cleanup
	//	gymRepository.deleteAll();
	}

	@Test
	void testSearchAutocompleteIsDeletedSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(gymIsDeleted.getName(), 10);
		assertSearch(criteria,0,0);
	}
	
	@Test
	void testSearchAutocompleteCitySuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(gym.getAddress().getCity(), 3);
		assertSearch(criteria,1,1000);
	}

	@Test
	void testSearchAutocompleteStateSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(gym.getAddress().getState(), 2);
		assertSearch(criteria,1,1000);
	}

	@Test
	void testSearchAutocompleteStreetNameSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(gym.getAddress().getStreetName(), 3);
		assertSearch(criteria,1,1000);
	}

	@Test
	void testSearchAutocompleteZipCodeSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(gym.getAddress().getZipCode(), 3);
		assertSearch(criteria,1,1000);
	}

	@Test
	void testSearchAutocompleteNameSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(gym.getName(), 3);
		assertSearch(criteria,1,1000);
	}

	@Test
	void testSearchAutocompleteEmailSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(gym.getEmail(), 3);
		assertSearch(criteria,1,1000);
	}

	@Test
	void testListFirstPageSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		HttpEntity<String> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, null);

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "0");
		params.add(pageSize, "4");

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(String.format(listURI, brand.getUuid())), port, params),
				HttpMethod.GET, httpEntity, JsonNode.class);

		// Assert
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		// Assert
		Page<GymDto> page = TestResponseUtils.toPage(response, new TypeReference<Page<GymDto>>() {}, objectMapper);
		Assertions.assertEquals(0, page.getPageable().getPageNumber(),
				String.format("Gym list first page number invalid: %d", page.getPageable().getPageNumber()));
		Assertions.assertEquals(4, page.getNumberOfElements(),
				String.format("Gym list first page number of elements invalid: %d", page.getNumberOfElements()));
	}

	@Test
	void testListSecondPageSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		HttpEntity<String> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, "");

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "1");
		params.add(pageSize, "4");

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(String.format(listURI, brand.getUuid())), port, params),
				HttpMethod.GET, httpEntity, JsonNode.class);


		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		Page<GymDto> page = TestResponseUtils.toPage(response, new TypeReference<Page<GymDto>>() {}, objectMapper);

		// Assert
		Assertions.assertEquals(1, page.getPageable().getPageNumber(),
				String.format("Gym list second page number invalid: %d", page.getPageable().getPageNumber()));
		Assertions.assertEquals(4, page.getNumberOfElements(),
				String.format("Gym list second page number of elements invalid: %d", page.getNumberOfElements()));
	}

	@Test
	void testPostSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostGymDto postDto = modelMapper.map(GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.company().name()), PostGymDto.class);
		HttpEntity<PostGymDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(String.format(postURI, brand.getUuid())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));

		GymDto createdDto = TestResponseUtils.toDto(response, GymDto.class, objectMapper);
		assertGym(modelMapper.map(postDto, GymDto.class), createdDto);
	}
	
	@Test
	void testPostDuplicateFailure() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostGymDto postDto = modelMapper.map(GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.company().name()), PostGymDto.class);
		HttpEntity<PostGymDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postDto);

		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(String.format(postURI, brand.getUuid())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));

		// Act
		response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(String.format(postURI, brand.getUuid())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);
		
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
		
		GymDto dupDto = TestResponseUtils.toDto(response, GymDto.class, objectMapper);

		Assertions.assertEquals(1, dupDto.getMessages().size(),
				String.format("Duplicate error ,missing message: %s", dupDto.getMessages().size()));
		
		Assertions.assertEquals(GymException.GYM_CODE_ALREADY_EXIST, dupDto.getMessages().getFirst().getCode(),
				String.format("Duplicate error, missing message: %s", dupDto.getMessages().getFirst().getCode()));
	}
	
	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPostFailureForbiddenBrandMismatch(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostGymDto postDto = modelMapper.map(GymBuilder.build(brand.getUuid(), faker.code().isbn10(), faker.company().name()), PostGymDto.class);
		HttpEntity<PostGymDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(String.format(postURI, faker.code().isbn10())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis", "Member, Guillaume Fortin", })
	void testGetSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostGymDto postDto = modelMapper.map(GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.company().name()), PostGymDto.class);
		HttpEntity<PostGymDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postDto);

		ResponseEntity<JsonNode> responsePost = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postURI, brand.getUuid())), port, null), HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.CREATED, responsePost.getStatusCode(),
				String.format("Post error: %s", responsePost.getStatusCode()));

		// Act
		httpEntity = HttpUtils.createHttpEntity(role, user, null);
		GymDto createdDto = TestResponseUtils.toDto(responsePost, GymDto.class, objectMapper);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(getURI, createdDto.getBrandUuid(), createdDto.getUuid())), port, null),
				HttpMethod.GET, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));

		GymDto fetchedDto = TestResponseUtils.toDto(response, GymDto.class, objectMapper);
		assertGym(modelMapper.map(postDto, GymDto.class), fetchedDto);
	}
	
	@Test
	void testGetFailureNotFound() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		HttpEntity<Object> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, null);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(getURI, brand.getUuid(), faker.code().isbn10())), port, null),
				HttpMethod.GET, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPutSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Gym updatedGym = GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.company().name());
		updatedGym.setActive(true);
		updatedGym = gymRepository.save(updatedGym);

		PutGymDto putDto = modelMapper.map(updatedGym, PutGymDto.class);
		// mutate mutable fields
		putDto.setEmail(faker.internet().emailAddress());
		putDto.setName(putDto.getName() + " - updated");
		if (putDto.getAddress() != null) {
			putDto.getAddress().setStreetName(faker.address().streetName());
		}
		if (putDto.getPhoneNumbers() != null && putDto.getPhoneNumbers().size() > 0) {
			putDto.getPhoneNumbers().remove(0);
		}
		if (putDto.getContacts() != null && putDto.getContacts().size() > 1) {
			putDto.getContacts().remove(1);
			putDto.getContacts().get(0).setLastname("Updated" + faker.name().lastName());
		} else if (putDto.getContacts() != null && putDto.getContacts().size() == 1) {
			putDto.getContacts().get(0).setLastname("Updated" + faker.name().lastName());
		}

		// Act
		HttpEntity<PutGymDto> httpEntity = HttpUtils.createHttpEntity(role, user, putDto);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, updatedGym.getBrandUuid(), updatedGym.getUuid())), port, null),
				HttpMethod.PUT, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put error: %s", response.getStatusCode()));

		GymDto updatedDto = TestResponseUtils.toDto(response, GymDto.class, objectMapper);
		assertGym(modelMapper.map(putDto, GymDto.class), updatedDto);
	}
	
	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPutNullSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Gym updatedGym = GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.company().name());
		updatedGym.setActive(true);
		updatedGym = gymRepository.save(updatedGym);
		
		PutGymDto putDto = modelMapper.map(updatedGym, PutGymDto.class);
		
		putDto.setEmail(null);
		putDto.setAddress(null);
		putDto.setPhoneNumbers(null);
		putDto.setContacts(null);
		
		// Act
		HttpEntity<PutGymDto> httpEntity = HttpUtils.createHttpEntity(role, user, putDto);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, updatedGym.getBrandUuid(), updatedGym.getUuid())), port, null),
				HttpMethod.PUT, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put null error: %s", response.getStatusCode()));

	 	GymDto updated = TestResponseUtils.toDto(response, GymDto.class, objectMapper);
	 	assertGym(modelMapper.map(putDto, GymDto.class), updated);
	}
	
	@Test
	void testPutFailureNotFound() throws MalformedURLException, JsonProcessingException, Exception {
		PutGymDto putDto = modelMapper.map(GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.company().name()), PutGymDto.class);
		HttpEntity<PutGymDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, putDto);

		// Arrange
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, brand.getUuid(), putDto.getUuid())), port, null),
				HttpMethod.PUT, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));
	}
	
	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPutFailureForbiddenBrandMismatch(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PutGymDto putDto = modelMapper.map(GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.company().name()), PutGymDto.class);
		HttpEntity<PutGymDto> httpEntity = HttpUtils.createHttpEntity(role, user, putDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, faker.code().isbn10(), putDto.getUuid())), port, null),
				HttpMethod.PUT, httpEntity, JsonNode.class);
		
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
	}
	
	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPutFailureForbiddenGymMismatch(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PutGymDto putDto = modelMapper.map(GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.company().name()), PutGymDto.class);
		HttpEntity<PutGymDto> httpEntity = HttpUtils.createHttpEntity(role, user, putDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, brand.getUuid(), faker.code().isbn10())), port, null),
				HttpMethod.PUT, httpEntity, JsonNode.class);
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPatchSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Gym gymToPatch = GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.company().name());
		gymToPatch.setActive(true);
		gymToPatch = gymRepository.save(gymToPatch);
		
		PatchGymDto patchDto = modelMapper.map(gymToPatch, PatchGymDto.class);
		patchDto.getAddress().setStreetName(faker.address().streetName());
		patchDto.setEmail(faker.internet().emailAddress());
		
		// Act
		HttpEntity<PatchGymDto> httpEntity = HttpUtils.createHttpEntity(role, user, patchDto);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(patchURI, gymToPatch.getBrandUuid(), gymToPatch.getUuid())), port, null),
				HttpMethod.PATCH, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));

		GymDto patchedDto = TestResponseUtils.toDto(response, GymDto.class, objectMapper);
	  	assertGym(modelMapper.map(patchDto, GymDto.class), patchedDto);
	}
	
	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPatchFailureNotFound(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		Gym patchTarget = GymBuilder.build(brand.getUuid(), faker.code().isbn10(), faker.company().name());
		PatchGymDto patchDto = modelMapper.map(patchTarget, PatchGymDto.class);
		
		HttpEntity<PatchGymDto> httpEntity = HttpUtils.createHttpEntity(role, user, patchDto);
		
		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(patchURI, patchTarget.getBrandUuid(), patchTarget.getUuid())), port, null),
				HttpMethod.PATCH, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Patch error: %s", response.getStatusCode()));
	}
	
	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPatchFailureForbiddenBrandMismatch(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PutGymDto patchDto = modelMapper.map(GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.company().name()), PutGymDto.class);
		HttpEntity<PutGymDto> httpEntity = HttpUtils.createHttpEntity(role, user, patchDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, faker.code().isbn10(), patchDto.getUuid())), port, null),
				HttpMethod.PATCH, httpEntity, JsonNode.class);
		
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
	}
	
	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPatchFailureForbiddenGymMismatch(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PutGymDto putDto = modelMapper.map(GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.company().name()), PutGymDto.class);
		HttpEntity<PutGymDto> httpEntity = HttpUtils.createHttpEntity(role, user, putDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, brand.getUuid(), faker.code().isbn10())), port, null),
				HttpMethod.PATCH, httpEntity, JsonNode.class);
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testActivateSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Gym gymToActivate = GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.company().name());
		gymToActivate.setActive(false);
		gymToActivate.setActivatedOn(null);
		gymToActivate.setDeactivatedOn(null);
		gymToActivate = gymRepository.save(gymToActivate);

		gymToActivate.setActive(true);
		gymToActivate.setActivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		gymToActivate.setDeactivatedOn(null);

		// Act
		HttpEntity<PutGymDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postActivateURI, gymToActivate.getBrandUuid(), gymToActivate.getUuid())),
						port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Gym activation error: %s", response.getStatusCode()));

		GymDto activated = TestResponseUtils.toDto(response, GymDto.class, objectMapper);
		assertGym(modelMapper.map(gymToActivate, GymDto.class), activated);
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testActivateFailureNotFound(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange

		// Act
		HttpEntity<PutGymDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(postActivateURI, brand.getUuid(), faker.code().isbn10())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);
											
		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Gym activation error: %s", response.getStatusCode()));

		if (response.getBody() != null && response.getBody().size() > 0) {
			ErrorDto err = TestResponseUtils.toError(response, objectMapper);
			Assertions.assertEquals(GymException.GYM_NOT_FOUND, err.getCode());
		}
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testDeactivateSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Gym gymToDeactivate = GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.company().name());
		gymToDeactivate.setActive(true);
		gymToDeactivate.setActivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		gymToDeactivate = gymRepository.save(gymToDeactivate);

		gymToDeactivate.setActive(false);
		gymToDeactivate.setDeactivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));

		// Act
		HttpEntity<PutGymDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(
				URI.create(String.format(postDeactivateURI, brand.getUuid(), gymToDeactivate.getUuid())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Gym deactivation error: %s", response.getStatusCode()));

		GymDto deactivated = TestResponseUtils.toDto(response, GymDto.class, objectMapper);
		assertGym(modelMapper.map(gymToDeactivate, GymDto.class), deactivated);
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testDeactivateFailure(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange

		// Act
		HttpEntity<PutGymDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(postDeactivateURI, brand.getUuid(), faker.code().ean13())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Gym activation error: %s", response.getStatusCode()));

		if (response.getBody() != null && response.getBody().size() > 0) {
			ErrorDto err = TestResponseUtils.toError(response, objectMapper);
			Assertions.assertEquals(GymException.GYM_NOT_FOUND, err.getCode());
		}
	}
	
	@Test
	void testDeleteSuccess() throws JsonProcessingException, MalformedURLException {
		// Arrange
		Populator populator = new Populator(brandRepository, gymRepository, coachRepository, courseRepository, membershipPlanRepository);
		Brand brand = populator.populateFullBrand("todelete", "Brand to delete");
		Gym gymToDelete = gymRepository.findByBrandUuidAndCode(brand.getUuid(), "boucherville").get();
		
		// Act
		HttpEntity<PutBrandDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, null);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(
				URI.create(String.format(deleteURI, brand.getUuid(), gymToDelete.getUuid())), port, null),
				HttpMethod.DELETE, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Brand delete error: %s", response.getStatusCode()));
		
		httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, null);
		response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(getURI, brand.getUuid(), gymToDelete.getUuid())), port, null),
				HttpMethod.GET, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));
		
		Page<Coach> pageCoach = coachRepository.findAllByBrandUuidAndGymUuidAndIsDeletedIsFalse(brand.getUuid(),gymToDelete.getUuid(),  PageRequest.of(0, 1000, Sort.Direction.ASC, "name"));
		Assertions.assertEquals(0, pageCoach.getTotalElements(),
				String.format("Deleted brand coachs not deleted: %d", pageCoach.getTotalElements()));
		
		Page<Course> pageCourse = courseRepository.findAllByBrandUuidAndGymUuidAndIsDeletedIsFalse(brand.getUuid(), gymToDelete.getUuid(), PageRequest.of(0, 1000, Sort.Direction.ASC, "name"));
		Assertions.assertEquals(0, pageCourse.getTotalElements(),
				String.format("Deleted brand courses not deleted: %d", pageCourse.getTotalElements()));
	}

	private void assertSearch(String criteria, int minimumNumberOfElements, int maximumNumberOfElements) 
			throws JsonProcessingException, MalformedURLException {
		// Arrange
		HttpEntity<String> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, "");

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(searchCriteria, criteria);
		params.add(pageNumber, "0");
		params.add(pageSize, "4");
		
		// Act
		await()
        .atMost(5, TimeUnit.SECONDS)
        .pollInterval(200, TimeUnit.MILLISECONDS)
        .untilAsserted(() -> {
    		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
    				HttpUtils.createURL(URI.create(String.format(searchURI, brand.getUuid()) ), port, params), HttpMethod.GET, httpEntity,
    				JsonNode.class);

    		Assertions.assertEquals(response.getStatusCode(), HttpStatus.OK,
    				String.format("Search error: %s", response.getStatusCode()));
    		
    		Page<GymSearchDto> page = TestResponseUtils.toPage(response, new TypeReference<Page<GymSearchDto>>() {}, objectMapper);
				Assertions.assertTrue(page.getNumberOfElements() >= minimumNumberOfElements &&
						page.getNumberOfElements() <= maximumNumberOfElements,
						String.format("Brand search return invalid number of results [%s]: %d",
							criteria, page.getNumberOfElements()));
			});
	}

	public static final void assertGym(GymDto expected, GymDto result) {
		if (expected.getUuid() != null) {
			Assertions.assertEquals(expected.getUuid(), result.getUuid());
		}
		
		Assertions.assertEquals(expected.getCode(), result.getCode());
		Assertions.assertEquals(expected.getName(), result.getName());
		Assertions.assertEquals(expected.getEmail(), result.getEmail());
		
		if (expected.getAddress() != null) {
			Assertions.assertEquals(expected.getAddress().getCivicNumber(), result.getAddress().getCivicNumber());
			Assertions.assertEquals(expected.getAddress().getStreetName(), result.getAddress().getStreetName());
			Assertions.assertEquals(expected.getAddress().getAppartment(), result.getAddress().getAppartment());
			Assertions.assertEquals(expected.getAddress().getCity(), result.getAddress().getCity());
			Assertions.assertEquals(expected.getAddress().getState(), result.getAddress().getState());
			Assertions.assertEquals(expected.getAddress().getZipCode(), result.getAddress().getZipCode());
		}
		
		if (expected.getAddress() == null) {
			Assertions.assertNull(result.getAddress());
		}
		
		if (expected.getPhoneNumbers() != null) {
			Assertions.assertNotNull(result.getPhoneNumbers());

			Assertions.assertEquals(expected.getPhoneNumbers().size(), result.getPhoneNumbers().size());
			expected.getPhoneNumbers().forEach(phone -> {
				Optional<PhoneNumberDto> previous = result.getPhoneNumbers().stream()
						.filter(item -> item.getType().equals(phone.getType())).findFirst();
				Assertions.assertTrue(previous.isPresent());
				Assertions.assertEquals(previous.get().getNumber(), phone.getNumber());
			});
		}

		if (expected.getPhoneNumbers() == null) {
			Assertions.assertNull(result.getPhoneNumbers());
		}

	
		if (expected.getContacts() != null) {
			Assertions.assertNotNull(result.getContacts());

			Assertions.assertEquals(expected.getContacts().size(), result.getContacts().size());
			expected.getContacts().forEach(contact -> {
				Optional<ContactDto> previous = result.getContacts().stream()
						.filter(item -> item.getFirstname().equals(contact.getFirstname())).findFirst();
				Assertions.assertTrue(previous.isPresent());
				Assertions.assertEquals(previous.get().getLastname(), contact.getLastname());
				Assertions.assertEquals(previous.get().getDescription(), contact.getDescription());
				Assertions.assertEquals(previous.get().getEmail(), contact.getEmail());
				
				if (previous.get().getPhoneNumbers() != null) {
					Assertions.assertNotNull(contact.getPhoneNumbers());
					
					Assertions.assertEquals(contact.getPhoneNumbers().size(), contact.getPhoneNumbers().size());
					
					contact.getPhoneNumbers().forEach(phone -> {
						Optional<PhoneNumberDto> previousPhone = contact.getPhoneNumbers().stream()
								.filter(item -> item.getType().equals(phone.getType())).findFirst();
						Assertions.assertTrue(previousPhone.isPresent());
						Assertions.assertEquals(previousPhone.get().getNumber(), phone.getNumber());
					});
				}
				
				if (previous.get().getPhoneNumbers() == null) {
					Assertions.assertNull(result.getPhoneNumbers());
				}
			});
		}

		if (expected.getContacts() == null) {
			Assertions.assertNull(result.getContacts());
		}
	}
}
