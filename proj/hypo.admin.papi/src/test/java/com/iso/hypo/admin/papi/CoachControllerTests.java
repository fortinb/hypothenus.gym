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
import com.iso.hypo.admin.papi.dto.contact.ContactDto;
import com.iso.hypo.admin.papi.dto.contact.PhoneNumberDto;
import com.iso.hypo.admin.papi.dto.model.CoachDto;
import com.iso.hypo.admin.papi.dto.patch.PatchCoachDto;
import com.iso.hypo.admin.papi.dto.post.PostCoachDto;
import com.iso.hypo.admin.papi.dto.put.PutCoachDto;
import com.iso.hypo.brand.domain.model.Brand;
import com.iso.hypo.brand.domain.model.Coach;
import com.iso.hypo.brand.domain.repository.BrandRepository;
import com.iso.hypo.brand.domain.repository.CoachRepository;
import com.iso.hypo.common.application.dto.PageResultDto;
import com.iso.hypo.common.application.security.Roles;
import com.iso.hypo.domain.BrandBuilder;
import com.iso.hypo.domain.CoachBuilder;
import com.iso.hypo.tests.http.HttpUtils;
import com.iso.hypo.tests.security.Users;

import net.datafaker.Faker;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper.Builder;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "app.test.run=true")
@TestInstance(Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class CoachControllerTests {

	public static final String listURI = "/v1/brands/%s/coachs";
	public static final String postURI = "/v1/brands/%s/coachs";
	public static final String getURI = "/v1/brands/%s/coachs/%s";
	public static final String putURI = "/v1/brands/%s/coachs/%s";
	public static final String postActivateURI = "/v1/brands/%s/coachs/%s/activate";
	public static final String postDeactivateURI = "/v1/brands/%s/coachs/%s/deactivate";
	public static final String patchURI = "/v1/brands/%s/coachs/%s";
	public static final String deleteURI = "/v1/brands/%s/coachs/%s";
	public static final String pageNumber = "page";
	public static final String pageSize = "pageSize";
	public static final String includeInactive = "includeInactive";
	
	public static final String brandCode_1 = "CoachBrand1";
	public static final String brandCode_2= "CoachBrand2";
	
	@LocalServerPort
	private int port;

	@Autowired
	BrandRepository brandRepository;
	@Autowired
	CoachRepository coachRepository;
	@Autowired
	Builder objectMapper;
	@Autowired
	ModelMapper modelMapper;
	
	private Faker faker = new Faker();

	private RestTestClient restClient;

	private Coach coach;
	private Coach coachDeleted;
	private Brand brand_1;
	private Brand brand_2;
	private List<Coach> coachs = new ArrayList<Coach>();

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
		
		coachRepository.deleteAll();

		brand_1 = BrandBuilder.build(brandCode_1, faker.company().name());
		brandRepository.save(brand_1);
		
		brand_2 = BrandBuilder.build(brandCode_2, faker.company().name());
		brandRepository.save(brand_2);
		
		coach = CoachBuilder.build(brand_1.getUuid());
		coach.setActive(true);
		coachRepository.save(coach);
		
		coachDeleted = CoachBuilder.build(brand_1.getUuid());
		coachDeleted.setDeleted(true);
		coachDeleted = coachRepository.save(coachDeleted);

		for (int i = 0; i < 10; i++) {
			Coach item = CoachBuilder.build(brand_1.getUuid());
			item.setActive(true);
			coachRepository.save(item);
			coachs.add(item);
		}
		
		for (int i = 0; i < 4; i++) {
			Coach item = CoachBuilder.build(brand_2.getUuid());
			item.setActive(true);
			coachRepository.save(item);
			coachs.add(item);
		}
		
		Coach item = CoachBuilder.build(brand_2.getUuid());
		item.setActive(false);
		coachRepository.save(item);
	}

	@AfterAll
	void cleanup() {
		// Cleanup
	//	coachRepository.deleteAll();
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
		PageResultDto<CoachDto> page = 
				this.restClient.get()
					.uri(HttpUtils.createURL(URI.create(String.format(listURI, brand_2.getUuid())), port, params))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(new ParameterizedTypeReference<PageResultDto<CoachDto>>() {}) 
					.returnResult()
				    .getResponseBody(); 
		
		// Assert
		Assertions.assertEquals(0, page.getPageNumber(),
				String.format("Coach list first page number invalid: %d", page.getPageNumber()));
		Assertions.assertEquals(4, page.getContent().size(),
				String.format("Coach list first page number of elements invalid: %d", page.getContent().size()));
		Assertions.assertEquals(4, page.getTotalElements(),
				String.format("Coach total number of elements invalid: %d", page.getTotalElements()));
		
		page.getContent().forEach(coach ->Assertions.assertTrue(coach.isActive()));
		page.getContent().forEach(coach ->Assertions.assertTrue(coach.isDeleted() == false));
	}
	
	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testListFirstPageSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "0");
		params.add(pageSize, "5");
		params.add(includeInactive, "true");
		
		// Act
		PageResultDto<CoachDto> page = 
				this.restClient.get()
					.uri(HttpUtils.createURL(URI.create(String.format(listURI, brand_2.getUuid())), port, params))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(new ParameterizedTypeReference<PageResultDto<CoachDto>>() {}) 
					.returnResult()
				    .getResponseBody(); 
		
		// Assert
		Assertions.assertEquals(0, page.getPageNumber(),
				String.format("Coach list first page number invalid: %d", page.getPageNumber()));
		Assertions.assertEquals(5, page.getContent().size(),
				String.format("Coach list first page number of elements invalid: %d", page.getContent().size()));
		Assertions.assertEquals(5, page.getTotalElements(),
				String.format("Coach total number of elements invalid: %d", page.getTotalElements()));
		
		page.getContent().forEach(coach ->Assertions.assertTrue(coach.isDeleted() == false));
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testListSecondPageSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "1");
		params.add(pageSize, "2");

		// Act
		PageResultDto<CoachDto> page = 
				this.restClient.get()
					.uri(HttpUtils.createURL(URI.create(String.format(listURI, brand_2.getUuid())), port, params))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(new ParameterizedTypeReference<PageResultDto<CoachDto>>() {}) 
					.returnResult()
				    .getResponseBody(); 

		// Assert
		Assertions.assertEquals(1, page.getPageNumber(),
				String.format("Coach list second page number invalid: %d", page.getPageNumber()));
		Assertions.assertEquals(2, page.getContent().size(),
				String.format("Coach list second page number of elements invalid: %d", page.getContent().size()));
		
		page.getContent().forEach(coach ->Assertions.assertTrue(coach.isActive()));
		page.getContent().forEach(coach ->Assertions.assertTrue(coach.isDeleted() == false));
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPostSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostCoachDto postDto = modelMapper.map(CoachBuilder.build(brand_1.getUuid()), PostCoachDto.class);
		
		CoachDto createdDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(postURI, brand_1.getUuid())), port, null))					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(postDto)
					.exchange() 
				    .expectStatus().isCreated() 
					.expectBody(CoachDto.class) 
					.returnResult()
				    .getResponseBody(); 
		
		// Assert
		assertCoach(modelMapper.map(postDto, CoachDto.class), createdDto);
	}
	
	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPostFailureForbiddenBrandMismatch(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostCoachDto postDto = modelMapper.map(CoachBuilder.build(brand_1.getUuid()), PostCoachDto.class);
		
		// Act
		CoachDto _ = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(postURI, faker.code().isbn10())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(postDto)
					.exchange() 
				    .expectStatus().isForbidden() 
					.expectBody(CoachDto.class) 
					.returnResult()
				    .getResponseBody(); 
	}
	
	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testGetSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostCoachDto postDto = modelMapper.map(CoachBuilder.build(brand_1.getUuid()), PostCoachDto.class);
		
		CoachDto createdDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(postURI, brand_1.getUuid())), port, null))					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(postDto)
					.exchange() 
				    .expectStatus().isCreated() 
					.expectBody(CoachDto.class) 
					.returnResult()
				    .getResponseBody();
	

		// Act
		CoachDto fetchedDto = 
				this.restClient.get()
					.uri(HttpUtils.createURL(URI.create(String.format(getURI, brand_1.getUuid(), createdDto.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(CoachDto.class) 
					.returnResult()
				    .getResponseBody();
		
		//Assert
		assertCoach(modelMapper.map(postDto, CoachDto.class), fetchedDto);
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
		Coach coachToUpdate = CoachBuilder.build(brand_1.getUuid());
		coachToUpdate = coachRepository.save(coachToUpdate);
		
		PutCoachDto putDto = modelMapper.map(coachToUpdate, PutCoachDto.class);

		// Mutate all mutable fields (keep uuid/code/dates)
		if (putDto.getPerson() != null) {
			putDto.getPerson().setEmail(faker.internet().emailAddress());
			putDto.getPerson().setFirstname(putDto.getPerson().getFirstname() + " - updated");
			putDto.getPerson().setLastname(putDto.getPerson().getLastname() + " - updated");
			if (putDto.getPerson().getAddress() != null) {
				putDto.getPerson().getAddress().setStreetName(faker.address().streetName());
			}
			if (putDto.getPerson().getPhoneNumbers() != null && putDto.getPerson().getPhoneNumbers().size() > 0) {
				// modify first phone number instead of removing
				putDto.getPerson().getPhoneNumbers().get(0).setNumber(faker.phoneNumber().cellPhone());
			}
			if (putDto.getPerson().getContacts() != null && putDto.getPerson().getContacts().size() > 0) {
				// modify a property on the first contact sub-object instead of removing
				putDto.getPerson().getContacts().get(0).setLastname("Updated" + faker.name().lastName());
			}
		}

		// Act
		CoachDto updatedDto = 
				this.restClient.put()
					.uri(HttpUtils.createURL(URI.create(String.format(putURI, brand_1.getUuid(), putDto.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(putDto)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(CoachDto.class) 
					.returnResult()
				    .getResponseBody();
		
		// Assert
		assertCoach(modelMapper.map(putDto, CoachDto.class), updatedDto);
	}
	
	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPutNullSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Coach coachToUpdate = CoachBuilder.build(brand_1.getUuid());
		coachToUpdate = coachRepository.save(coachToUpdate);
		
		PutCoachDto putDto = modelMapper.map(coachToUpdate, PutCoachDto.class);
		putDto.getPerson().setPhotoUri(null);
		
		// Act
		CoachDto updatedDto = 
				this.restClient.put()
					.uri(HttpUtils.createURL(URI.create(String.format(putURI, brand_1.getUuid(), putDto.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(putDto)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(CoachDto.class) 
					.returnResult()
				    .getResponseBody();
		
		// Assert
		coachToUpdate.getPerson().setPhotoUri(null);
		
		assertCoach(modelMapper.map(coachToUpdate, CoachDto.class), updatedDto);
	}
	
	@Test
	void testPutFailureNotFound() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		Coach coachToUpdate = CoachBuilder.build(brand_1.getUuid());
		PutCoachDto putDto = modelMapper.map(coachToUpdate, PutCoachDto.class);
		
		// Act
		ErrorDto _ = 
				this.restClient.put()
					.uri(HttpUtils.createURL(URI.create(String.format(putURI, brand_1.getUuid(), putDto.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
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
		PutCoachDto putDto = modelMapper.map(CoachBuilder.build(brand_1.getUuid()), PutCoachDto.class);
		
		// Act
		CoachDto _ = 
				this.restClient.put()
					.uri(HttpUtils.createURL(URI.create(String.format(putURI, faker.code().isbn10(), putDto.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(putDto)
					.exchange() 
				    .expectStatus().isForbidden() 
					.expectBody(CoachDto.class) 
					.returnResult()
				    .getResponseBody(); 
	}
	
	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPutFailureForbiddenUuidMismatch(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PutCoachDto putDto = modelMapper.map(CoachBuilder.build(brand_1.getUuid()), PutCoachDto.class);

		// Act
		CoachDto _ = 
				this.restClient.put()
					.uri(HttpUtils.createURL(URI.create(String.format(putURI, brand_1.getUuid(), faker.code().isbn10())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(putDto)
					.exchange() 
				    .expectStatus().isForbidden() 
					.expectBody(CoachDto.class) 
					.returnResult()
				    .getResponseBody(); 
	}
	
	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPatchSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Coach coachToPatch = CoachBuilder.build(brand_1.getUuid());
		coachToPatch.setActive(true);
		coachToPatch = coachRepository.save(coachToPatch);

		PatchCoachDto patchDto = modelMapper.map(coachToPatch, PatchCoachDto.class);
		patchDto.getPerson().setEmail(null);
		patchDto.getPerson().setFirstname(null);
		patchDto.getPerson().getAddress().setStreetName(null);
		patchDto.getPerson().setLastname(faker.name().lastName());
		
		// Act
		CoachDto patchedDto = 
				this.restClient.patch()
					.uri(HttpUtils.createURL(URI.create(String.format(patchURI, brand_1.getUuid(), patchDto.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(patchDto)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(CoachDto.class) 
					.returnResult()
				    .getResponseBody(); 
		
		// Assert
		coachToPatch.getPerson().setLastname(patchDto.getPerson().getLastname());
		
		assertCoach(modelMapper.map(coachToPatch, CoachDto.class), patchedDto);
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPatchFailureNotFound(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		Coach patchTarget = CoachBuilder.build(brand_1.getUuid());
		PatchCoachDto patchDto = modelMapper.map(patchTarget, PatchCoachDto.class);
		
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
		PatchCoachDto patchDto = modelMapper.map(CoachBuilder.build(brand_1.getUuid()), PatchCoachDto.class);
		
		// Act
		CoachDto _ = 
				this.restClient.patch()
					.uri(HttpUtils.createURL(URI.create(String.format(patchURI, faker.code().isbn10(), patchDto.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(patchDto)
					.exchange() 
				    .expectStatus().isForbidden() 
					.expectBody(CoachDto.class) 
					.returnResult()
				    .getResponseBody(); 
	}
	
	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPatchFailureForbiddenUuidMismatch(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PatchCoachDto patchDto = modelMapper.map(CoachBuilder.build(brand_1.getUuid()), PatchCoachDto.class);
		
		CoachDto _ = 
				this.restClient.patch()
					.uri(HttpUtils.createURL(URI.create(String.format(patchURI, brand_1.getUuid(), faker.code().isbn10())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(patchDto)
					.exchange() 
				    .expectStatus().isForbidden() 
					.expectBody(CoachDto.class) 
					.returnResult()
				    .getResponseBody(); 
	}
	
	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testActivateSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Coach coachToActivate = CoachBuilder.build(brand_1.getUuid());
		coachToActivate.setActive(false);
		coachToActivate.setActivatedOn(null);
		coachToActivate.setDeactivatedOn(null);
		coachToActivate = coachRepository.save(coachToActivate);
		coachToActivate.setActive(true);
		coachToActivate.setActivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		coachToActivate.setDeactivatedOn(null);
		
		// Act
		CoachDto activatedDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(postActivateURI, brand_1.getUuid(), coachToActivate.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(CoachDto.class) 
					.returnResult()
				    .getResponseBody(); 
		
		// Assert
		assertCoach(modelMapper.map(coachToActivate, CoachDto.class), activatedDto);
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
		Coach coachToDeactivate = CoachBuilder.build(brand_1.getUuid());
		coachToDeactivate.setActive(true);
		coachToDeactivate.setActivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		coachToDeactivate = coachRepository.save(coachToDeactivate);
		coachToDeactivate.setActive(false);
		coachToDeactivate.setDeactivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		
		// Act
		CoachDto deactivatedDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(postDeactivateURI, brand_1.getUuid(), coachToDeactivate.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(CoachDto.class) 
					.returnResult()
				    .getResponseBody(); 
		
		// Assert
		assertCoach(modelMapper.map(coachToDeactivate, CoachDto.class), deactivatedDto);
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
		Coach coachToDelete = CoachBuilder.build(brand_1.getUuid());
		coachToDelete = coachRepository.save(coachToDelete);

		// Act
		CoachDto _ = 
				this.restClient.delete()
					.uri(HttpUtils.createURL(URI.create(String.format(deleteURI, brand_1.getUuid(), coachToDelete.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isAccepted() 
					.expectBody(CoachDto.class) 
					.returnResult()
				    .getResponseBody(); 
		
		// Assert
		ErrorDto _ = 
				this.restClient.get()
					.uri(HttpUtils.createURL(URI.create(String.format(getURI, brand_1.getUuid(), coachToDelete.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isNotFound() 
					.expectBody(ErrorDto.class) 
					.returnResult()
				    .getResponseBody(); 
	}
	
	public static final void assertCoach(CoachDto expected, CoachDto result) {
		if (expected.getUuid() != null) {
			Assertions.assertEquals(expected.getUuid(), result.getUuid());
		}
		
		Assertions.assertEquals(expected.getBrandUuid(), result.getBrandUuid());
		Assertions.assertEquals(expected.getPerson().getFirstname(), result.getPerson().getFirstname());
		Assertions.assertEquals(expected.getPerson().getLastname(), result.getPerson().getLastname());
		Assertions.assertEquals(expected.getPerson().getEmail(), result.getPerson().getEmail());
		Assertions.assertEquals(expected.getPerson().getPhotoUri(), result.getPerson().getPhotoUri());
		Assertions.assertEquals(expected.getPerson().getCommunicationLanguage(), result.getPerson().getCommunicationLanguage());
		Assertions.assertEquals(expected.getPerson().getNote(), result.getPerson().getNote());
		
		LocalDate expecteDob = expected.getPerson().getDateOfBirth()
		        .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

		LocalDate resultDob = result.getPerson().getDateOfBirth()
		        .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		Assertions.assertEquals(expecteDob, resultDob);
		
		Assertions.assertEquals(expected.isActive(), result.isActive());
				
		if (expected.getActivatedOn() != null) {
			Assertions.assertNotNull(result.getActivatedOn());
			Assertions.assertTrue(expected.getActivatedOn().truncatedTo(ChronoUnit.DAYS).equals(result.getActivatedOn().truncatedTo(ChronoUnit.DAYS)));
		}
		
		if (expected.getDeactivatedOn() != null) {
			Assertions.assertNotNull(result.getDeactivatedOn());
			Assertions.assertTrue(expected.getDeactivatedOn().truncatedTo(ChronoUnit.DAYS).equals(result.getDeactivatedOn().truncatedTo(ChronoUnit.DAYS)));
		}
		
		if (expected.getPerson().getAddress() != null) {
			Assertions.assertEquals(expected.getPerson().getAddress().getCivicNumber(), result.getPerson().getAddress().getCivicNumber());
			Assertions.assertEquals(expected.getPerson().getAddress().getStreetName(), result.getPerson().getAddress().getStreetName());
			Assertions.assertEquals(expected.getPerson().getAddress().getAppartment(), result.getPerson().getAddress().getAppartment());
			Assertions.assertEquals(expected.getPerson().getAddress().getCity(), result.getPerson().getAddress().getCity());
			Assertions.assertEquals(expected.getPerson().getAddress().getState(), result.getPerson().getAddress().getState());
			Assertions.assertEquals(expected.getPerson().getAddress().getZipCode(), result.getPerson().getAddress().getZipCode());
		}
		
		if (expected.getPerson().getAddress() == null) {
			Assertions.assertNull(result.getPerson().getAddress());
		}
		
		if (expected.getPerson().getPhoneNumbers() != null) {
			Assertions.assertNotNull(result.getPerson().getPhoneNumbers());

			Assertions.assertEquals(expected.getPerson().getPhoneNumbers().size(), result.getPerson().getPhoneNumbers().size());
			expected.getPerson().getPhoneNumbers().forEach(phone -> {
				Optional<PhoneNumberDto> previous = result.getPerson().getPhoneNumbers().stream()
						.filter(item -> item.getType().equals(phone.getType())).findFirst();
				Assertions.assertTrue(previous.isPresent());
			});
		}

		if (expected.getPerson().getPhoneNumbers() == null) {
			Assertions.assertNull(result.getPerson().getPhoneNumbers());
		}
			
		if (expected.getPerson().getContacts() != null) {
			Assertions.assertNotNull(result.getPerson().getContacts());

			Assertions.assertEquals(expected.getPerson().getContacts().size(), result.getPerson().getContacts().size());
			expected.getPerson().getContacts().forEach(contact -> {
				Optional<ContactDto> previous = result.getPerson().getContacts().stream()
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
					Assertions.assertNull(contact.getPhoneNumbers());
				}
			});
		}

		if (expected.getPerson().getContacts() == null) {
			Assertions.assertNull(result.getPerson().getContacts());
		}			
		
	}
}
