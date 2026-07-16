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
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.iso.hypo.admin.papi.dto.ErrorDto;
import com.iso.hypo.admin.papi.dto.LocalizedStringDto;
import com.iso.hypo.admin.papi.dto.model.CourseDto;
import com.iso.hypo.admin.papi.dto.patch.PatchCourseDto;
import com.iso.hypo.admin.papi.dto.post.PostCourseDto;
import com.iso.hypo.admin.papi.dto.put.PutCourseDto;
import com.iso.hypo.brand.application.exception.CourseException;
import com.iso.hypo.brand.domain.model.Brand;
import com.iso.hypo.brand.domain.model.Course;
import com.iso.hypo.brand.domain.repository.BrandRepository;
import com.iso.hypo.brand.domain.repository.CourseRepository;
import com.iso.hypo.common.application.dto.PageResultDto;
import com.iso.hypo.common.application.security.Roles;
import com.iso.hypo.domain.BrandBuilder;
import com.iso.hypo.domain.CourseBuilder;
import com.iso.hypo.tests.http.HttpUtils;
import com.iso.hypo.tests.security.Users;

import net.datafaker.Faker;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper.Builder;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "app.test.run=true")
@TestInstance(Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class CourseControllerTests {

	public static final String listURI = "/v1/brands/%s/courses";
	public static final String postURI = "/v1/brands/%s/courses";
	public static final String getURI = "/v1/brands/%s/courses/%s";
	public static final String putURI = "/v1/brands/%s/courses/%s";
	public static final String postActivateURI = "/v1/brands/%s/courses/%s/activate";
	public static final String postDeactivateURI = "/v1/brands/%s/courses/%s/deactivate";
	public static final String patchURI = "/v1/brands/%s/courses/%s";
	public static final String deleteURI = "/v1/brands/%s/courses/%s";
	public static final String pageNumber = "page";
	public static final String pageSize = "pageSize";
	public static final String includeInactive = "includeInactive";

	public static final String brandCode_1 = "CourseBrand1";
	public static final String brandCode_2 = "CourseBrand2";
	
	@LocalServerPort
	private int port;

	@Autowired
	BrandRepository brandRepository;
	@Autowired
	CourseRepository courseRepository;
	@Autowired
	Builder objectMapper;
	@Autowired
	ModelMapper modelMapper;

	private Faker faker = new Faker();

	private RestTestClient restClient;

	private Course course;
	private Course courseDeleted;
	private Brand brand_1;
	private Brand brand_2;
	private List<Course> courses = new ArrayList<Course>();

	@BeforeAll
	void arrange() {
		restClient = RestTestClient.bindToServer()
		        .baseUrl("http://localhost:" + port)
		        .configureMessageConverters(converters -> 
		        	converters.addCustomConverter(
		        			new JacksonJsonHttpMessageConverter(
		        			objectMapper
		        			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
		        			.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false))))
		        .build();

		courseRepository.deleteAll();

		brand_1 = BrandBuilder.build(brandCode_1, faker.company().name());
		brandRepository.save(brand_1);
		
		brand_2 = BrandBuilder.build(brandCode_2, faker.company().name());
		brandRepository.save(brand_2);
		
		course = CourseBuilder.build(brand_1.getUuid());
		course = courseRepository.save(course);

		courseDeleted = CourseBuilder.build(brand_2.getUuid());
		courseDeleted.setDeleted(true);
		courseDeleted = courseRepository.save(courseDeleted);

		for (int i = 0; i < 10; i++) {
			Course item = CourseBuilder.build(brand_2.getUuid());
			item = courseRepository.save(item);
			courses.add(item);
		}

		for (int i = 0; i < 4; i++) {
			Course item = CourseBuilder.build(brand_2.getUuid());
			item = courseRepository.save(item);
			courses.add(item);
		}

		Course item = CourseBuilder.build(brand_2.getUuid());
		item.setActive(false);
		courseRepository.save(item);
	}

	@AfterAll
	void cleanup() {
		// Cleanup
		// courseRepository.deleteAll();
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testListActiveSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "0");
		params.add(pageSize, "5");
		params.add(includeInactive, "false");

		// Act
		PageResultDto<CourseDto> page = 
				this.restClient.get()
					.uri(HttpUtils.createURL(URI.create(String.format(listURI, brand_2.getUuid())), port, params))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(new ParameterizedTypeReference<PageResultDto<CourseDto>>() {}) 
					.returnResult()
				    .getResponseBody(); 
		
		// Assert
		Assertions.assertEquals(0, page.getPageNumber(),
				String.format("Course list first page number invalid: %d", page.getPageNumber()));
		Assertions.assertEquals(5, page.getContent().size(),
				String.format("Course list first page number of elements invalid: %d", page.getContent().size()));
		Assertions.assertEquals(14, page.getTotalElements(),
				String.format("Course total number of elements invalid: %d", page.getTotalElements()));

		page.getContent().forEach(course -> Assertions.assertTrue(course.isActive()));
		page.getContent().forEach(course -> Assertions.assertTrue(course.isDeleted() == false));
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testListFirstPageSuccess(String role, String user)	throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "0");
		params.add(pageSize, "5");
		params.add(includeInactive, "true");

		// Act
		PageResultDto<CourseDto> page = 
				this.restClient.get()
					.uri(HttpUtils.createURL(URI.create(String.format(listURI, brand_2.getUuid())), port, params))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(new ParameterizedTypeReference<PageResultDto<CourseDto>>() {}) 
					.returnResult()
				    .getResponseBody(); 
		
		// Assert
		Assertions.assertEquals(0, page.getPageNumber(),
				String.format("Course list first page number invalid: %d", page.getPageNumber()));
		Assertions.assertEquals(5, page.getContent().size(),
				String.format("Course list first page number of elements invalid: %d", page.getContent().size()));
		Assertions.assertEquals(15, page.getTotalElements(),
				String.format("Course total number of elements invalid: %d", page.getTotalElements()));

		page.getContent().forEach(course -> Assertions.assertTrue(course.isDeleted() == false));
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testListSecondPageSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "1");
		params.add(pageSize, "2");

		// Act
		PageResultDto<CourseDto> page = 
				this.restClient.get()
					.uri(HttpUtils.createURL(URI.create(String.format(listURI, brand_2.getUuid())), port, params))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(new ParameterizedTypeReference<PageResultDto<CourseDto>>() {}) 
					.returnResult()
				    .getResponseBody(); 

		// Assert
		Assertions.assertEquals(1, page.getPageNumber(),
				String.format("Course list second page number invalid: %d", page.getPageNumber()));
		Assertions.assertEquals(2, page.getContent().size(),
				String.format("Course list second page number of elements invalid: %d", page.getContent().size()));
		
		page.getContent().forEach(course -> Assertions.assertTrue(course.isActive()));
		page.getContent().forEach(course -> Assertions.assertTrue(course.isDeleted() == false));
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPostSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostCourseDto postDto = modelMapper.map(CourseBuilder.build(brand_1.getUuid()), PostCourseDto.class);

		// Act
		CourseDto createdDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(postURI, brand_1.getUuid())), port, null))					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(postDto)
					.exchange() 
				    .expectStatus().isCreated() 
					.expectBody(CourseDto.class) 
					.returnResult()
				    .getResponseBody(); 
		
		// Assert
		assertCourse(modelMapper.map(postDto, CourseDto.class), createdDto);
	}

	@Test
	void testPostDuplicateFailure() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostCourseDto postDto = modelMapper.map(CourseBuilder.build(brand_1.getUuid()), PostCourseDto.class);
		
		CourseDto _ = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(postURI, brand_1.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(postDto)
					.exchange() 
				    .expectStatus().isCreated() 
					.expectBody(CourseDto.class) 
					.returnResult()
				    .getResponseBody(); 

		// Act
		CourseDto dupDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(postURI, brand_1.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(postDto)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(CourseDto.class) 
					.returnResult()
				    .getResponseBody(); 

		Assertions.assertEquals(1, dupDto.getMessages().size(),
				String.format("Duplicate error ,missing message: %s", dupDto.getMessages().size()));
		
		Assertions.assertEquals(CourseException.COURSE_CODE_ALREADY_EXIST, dupDto.getMessages().getFirst().getCode(),
				String.format("Duplicate error, missing message: %s", dupDto.getMessages().getFirst().getCode()));
	}
	
	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPostFailureForbiddenBrandMismatch(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostCourseDto postDto = modelMapper.map(CourseBuilder.build(brand_1.getUuid()), PostCourseDto.class);
		
		// Act
		ErrorDto _ = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(postURI, faker.code().isbn10())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(postDto)
					.exchange() 
				    .expectStatus().isForbidden() 
					.expectBody(ErrorDto.class) 
					.returnResult()
				    .getResponseBody(); 
	}

	
	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testGetSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostCourseDto postDto = modelMapper.map(CourseBuilder.build(brand_1.getUuid()), PostCourseDto.class);

		CourseDto createdDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(postURI, brand_1.getUuid())), port, null))					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(postDto)
					.exchange() 
				    .expectStatus().isCreated() 
					.expectBody(CourseDto.class) 
					.returnResult()
				    .getResponseBody();

		// Act
		CourseDto fetchedDto = 
				this.restClient.get()
					.uri(HttpUtils.createURL(URI.create(String.format(getURI, brand_1.getUuid(), createdDto.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(CourseDto.class) 
					.returnResult()
				    .getResponseBody();
		
		// Assert
		assertCourse(modelMapper.map(postDto, CourseDto.class), fetchedDto);
	}
	
	@Test
	void testGetFailureNotFound() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		ErrorDto _ = 
				this.restClient.get()
					.uri(HttpUtils.createURL(URI.create(String.format(getURI, brand_1.getUuid(), faker.code().isbn10())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isNotFound() 
					.expectBody(ErrorDto.class) 
					.returnResult()
				    .getResponseBody();  
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPutSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Course courseToUpdate = CourseBuilder.build(brand_1.getUuid());
		courseToUpdate = courseRepository.save(courseToUpdate);

		PutCourseDto putDto = modelMapper.map(courseToUpdate, PutCourseDto.class);
		putDto.setUuid(courseToUpdate.getUuid());

		if (putDto.getName() != null && !putDto.getName().isEmpty()) {
			putDto.getName().get(0).setText(putDto.getName().get(0).getText() + " - updated");
		}

		if (putDto.getDescription() != null && !putDto.getDescription().isEmpty()) {
			putDto.getDescription().get(0).setText(putDto.getDescription().get(0).getText() + " - updated");
		}

		// Act
		CourseDto updatedDto = 
				this.restClient.put()
					.uri(HttpUtils.createURL(URI.create(String.format(putURI, brand_1.getUuid(), putDto.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(putDto)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(CourseDto.class) 
					.returnResult()
				    .getResponseBody();
		
		// Assert
		assertCourse(modelMapper.map(putDto, CourseDto.class), updatedDto);
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPutNullSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Course courseToUpdate = CourseBuilder.build(brand_1.getUuid());
		courseToUpdate.setActive(false);
		courseToUpdate.setActivatedOn(null);
		courseToUpdate.setDeactivatedOn(null);
		courseToUpdate = courseRepository.save(courseToUpdate);

		Course updatedCourse = CourseBuilder.build(brand_1.getUuid());

		PutCourseDto courseToUpdateDto = modelMapper.map(courseToUpdate, PutCourseDto.class);
		
		PutCourseDto putDto = modelMapper.map(updatedCourse, PutCourseDto.class);
		putDto.setUuid(courseToUpdate.getUuid());
		putDto.setCode(courseToUpdateDto.getCode());
		putDto.setName(courseToUpdateDto.getName());
		putDto.setDescription(null);

		// Act
		CourseDto updatedDto = 
				this.restClient.put()
					.uri(HttpUtils.createURL(URI.create(String.format(putURI, brand_1.getUuid(), putDto.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(putDto)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(CourseDto.class) 
					.returnResult()
				    .getResponseBody();
		
		// Assert
		courseToUpdate.setDescription(null);

		assertCourse(modelMapper.map(courseToUpdate, CourseDto.class), updatedDto);
	}
	
	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPutFailureNotFound(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		Course putDto = CourseBuilder.build(brand_1.getUuid());
		PutCourseDto patchDto = modelMapper.map(putDto, PutCourseDto.class);
		
		// Act
		ErrorDto _ = 
				this.restClient.put()
					.uri(HttpUtils.createURL(URI.create(String.format(putURI, brand_1.getUuid(), patchDto.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(putDto)
					.exchange() 
				    .expectStatus().isNotFound() 
					.expectBody(ErrorDto.class) 
					.returnResult()
				    .getResponseBody(); 
	}
	
	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPutFailureForbiddenBrandMismatch(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PutCourseDto putDto = modelMapper.map(CourseBuilder.build(brand_1.getUuid()), PutCourseDto.class);
		
		// Act
		ErrorDto _ = 
				this.restClient.put()
					.uri(HttpUtils.createURL(URI.create(String.format(putURI, faker.code().isbn10(), putDto.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(putDto)
					.exchange() 
				    .expectStatus().isForbidden() 
					.expectBody(ErrorDto.class) 
					.returnResult()
				    .getResponseBody(); 
	}
	
	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPutFailureForbiddenUuidMismatch(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PutCourseDto putDto = modelMapper.map(CourseBuilder.build(brand_1.getUuid()), PutCourseDto.class);
		
		// Act
		ErrorDto _ = 
				this.restClient.put()
					.uri(HttpUtils.createURL(URI.create(String.format(putURI, brand_1.getUuid(), faker.code().isbn10())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(putDto)
					.exchange() 
				    .expectStatus().isForbidden() 
					.expectBody(ErrorDto.class) 
					.returnResult()
				    .getResponseBody(); 
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPatchSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Course courseToPatch = CourseBuilder.build(brand_1.getUuid());
		courseToPatch = courseRepository.save(courseToPatch);

		PatchCourseDto patchDto = modelMapper.map(courseToPatch, PatchCourseDto.class);
		patchDto.setUuid(courseToPatch.getUuid());
		patchDto.setStartDate(Date.from(Instant.now().plus(5, ChronoUnit.DAYS)));
		patchDto.setName(null);
		patchDto.setDescription(null);
		
		courseToPatch.setStartDate(patchDto.getStartDate());
		
		// Act
		CourseDto patchedDto = 
				this.restClient.patch()
					.uri(HttpUtils.createURL(URI.create(String.format(patchURI, brand_1.getUuid(), patchDto.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(patchDto)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(CourseDto.class) 
					.returnResult()
				    .getResponseBody(); 
		
		// Assert
		assertCourse(modelMapper.map(courseToPatch, CourseDto.class), patchedDto);
	}

	
	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPatchFailureNotFound(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		Course patchTarget = CourseBuilder.build(brand_1.getUuid());
		PatchCourseDto patchDto = modelMapper.map(patchTarget, PatchCourseDto.class);
		
		// Act
		ErrorDto _ = 
				this.restClient.patch()
					.uri(HttpUtils.createURL(URI.create(String.format(patchURI, brand_1.getUuid(), patchDto.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(patchDto)
					.exchange() 
				    .expectStatus().isNotFound() 
					.expectBody(ErrorDto.class) 
					.returnResult()
				    .getResponseBody(); 
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPatchFailureForbiddenBrandMismatch(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PatchCourseDto patchDto = modelMapper.map(CourseBuilder.build(brand_1.getUuid()), PatchCourseDto.class);
		
		// Act
		ErrorDto _ = 
				this.restClient.patch()
					.uri(HttpUtils.createURL(URI.create(String.format(patchURI, faker.code().isbn10(), patchDto.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(patchDto)
					.exchange() 
				    .expectStatus().isForbidden() 
					.expectBody(ErrorDto.class) 
					.returnResult()
				    .getResponseBody(); 
	}
	
	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPatchFailureForbiddenUuidMismatch(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PatchCourseDto patchDto = modelMapper.map(CourseBuilder.build(brand_1.getUuid()), PatchCourseDto.class);

		// Act
		ErrorDto _ = 
				this.restClient.patch()
					.uri(HttpUtils.createURL(URI.create(String.format(patchURI, brand_1.getUuid(), faker.code().isbn10())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(patchDto)
					.exchange() 
				    .expectStatus().isForbidden() 
					.expectBody(ErrorDto.class) 
					.returnResult()
				    .getResponseBody(); 
	}
	

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testActivateSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Course courseToActivate = CourseBuilder.build(brand_1.getUuid());
		courseToActivate.setActive(false);
		courseToActivate.setActivatedOn(null);
		courseToActivate.setDeactivatedOn(null);
		courseToActivate = courseRepository.save(courseToActivate);

		courseToActivate.setActive(true);
		courseToActivate.setActivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		courseToActivate.setDeactivatedOn(null);

		// Act
		CourseDto activatedDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(postActivateURI, brand_1.getUuid(), courseToActivate.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(CourseDto.class) 
					.returnResult()
				    .getResponseBody(); 
		
		// Assert
		assertCourse(modelMapper.map(courseToActivate, CourseDto.class), activatedDto);
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testActivateFailureNotFound(String role, String user) throws JsonProcessingException, MalformedURLException {		
		// Act
		ErrorDto _ = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(postActivateURI, brand_1.getUuid(), faker.code().isbn10())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isNotFound() 
					.expectBody(ErrorDto.class) 
					.returnResult()
				    .getResponseBody(); 
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testDeactivateSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Course courseToDeactivate = CourseBuilder.build(brand_1.getUuid());
		courseToDeactivate.setActive(true);
		courseToDeactivate.setActivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		courseToDeactivate = courseRepository.save(courseToDeactivate);

		courseToDeactivate.setActive(false);
		courseToDeactivate.setDeactivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));

		// Act
		CourseDto deactivatedDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(postDeactivateURI, brand_1.getUuid(), courseToDeactivate.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(CourseDto.class) 
					.returnResult()
				    .getResponseBody(); 

		assertCourse(modelMapper.map(courseToDeactivate, CourseDto.class), deactivatedDto);
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testDeactivateFailureNotFound(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Act
		ErrorDto _ = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(postDeactivateURI, brand_1.getUuid(), faker.code().isbn10())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isNotFound() 
					.expectBody(ErrorDto.class) 
					.returnResult()
				    .getResponseBody(); 
	}

	@Test
	void testDeleteSuccess() throws JsonProcessingException, MalformedURLException {
		// Arrange
		Course courseToDelete = CourseBuilder.build(brand_1.getUuid());
		courseToDelete = courseRepository.save(courseToDelete);

		// Act
		CourseDto _ = 
				this.restClient.delete()
					.uri(HttpUtils.createURL(URI.create(String.format(deleteURI, brand_1.getUuid(), courseToDelete.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isAccepted() 
					.expectBody(CourseDto.class) 
					.returnResult()
				    .getResponseBody(); 
		
		// Assert
		ErrorDto _ = 
				this.restClient.get()
					.uri(HttpUtils.createURL(URI.create(String.format(getURI, brand_1.getUuid(), courseToDelete.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isNotFound() 
					.expectBody(ErrorDto.class) 
					.returnResult()
				    .getResponseBody(); 
	}
	
	public static final void assertCourse(CourseDto expected, CourseDto result) {
		if (expected.getUuid() != null) {
			Assertions.assertEquals(expected.getUuid(), result.getUuid());
		}
		
		Assertions.assertEquals(expected.getBrandUuid(), result.getBrandUuid());
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
