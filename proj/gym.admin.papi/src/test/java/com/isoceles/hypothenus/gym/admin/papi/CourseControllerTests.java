package com.isoceles.hypothenus.gym.admin.papi;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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
import com.isoceles.hypothenus.gym.admin.papi.dto.CourseDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.ErrorDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.LocalizedStringDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.patch.PatchCourseDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.post.PostCourseDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.put.PutCourseDto;
import com.isoceles.hypothenus.gym.domain.model.aggregate.Course;
import com.isoceles.hypothenus.gym.domain.repository.CourseRepository;
import com.isoceles.hypothenus.tests.http.HttpUtils;
import com.isoceles.hypothenus.tests.model.CourseBuilder;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
class CourseControllerTests {

	public static final String listURI = "/v1/admin/gyms/%s/courses";
	public static final String postURI = "/v1/admin/gyms/%s/courses";
	public static final String getURI = "/v1/admin/gyms/%s/courses/%s";
	public static final String putURI = "/v1/admin/gyms/%s/courses/%s";
	public static final String postActivateURI = "/v1/admin/gyms/%s/courses/%s/activate";
	public static final String postDeactivateURI = "/v1/admin/gyms/%s/courses/%s/deactivate";
	public static final String patchURI = "/v1/admin/gyms/%s/courses/%s";
	public static final String pageNumber = "page";
	public static final String pageSize = "pageSize";
	public static final String isActive = "isActive";

	public static final String gymId_16034 = "16034";
	public static final String gymId_16035 = "16035";

	@LocalServerPort
	private int port;

	@Autowired
	CourseRepository courseRepository;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	ModelMapper modelMapper;

	private Faker faker = new Faker();

	private TestRestTemplate restTemplate = new TestRestTemplate();

	private Course course;
	private Course courseIsDeleted;
	private List<Course> courses = new ArrayList<Course>();

	@BeforeAll
	void arrange() {
		restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());

		courseRepository.deleteAll();

		course = CourseBuilder.build(gymId_16034);
		courseRepository.save(course);

		courseIsDeleted = CourseBuilder.build(gymId_16034);
		courseIsDeleted.setDeleted(true);
		courseIsDeleted = courseRepository.save(courseIsDeleted);

		for (int i = 0; i < 10; i++) {
			Course item = CourseBuilder.build(gymId_16034);
			courseRepository.save(item);
			courses.add(item);
		}

		for (int i = 0; i < 4; i++) {
			Course item = CourseBuilder.build(gymId_16035);
			courseRepository.save(item);
			courses.add(item);
		}

		Course item = CourseBuilder.build(gymId_16035);
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
		params.add(isActive, "true");

