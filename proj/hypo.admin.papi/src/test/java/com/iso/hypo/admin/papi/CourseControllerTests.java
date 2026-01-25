package com.iso.hypo.admin.papi;

import java.net.MalformedURLException;
import java.net.URI;
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
import com.fasterxml.jackson.databind.JsonNode;
import com.iso.hypo.admin.papi.dto.LocalizedStringDto;
import com.iso.hypo.admin.papi.dto.model.BrandDto;
import com.iso.hypo.admin.papi.dto.model.CourseDto;
import com.iso.hypo.admin.papi.dto.patch.PatchCourseDto;
import com.iso.hypo.admin.papi.dto.post.PostCourseDto;
import com.iso.hypo.admin.papi.dto.put.PutBrandDto;
import com.iso.hypo.admin.papi.dto.put.PutCoachDto;
import com.iso.hypo.admin.papi.dto.put.PutCourseDto;
import com.iso.hypo.admin.papi.dto.ErrorDto;
import com.iso.hypo.domain.BrandBuilder;
import com.iso.hypo.domain.CoachBuilder;
import com.iso.hypo.domain.CourseBuilder;
import com.iso.hypo.domain.GymBuilder;
import com.iso.hypo.domain.aggregate.Brand;
import com.iso.hypo.domain.aggregate.Coach;
import com.iso.hypo.domain.aggregate.Course;
import com.iso.hypo.domain.aggregate.Gym;
import com.iso.hypo.services.exception.CourseException;
import com.iso.hypo.repositories.BrandRepository;
import com.iso.hypo.repositories.CoachRepository;
import com.iso.hypo.repositories.CourseRepository;
import com.iso.hypo.repositories.GymRepository;
import com.iso.hypo.tests.http.HttpUtils;
import com.iso.hypo.tests.security.Roles;
import com.iso.hypo.tests.security.Users;

import net.datafaker.Faker;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
class CourseControllerTests {

	public static final String listURI = "/v1/brands/%s/gyms/%s/courses";
	public static final String postURI = "/v1/brands/%s/gyms/%s/courses";
	public static final String getURI = "/v1/brands/%s/gyms/%s/courses/%s";
	public static final String putURI = "/v1/brands/%s/gyms/%s/courses/%s";
	public static final String postActivateURI = "/v1/brands/%s/gyms/%s/courses/%s/activate";
	public static final String postDeactivateURI = "/v1/brands/%s/gyms/%s/courses/%s/deactivate";
	public static final String patchURI = "/v1/brands/%s/gyms/%s/courses/%s";
	public static final String pageNumber = "page";
	public static final String pageSize = "pageSize";
	public static final String includeInactive = "includeInactive";

	public static final String brandCode_1 = "CourseBrand1";
	public static final String brandCode_2 = "CourseBrand2";
	
	public static final String gymCode_1 = "CourseGym1";
	public static final String gymCode_2 = "CourseGym2";
	
	@LocalServerPort
	private int port;

	@Autowired
	BrandRepository brandRepository;
	
	@Autowired
	GymRepository gymRepository;
	
	@Autowired
	CourseRepository courseRepository;
	
	@Autowired
	CoachRepository coachRepository;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	ModelMapper modelMapper;

	private Faker faker = new Faker();

	private TestRestTemplate testRestTemplate = new TestRestTemplate();

	private List<Coach> coachs = new ArrayList<Coach>();
	private Course course;
	private Course courseIsDeleted;
	private Brand brand_1;
	private Brand brand_2;
	private Gym gym_1;
	private Gym gym_2;
	private List<Course> courses = new ArrayList<Course>();

	@BeforeAll
	void arrange() {
		testRestTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());

		courseRepository.deleteAll();

		brand_1 = BrandBuilder.build(brandCode_1, faker.company().name());
		brandRepository.save(brand_1);
		
		brand_2 = BrandBuilder.build(brandCode_2, faker.company().name());
		brandRepository.save(brand_1);
		
		gym_1 = GymBuilder.build(brand_1.getUuid(), gymCode_1, faker.address().cityName());
		gymRepository.save(gym_1);
		
		gym_2 = GymBuilder.build(brand_2.getUuid(), gymCode_2, faker.address().cityName());
		gymRepository.save(gym_2);
		
		
		for (int i = 0; i < 5; i++) {
			Coach coach = CoachBuilder.build(brand_1.getUuid(), gym_1.getUuid());
			coach = coachRepository.save(coach);
			coachs.add(coach);
		}
		
		course = CourseBuilder.build(brand_1.getUuid(), gym_1.getUuid(), coachs);
		course = courseRepository.save(course);

		courseIsDeleted = CourseBuilder.build(brand_2.getUuid(), gym_1.getUuid(), coachs);
		courseIsDeleted.setDeleted(true);
		courseIsDeleted = courseRepository.save(courseIsDeleted);