		// Act
		ResponseEntity<String> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(listURI, gymId_16035)), port, params), HttpMethod.GET,
				httpEntity, new ParameterizedTypeReference<String>() {
				});

		// Assert
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		Page<CourseDto> page = objectMapper.readValue(response.getBody(), new TypeReference<Page<CourseDto>>() {
		});

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
		params.add(isActive, "false");

		// Act
		ResponseEntity<String> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(listURI, gymId_16035)), port, params), HttpMethod.GET,
				httpEntity, new ParameterizedTypeReference<String>() {
				});

		// Assert
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		Page<CourseDto> page = objectMapper.readValue(response.getBody(), new TypeReference<Page<CourseDto>>() {
		});

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
		ResponseEntity<String> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(listURI, gymId_16035)), port, params), HttpMethod.GET,
				httpEntity, new ParameterizedTypeReference<String>() {
				});

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		Page<CourseDto> page = objectMapper.readValue(response.getBody(), new TypeReference<Page<CourseDto>>() {
		});

		// Assert
		Assertions.assertEquals(1, page.getPageable().getPageNumber(),
				String.format("Course list second page number invalid: %d", page.getPageable().getPageNumber()));
		Assertions.assertEquals(2, page.getNumberOfElements(),
				String.format("Course list second page number of elements invalid: %d", page.getNumberOfElements()));
		page.get().forEach(course -> Assertions.assertTrue(course.isDeleted() == false));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPostSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostCourseDto postCourse = modelMapper.map(CourseBuilder.build(gymId_16034), PostCourseDto.class);

		HttpEntity<PostCourseDto> httpEntity = HttpUtils.createHttpEntity(role, user, postCourse);

		// Act
		ResponseEntity<CourseDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postURI, gymId_16034)), port, null), HttpMethod.POST,
				httpEntity, CourseDto.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));

		assertCourse(modelMapper.map(postCourse, CourseDto.class), response.getBody());
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testGetSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostCourseDto postCourse = modelMapper.map(CourseBuilder.build(gymId_16034), PostCourseDto.class);

		HttpEntity<PostCourseDto> httpEntity = HttpUtils.createHttpEntity(role, user, postCourse);

		ResponseEntity<CourseDto> responsePost = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postURI, gymId_16034)), port, null), HttpMethod.POST,
				httpEntity, CourseDto.class);

		Assertions.assertEquals(HttpStatus.CREATED, responsePost.getStatusCode(),
				String.format("Post error: %s", responsePost.getStatusCode()));

		// Act
		httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<CourseDto> response = restTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(getURI, gymId_16034, responsePost.getBody().getId())), port, null),
				HttpMethod.GET, httpEntity, CourseDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));

		assertCourse(modelMapper.map(postCourse, CourseDto.class), response.getBody());
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPutSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Course courseToUpdate = CourseBuilder.build(gymId_16034);
		courseToUpdate.setActive(false);
		courseToUpdate.setActivatedOn(null);
		courseToUpdate.setDeactivatedOn(null);
		courseToUpdate = courseRepository.save(courseToUpdate);

		Course updatedCourse = CourseBuilder.build(gymId_16034);
		updatedCourse.setId(courseToUpdate.getId());
		updatedCourse.setActive(false);
		updatedCourse.setActivatedOn(null);
		updatedCourse.setDeactivatedOn(null);

		PutCourseDto putCourse = modelMapper.map(updatedCourse, PutCourseDto.class);
		putCourse.setId(courseToUpdate.getId());

		// Act
		HttpEntity<PutCourseDto> httpEntity = HttpUtils.createHttpEntity(role, user, putCourse);
		ResponseEntity<CourseDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, gymId_16034, putCourse.getId())), port, null),
				HttpMethod.PUT, httpEntity, CourseDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put error: %s", response.getStatusCode()));

		assertCourse(modelMapper.map(updatedCourse, CourseDto.class), response.getBody());
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPutNullSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Course courseToUpdate = CourseBuilder.build(gymId_16034);
		courseToUpdate.setActive(false);
		courseToUpdate.setActivatedOn(null);
		courseToUpdate.setDeactivatedOn(null);
		courseToUpdate = courseRepository.save(courseToUpdate);

		Course updatedCourse = CourseBuilder.build(gymId_16034);

		PutCourseDto putCourse = modelMapper.map(updatedCourse, PutCourseDto.class);
		putCourse.setId(courseToUpdate.getId());
		putCourse.setCode(null);
		putCourse.setDescription(null);
		putCourse.setName(null);

		// Act
		HttpEntity<PutCourseDto> httpEntity = HttpUtils.createHttpEntity(role, user, putCourse);
		ResponseEntity<CourseDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, gymId_16034, putCourse.getId())), port, null),
				HttpMethod.PUT, httpEntity, CourseDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put null error: %s", response.getStatusCode()));

		courseToUpdate.setCode(null);
		courseToUpdate.setDescription(null);
		courseToUpdate.setName(null);

		assertCourse(modelMapper.map(courseToUpdate, CourseDto.class), response.getBody());
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testActivateSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Course courseToActivate = CourseBuilder.build(gymId_16034);
		courseToActivate.setActive(false);
		courseToActivate.setActivatedOn(null);
		courseToActivate.setDeactivatedOn(null);
		courseToActivate = courseRepository.save(courseToActivate);

		courseToActivate.setActive(true);
		courseToActivate.setActivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		courseToActivate.setDeactivatedOn(null);

		// Act
		HttpEntity<PutCourseDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<CourseDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postActivateURI, gymId_16034, courseToActivate.getId())),
						port, null),
				HttpMethod.POST, httpEntity, CourseDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Course activation error: %s", response.getStatusCode()));

		assertCourse(modelMapper.map(courseToActivate, CourseDto.class), response.getBody());
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testActivateFailure(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange

		// Act
		HttpEntity<PutCourseDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<Object> response = restTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(postActivateURI, gymId_16034, faker.code().isbn10())), port, null),
				HttpMethod.POST, httpEntity, Object.class);
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																											
		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Course activation error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testDeactivateSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Course courseToDeactivate = CourseBuilder.build(gymId_16034);
		courseToDeactivate.setActive(true);
		courseToDeactivate.setActivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		courseToDeactivate = courseRepository.save(courseToDeactivate);

		courseToDeactivate.setActive(false);
		courseToDeactivate.setDeactivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));

		// Act
		HttpEntity<PutCourseDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<CourseDto> response = restTemplate.exchange(HttpUtils.createURL(
				URI.create(String.format(postDeactivateURI, gymId_16034, courseToDeactivate.getId())), port, null),
				HttpMethod.POST, httpEntity, CourseDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Course deactivation error: %s", response.getStatusCode()));

		assertCourse(modelMapper.map(courseToDeactivate, CourseDto.class), response.getBody());
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testDeactivateFailure(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange

		// Act
		HttpEntity<PutCourseDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<Object> response = restTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(postDeactivateURI, gymId_16034, faker.code().ean13())), port, null),
				HttpMethod.POST, httpEntity, Object.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Course activation error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPatchSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Course courseToPatch = CourseBuilder.build(gymId_16034);
		courseToPatch = courseRepository.save(courseToPatch);

		PatchCourseDto patchCourse = modelMapper.map(courseToPatch, PatchCourseDto.class);
		patchCourse.setId(courseToPatch.getId());
		patchCourse.setDescription(null);
		patchCourse.setName(null);

		// Act
		HttpEntity<PatchCourseDto> httpEntity = HttpUtils.createHttpEntity(role, user, patchCourse);
		ResponseEntity<CourseDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(patchURI, gymId_16034, patchCourse.getId())), port, null),
				HttpMethod.PATCH, httpEntity, CourseDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));

		courseToPatch.setCode(patchCourse.getCode());

		assertCourse(modelMapper.map(courseToPatch, CourseDto.class), response.getBody());
	}

	public static final void assertCourse(CourseDto expected, CourseDto result) {
		Assertions.assertEquals(expected.getId(), result.getId());
		Assertions.assertEquals(expected.getCode(), result.getCode());
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