		for (int i = 0; i < 10; i++) {
			Course item = CourseBuilder.build(brand_2.getUuid(), gym_1.getUuid(), coachs);
			item = courseRepository.save(item);
			courses.add(item);
		}

		for (int i = 0; i < 4; i++) {
			Course item = CourseBuilder.build(brand_2.getUuid(), gym_2.getUuid(), null);
			item = courseRepository.save(item);
			courses.add(item);
		}

		Course item = CourseBuilder.build(brand_2.getUuid(), gym_2.getUuid(), null);
		item.setActive(false);
		courseRepository.save(item);
	}

	@AfterAll
	void cleanup() {
		// Cleanup
		// courseRepository.deleteAll();
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
				HttpUtils.createURL(URI.create(String.format(listURI, brand_2.getUuid(), gym_2.getUuid())), port, params), HttpMethod.GET,
				httpEntity, JsonNode.class);

		// Assert
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		Page<CourseDto> page = objectMapper.convertValue(response.getBody(), new TypeReference<Page<CourseDto>>() {});

		// Assert
		Assertions.assertEquals(0, page.getPageable().getPageNumber(),
				String.format("Course list first page number invalid: %d", page.getPageable().getPageNumber()));
		Assertions.assertEquals(4, page.getNumberOfElements(),
				String.format("Course list first page number of elements invalid: %d", page.getNumberOfElements()));
		Assertions.assertEquals(4, page.getTotalElements(),
				String.format("Course total number of elements invalid: %d", page.getTotalElements()));

		page.get().forEach(course -> Assertions.assertTrue(course.isActive()));
		page.get().forEach(course -> Assertions.assertTrue(course.isDeleted() == false));
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
				HttpUtils.createURL(URI.create(String.format(listURI, brand_2.getUuid(), gym_2.getUuid())), port, params), HttpMethod.GET,
				httpEntity, JsonNode.class);

		// Assert
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		Page<CourseDto> page = objectMapper.convertValue(response.getBody(), new TypeReference<Page<CourseDto>>() {});

		// Assert
		Assertions.assertEquals(0, page.getPageable().getPageNumber(),
				String.format("Course list first page number invalid: %d", page.getPageable().getPageNumber()));
		Assertions.assertEquals(5, page.getNumberOfElements(),
				String.format("Course list first page number of elements invalid: %d", page.getNumberOfElements()));
		Assertions.assertEquals(5, page.getTotalElements(),
				String.format("Course total number of elements invalid: %d", page.getTotalElements()));

		page.get().forEach(course -> Assertions.assertTrue(course.isDeleted() == false));
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
				HttpUtils.createURL(URI.create(String.format(listURI, brand_2.getUuid(), gym_2.getUuid())), port, params), HttpMethod.GET,
				httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		Page<CourseDto> page = objectMapper.convertValue(response.getBody(), new TypeReference<Page<CourseDto>>() {});

		// Assert
		Assertions.assertEquals(1, page.getPageable().getPageNumber(),
				String.format("Course list second page number invalid: %d", page.getPageable().getPageNumber()));
		Assertions.assertEquals(2, page.getNumberOfElements(),
				String.format("Course list second page number of elements invalid: %d", page.getNumberOfElements()));
		
		page.get().forEach(course -> Assertions.assertTrue(course.isActive()));
		page.get().forEach(course -> Assertions.assertTrue(course.isDeleted() == false));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPostSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostCourseDto postCourse = modelMapper.map(CourseBuilder.build(brand_1.getUuid(), gym_1.getUuid(), coachs), PostCourseDto.class);

		HttpEntity<PostCourseDto> httpEntity = HttpUtils.createHttpEntity(role, user, postCourse);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postURI, brand_1.getUuid(), gym_1.getUuid())), port, null), HttpMethod.POST,
				httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));

		CourseDto created = objectMapper.convertValue(response.getBody(), CourseDto.class);
		assertCourse(modelMapper.map(postCourse, CourseDto.class), created);
	}

	@Test
	void testPostDuplicateFailure() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostCourseDto postCourse = modelMapper.map(CourseBuilder.build(brand_1.getUuid(), gym_1.getUuid(),  null), PostCourseDto.class);
		HttpEntity<PostCourseDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postCourse);

		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postURI, brand_1.getUuid(), gym_1.getUuid())), port, null), HttpMethod.POST,
				httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));

		// Act
		response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postURI, brand_1.getUuid(), gym_1.getUuid())), port, null), HttpMethod.POST,
				httpEntity, JsonNode.class);
		
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
		
		CourseDto dupBody = objectMapper.convertValue(response.getBody(), CourseDto.class);
		Assertions.assertEquals(1, dupBody.getMessages().size(),
				String.format("Duplicate error ,missing message: %s", dupBody.getMessages().size()));
		
		Assertions.assertEquals(CourseException.COURSE_CODE_ALREADY_EXIST, dupBody.getMessages().getFirst().getCode(),
				String.format("Duplicate error, missing message: %s", dupBody.getMessages().getFirst().getCode()));
	}
	
	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testGetSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostCourseDto postCourse = modelMapper.map(CourseBuilder.build(brand_1.getUuid(), gym_1.getUuid(), coachs), PostCourseDto.class);

		HttpEntity<PostCourseDto> httpEntity = HttpUtils.createHttpEntity(role, user, postCourse);

		ResponseEntity<JsonNode> responsePost = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postURI, brand_1.getUuid(), gym_1.getUuid())), port, null), HttpMethod.POST,
				httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.CREATED, responsePost.getStatusCode(),
				String.format("Post error: %s", responsePost.getStatusCode()));

		// Act
		httpEntity = HttpUtils.createHttpEntity(role, user, null);
		CourseDto created = objectMapper.convertValue(responsePost.getBody(), CourseDto.class);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(getURI, brand_1.getUuid(), gym_1.getUuid(), created.getUuid())), port, null),
				HttpMethod.GET, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));

		CourseDto fetched = objectMapper.convertValue(response.getBody(), CourseDto.class);
		assertCourse(modelMapper.map(postCourse, CourseDto.class), fetched);
	}
	
	@Test
	void testGetFailureNotFound() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		HttpEntity<Object> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, null);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(getURI, brand_1.getUuid(), gym_1.getUuid(), faker.code().isbn10())), port, null),
				HttpMethod.GET, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPutSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Course courseToUpdate = CourseBuilder.build(brand_1.getUuid(), gym_1.getUuid(), coachs);
		courseToUpdate.setActive(false);
		courseToUpdate.setActivatedOn(null);
		courseToUpdate.setDeactivatedOn(null);
		courseToUpdate = courseRepository.save(courseToUpdate);

		Course updatedCourse = CourseBuilder.build(brand_1.getUuid(), gym_1.getUuid(), coachs);
		updatedCourse.setUuid(courseToUpdate.getUuid());
		updatedCourse.setActive(false);
		updatedCourse.setActivatedOn(null);
		updatedCourse.setDeactivatedOn(null);

		PutCourseDto putCourse = modelMapper.map(updatedCourse, PutCourseDto.class);
		putCourse.setUuid(courseToUpdate.getUuid());

		// Act
		HttpEntity<PutCourseDto> httpEntity = HttpUtils.createHttpEntity(role, user, putCourse);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, brand_1.getUuid(), gym_1.getUuid(), putCourse.getUuid())), port, null),
				HttpMethod.PUT, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put error: %s", response.getStatusCode()));

		CourseDto updated = objectMapper.convertValue(response.getBody(), CourseDto.class);
		assertCourse(modelMapper.map(updatedCourse, CourseDto.class), updated);
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPutNullSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Course courseToUpdate = CourseBuilder.build(brand_1.getUuid(), gym_1.getUuid(),  null);
		courseToUpdate.setActive(false);
		courseToUpdate.setActivatedOn(null);
		courseToUpdate.setDeactivatedOn(null);
		courseToUpdate = courseRepository.save(courseToUpdate);

		Course updatedCourse = CourseBuilder.build(brand_1.getUuid(), gym_1.getUuid(),  null);

		PutCourseDto courseToUpdateDto = modelMapper.map(courseToUpdate, PutCourseDto.class);
		
		PutCourseDto putCourse = modelMapper.map(updatedCourse, PutCourseDto.class);
		putCourse.setUuid(courseToUpdate.getUuid());
		putCourse.setCode(courseToUpdateDto.getCode());
		putCourse.setName(courseToUpdateDto.getName());
		putCourse.setDescription(null);

		// Act
		HttpEntity<PutCourseDto> httpEntity = HttpUtils.createHttpEntity(role, user, putCourse);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, brand_1.getUuid(), gym_1.getUuid(), putCourse.getUuid())), port, null),
				HttpMethod.PUT, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put null error: %s", response.getStatusCode()));

		courseToUpdate.setDescription(null);

		CourseDto updated = objectMapper.convertValue(response.getBody(), CourseDto.class);
		assertCourse(modelMapper.map(courseToUpdate, CourseDto.class), updated);
	}
	
	@Test
	void testPutFailureNotFound() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		Course courseToUpdate = CourseBuilder.build(brand_1.getUuid(), gym_1.getUuid(),  null);
		PutCourseDto courseToUpdateDto = modelMapper.map(courseToUpdate, PutCourseDto.class);
		
		HttpEntity<PutCourseDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, courseToUpdateDto);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, brand_1.getUuid(), gym_1.getUuid(), courseToUpdate.getUuid())), port, null),
				HttpMethod.PUT, httpEntity, JsonNode.class);

		// Assert status first
		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));

		// If there's a body, try to map to ErrorDto and validate the error code
		if (response.getBody() != null && response.getBody().size() > 0) {
			ErrorDto error = objectMapper.convertValue(response.getBody(), ErrorDto.class);
			Assertions.assertEquals(CourseException.COURSE_NOT_FOUND, error.getCode(),
					String.format("Unexpected error code: %s", error.getCode()));
		}
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testActivateSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Course courseToActivate = CourseBuilder.build(brand_1.getUuid(), gym_1.getUuid(),  null);
		courseToActivate.setActive(false);
		courseToActivate.setActivatedOn(null);
		courseToActivate.setDeactivatedOn(null);
		courseToActivate = courseRepository.save(courseToActivate);

		courseToActivate.setActive(true);
		courseToActivate.setActivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		courseToActivate.setDeactivatedOn(null);

		// Act
		HttpEntity<PutCourseDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postActivateURI, brand_1.getUuid(), gym_1.getUuid(), courseToActivate.getUuid())),
						port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Course activation error: %s", response.getStatusCode()));

		CourseDto activated = objectMapper.convertValue(response.getBody(), CourseDto.class);
		assertCourse(modelMapper.map(courseToActivate, CourseDto.class), activated);
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testActivateFailure(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange

		// Act
		HttpEntity<PutCourseDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<Object> response = testRestTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(postActivateURI, brand_1.getUuid(), gym_1.getUuid(), faker.code().isbn10())), port, null),
				HttpMethod.POST, httpEntity, Object.class);
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																											
		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Course activation error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testDeactivateSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Course courseToDeactivate = CourseBuilder.build(brand_1.getUuid(), gym_1.getUuid(),  null);
		courseToDeactivate.setActive(true);
		courseToDeactivate.setActivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		courseToDeactivate = courseRepository.save(courseToDeactivate);

		courseToDeactivate.setActive(false);
		courseToDeactivate.setDeactivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));

		// Act
		HttpEntity<PutCourseDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(
				URI.create(String.format(postDeactivateURI, brand_1.getUuid(), gym_1.getUuid(), courseToDeactivate.getUuid())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Course deactivation error: %s", response.getStatusCode()));

		CourseDto deactivated = objectMapper.convertValue(response.getBody(), CourseDto.class);
		assertCourse(modelMapper.map(courseToDeactivate, CourseDto.class), deactivated);
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testDeactivateFailure(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange

		// Act
		HttpEntity<PutCourseDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<Object> response = testRestTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(postDeactivateURI, brand_1.getUuid(), gym_1.getUuid(), faker.code().ean13())), port, null),
				HttpMethod.POST, httpEntity, Object.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Course activation error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPatchSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Course courseToPatch = CourseBuilder.build(brand_1.getUuid(), gym_1.getUuid(),  null);
		courseToPatch = courseRepository.save(courseToPatch);

		PatchCourseDto patchCourse = modelMapper.map(courseToPatch, PatchCourseDto.class);
		patchCourse.setUuid(courseToPatch.getUuid());
		patchCourse.setDescription(null);
		patchCourse.setName(null);

		// Act
		HttpEntity<PatchCourseDto> httpEntity = HttpUtils.createHttpEntity(role, user, patchCourse);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(patchURI, brand_1.getUuid(), gym_1.getUuid(), patchCourse.getUuid())), port, null),
				HttpMethod.PATCH, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));

		CourseDto patched = objectMapper.convertValue(response.getBody(), CourseDto.class);
		assertCourse(modelMapper.map(patchCourse, CourseDto.class), patched);
	}


	public static final void assertCourse(CourseDto expected, CourseDto result) {
		if (expected.getUuid() != null) {
			Assertions.assertEquals(expected.getUuid(), result.getUuid());
		}
		
		Assertions.assertEquals(expected.getBrandUuid(), result.getBrandUuid());
		Assertions.assertEquals(expected.getGymUuid(), result.getGymUuid());
		Assertions.assertEquals(expected.getCode(), result.getCode());
		Assertions.assertEquals(expected.isActive(), result.isActive());

		if (expected.getStartDate() != null) {
			LocalDate expecteStart = expected.getStartDate()
			        .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			LocalDate resultStart = result.getStartDate()
			        .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			Assertions.assertEquals(expecteStart, resultStart);
		}
		
		if (expected.getEndDate() != null) {
			LocalDate expecteEnd = expected.getEndDate()
			        .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			LocalDate resultEnd = result.getEndDate()
			        .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			Assertions.assertEquals(expecteEnd, resultEnd);
		}
		
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
	}
}