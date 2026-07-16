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
import com.iso.hypo.admin.papi.dto.model.BrandDto;
import com.iso.hypo.admin.papi.dto.model.CoachDto;
import com.iso.hypo.admin.papi.dto.model.GymDto;
import com.iso.hypo.admin.papi.dto.patch.PatchGymDto;
import com.iso.hypo.admin.papi.dto.post.PostBrandDto;
import com.iso.hypo.admin.papi.dto.post.PostGymDto;
import com.iso.hypo.admin.papi.dto.put.PutGymDto;
import com.iso.hypo.admin.papi.dto.search.GymSearchDto;
import com.iso.hypo.brand.application.exception.GymException;
import com.iso.hypo.brand.application.mapper.BrandDtoMapper;
import com.iso.hypo.brand.application.usecase.BrandService;
import com.iso.hypo.brand.domain.model.Brand;
import com.iso.hypo.brand.domain.model.Coach;
import com.iso.hypo.brand.domain.model.Gym;
import com.iso.hypo.brand.domain.repository.BrandRepository;
import com.iso.hypo.brand.domain.repository.CoachRepository;
import com.iso.hypo.brand.domain.repository.CourseRepository;
import com.iso.hypo.brand.domain.repository.GymRepository;
import com.iso.hypo.common.application.dto.PageResultDto;
import com.iso.hypo.common.application.security.Roles;
import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;
import com.iso.hypo.domain.BrandBuilder;
import com.iso.hypo.domain.CoachBuilder;
import com.iso.hypo.domain.GymBuilder;
import com.iso.hypo.membership.domain.repository.MemberRepository;
import com.iso.hypo.membership.domain.repository.MembershipPlanRepository;
import com.iso.hypo.tests.data.Populator;
import com.iso.hypo.tests.http.HttpUtils;
import com.iso.hypo.tests.security.Users;
import com.iso.hypo.tests.utils.StringUtils;

import net.datafaker.Faker;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper.Builder;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "app.test.run=true")
@TestInstance(Lifecycle.PER_CLASS)
@ActiveProfiles("test")
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
	public static final String postAssignCoachURI = "/v1/brands/%s/gyms/%s/coachs/%s/assign";
	public static final String postUnassignCoachURI = "/v1/brands/%s/gyms/%s/coachs/%s/unassign";
	public static final String searchCriteria = "criteria";
	public static final String pageNumber = "page";
	public static final String pageSize = "pageSize";

	public static final String deleteCoachURI = "/v1/brands/%s/coachs/%s";
	
	public static final String postBrandURI = "/v1/brands";
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
	MemberRepository memberRepository;
	@Autowired
	BrandService brandService;
	@Autowired
	BrandDtoMapper brandMapper;
	@Autowired
	Builder objectMapper;

	@Autowired
	ModelMapper modelMapper;

	private Faker faker = new Faker();
	
	private RestTestClient restClient;
	
	private Gym gym;
	private Gym gymDeleted;
	private Brand brand;
	private List<Gym> gyms = new ArrayList<>();
	private List<Coach> coachs = new ArrayList<>();

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
		
		gymRepository.deleteAll();

		brand = BrandBuilder.build(brandCode, faker.company().name());
		brandRepository.save(brand);
		
		for (int i = 0; i < 5; i++) {
			Coach item = CoachBuilder.build(brand.getUuid());
			item = coachRepository.save(item);
			coachs.add(item);
		}
		
		gym = GymBuilder.build(brand.getUuid(), gymCode_1, faker.address().cityName(),coachs.subList(0, 2));
		gymRepository.save(gym);
		
		gym = GymBuilder.build(brand.getUuid(), gymCode_2, faker.address().cityName(),coachs.subList(2, 2));
		gymRepository.save(gym);
		
		gymDeleted = GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.code().isbn10(), null);
		gymDeleted.setDeleted(true);
		gymDeleted = gymRepository.save(gymDeleted);

		for (int i = 0; i < 10; i++) {
			Gym item = GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.company().name(), null);
			
			gymRepository.save(item);
			gyms.add(item);
		}
		
		for (int i = 0; i < 5; i++) {
			Gym item = GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.company().name(), null);
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
	void testSearchAutocompleteDeletedSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(gymDeleted.getName(), 10);
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
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "0");
		params.add(pageSize, "4");

		// Act
		PageResultDto<GymDto> page = 
				this.restClient.get()
					.uri(HttpUtils.createURL(URI.create(String.format(listURI, brand.getUuid())), port, params))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(new ParameterizedTypeReference<PageResultDto<GymDto>>() {}) 
					.returnResult()
				    .getResponseBody(); 
		
		// Assert
		Assertions.assertEquals(0, page.getPageNumber(),
				String.format("Gym list first page number invalid: %d", page.getPageNumber()));
		Assertions.assertEquals(4, page.getContent().size(),
				String.format("Gym list first page number of elements invalid: %d", page.getContent().size()));
	}

	@Test
	void testListSecondPageSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "1");
		params.add(pageSize, "4");
		
		// Act
		PageResultDto<GymDto> page = 
				this.restClient.get()
					.uri(HttpUtils.createURL(URI.create(String.format(listURI, brand.getUuid())), port, params))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(new ParameterizedTypeReference<PageResultDto<GymDto>>() {}) 
					.returnResult()
				    .getResponseBody(); 

		// Assert
		Assertions.assertEquals(1, page.getPageNumber(),
				String.format("Gym list second page number invalid: %d", page.getPageNumber()));
		Assertions.assertEquals(4, page.getContent().size(),
				String.format("Gym list second page number of elements invalid: %d", page.getContent().size()));
	}

	@Test
	void testPostSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostGymDto postDto = modelMapper.map(GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.company().name(), coachs), PostGymDto.class);
		
		// Act
		GymDto createdDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(postURI, brand.getUuid())), port, null))					
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(postDto)
					.exchange() 
				    .expectStatus().isCreated() 
					.expectBody(GymDto.class) 
					.returnResult()
				    .getResponseBody();
		
		// Assert
		assertGym(modelMapper.map(postDto, GymDto.class), createdDto);
	}
	
	@Test
	void testPostDuplicateFailure() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostGymDto postDto = modelMapper.map(GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.company().name(), null), PostGymDto.class);
		
		GymDto _ = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(postURI, brand.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(postDto)
					.exchange() 
				    .expectStatus().isCreated() 
					.expectBody(GymDto.class) 
					.returnResult()
				    .getResponseBody(); 
		
		// Act
		GymDto dupDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(postURI, brand.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(postDto)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(GymDto.class) 
					.returnResult()
				    .getResponseBody(); 
		
		// Assert
		Assertions.assertEquals(1, dupDto.getMessages().size(),
				String.format("Duplicate error ,missing message: %s", dupDto.getMessages().size()));
		
		Assertions.assertEquals(GymException.GYM_CODE_ALREADY_EXIST, dupDto.getMessages().getFirst().getCode(),
				String.format("Duplicate error, missing message: %s", dupDto.getMessages().getFirst().getCode()));
	}
	
	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPostFailureForbiddenBrandMismatch(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostGymDto postDto = modelMapper.map(GymBuilder.build(brand.getUuid(), faker.code().isbn10(), faker.company().name(), null), PostGymDto.class);
		
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
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis", "member, Guillaume Fortin", })
	void testGetSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostGymDto postDto = modelMapper.map(GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.company().name(), coachs), PostGymDto.class);
		
		GymDto createdDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(postURI, brand.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(postDto)
					.exchange() 
				    .expectStatus().isCreated() 
					.expectBody(GymDto.class) 
					.returnResult()
				    .getResponseBody();
		
		// Act
		GymDto fetchedDto = 
				this.restClient.get()
					.uri(HttpUtils.createURL(URI.create(String.format(getURI, createdDto.getBrandUuid(), createdDto.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(GymDto.class) 
					.returnResult()
				    .getResponseBody();
		
		// Assert
		assertGym(modelMapper.map(postDto, GymDto.class), fetchedDto);
	}
	
	@Test
	void testGetFailureNotFound() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		ErrorDto _ = 
				this.restClient.get()
					.uri(HttpUtils.createURL(URI.create(String.format(getURI, brand.getUuid(), faker.code().isbn10())), port, null))
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
		Gym updatedGym = GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.company().name(), coachs.subList(0, 2));
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
		if (putDto.getCoachs() != null && putDto.getCoachs().size() > 0) {
			putDto.getCoachs().remove(0);
		}

		// Act
		GymDto updatedDto = 
				this.restClient.put()
					.uri(HttpUtils.createURL(URI.create(String.format(putURI, updatedGym.getBrandUuid(), updatedGym.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(putDto)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(GymDto.class) 
					.returnResult()
				    .getResponseBody();
		
		// Assert
		assertGym(modelMapper.map(putDto, GymDto.class), updatedDto);
	}
	
	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPutNullSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Gym updatedGym = GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.company().name(), null);
		updatedGym.setActive(true);
		updatedGym = gymRepository.save(updatedGym);
		
		PutGymDto putDto = modelMapper.map(updatedGym, PutGymDto.class);
		
		putDto.setEmail(null);
		putDto.setAddress(null);
		putDto.setPhoneNumbers(null);
		putDto.setContacts(null);
		
		// Act
		GymDto updatedDto = 
				this.restClient.put()
					.uri(HttpUtils.createURL(URI.create(String.format(putURI, updatedGym.getBrandUuid(), updatedGym.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(putDto)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(GymDto.class) 
					.returnResult()
				    .getResponseBody();
		
		// Assert
	 	assertGym(modelMapper.map(putDto, GymDto.class), updatedDto);
	}
	
	@Test
	void testPutFailureNotFound() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PutGymDto putDto = modelMapper.map(GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.company().name(), null), PutGymDto.class);
		
		// Act
		ErrorDto _ = 
				this.restClient.put()
					.uri(HttpUtils.createURL(URI.create(String.format(putURI, brand.getUuid(), putDto.getUuid())), port, null))
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
		PutGymDto putDto = modelMapper.map(GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.company().name(), null), PutGymDto.class);
		
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
		PutGymDto putDto = modelMapper.map(GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.company().name(), null), PutGymDto.class);
		
		// Act
		ErrorDto _ = 
				this.restClient.put()
					.uri(HttpUtils.createURL(URI.create(String.format(putURI, brand.getUuid(), faker.code().isbn10())), port, null))
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
		Gym gymToPatch = GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.company().name(), null);
		gymToPatch.setActive(true);
		gymToPatch = gymRepository.save(gymToPatch);
		
		PatchGymDto patchDto = modelMapper.map(gymToPatch, PatchGymDto.class);
		patchDto.getAddress().setStreetName(faker.address().streetName());
		patchDto.setEmail(faker.internet().emailAddress());
		
		// Act
		GymDto patchedDto = 
				this.restClient.patch()
					.uri(HttpUtils.createURL(URI.create(String.format(patchURI, gymToPatch.getBrandUuid(), gymToPatch.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(patchDto)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(GymDto.class) 
					.returnResult()
				    .getResponseBody(); 
		
		// Assert
	  	assertGym(modelMapper.map(patchDto, GymDto.class), patchedDto);
	}
	
	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPatchFailureNotFound(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		Gym patchTarget = GymBuilder.build(brand.getUuid(), faker.code().isbn10(), faker.company().name(), null);
		PatchGymDto patchDto = modelMapper.map(patchTarget, PatchGymDto.class);
		
		// Act
		ErrorDto _ = 
				this.restClient.patch()
					.uri(HttpUtils.createURL(URI.create(String.format(patchURI, patchTarget.getBrandUuid(), patchTarget.getUuid())), port, null))
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
		PatchGymDto patchDto = modelMapper.map(GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.company().name(), null), PatchGymDto.class);
		
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
		PatchGymDto patchDto = modelMapper.map(GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.company().name(), null), PatchGymDto.class);
		
		// Act
		ErrorDto _ = 
				this.restClient.patch()
					.uri(HttpUtils.createURL(URI.create(String.format(patchURI, brand.getUuid(), faker.code().isbn10())), port, null))
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
		Gym gymToActivate = GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.company().name(), null);
		gymToActivate.setActive(false);
		gymToActivate.setActivatedOn(null);
		gymToActivate.setDeactivatedOn(null);
		gymToActivate = gymRepository.save(gymToActivate);

		gymToActivate.setActive(true);
		gymToActivate.setActivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		gymToActivate.setDeactivatedOn(null);

		// Act
		GymDto activatedDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(postActivateURI, gymToActivate.getBrandUuid(), gymToActivate.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(GymDto.class) 
					.returnResult()
				    .getResponseBody(); 
		
		// Assert
		assertGym(modelMapper.map(gymToActivate, GymDto.class), activatedDto);
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testActivateFailureNotFound(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Act
		ErrorDto _ = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(postActivateURI, brand.getUuid(), faker.code().isbn10())), port, null))
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
		Gym gymToDeactivate = GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.company().name(), null);
		gymToDeactivate.setActive(true);
		gymToDeactivate.setActivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		gymToDeactivate = gymRepository.save(gymToDeactivate);

		gymToDeactivate.setActive(false);
		gymToDeactivate.setDeactivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));

		// Act
		GymDto deactivatedDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(postDeactivateURI, brand.getUuid(), gymToDeactivate.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(GymDto.class) 
					.returnResult()
				    .getResponseBody();
		
		// Assert
		assertGym(modelMapper.map(gymToDeactivate, GymDto.class), deactivatedDto);
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testDeactivateFailureNotFound(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Act
		ErrorDto _ = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(postDeactivateURI, brand.getUuid(), faker.code().isbn10())), port, null))
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
		PostBrandDto postBrandDto = modelMapper.map(BrandBuilder.build(faker.code().isbn10(),faker.company().name()), PostBrandDto.class);
		
		BrandDto createdBrandDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(postBrandURI), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(postBrandDto)
					.exchange() 
				    .expectStatus().isCreated() 
					.expectBody(BrandDto.class) 
					.returnResult()
				    .getResponseBody();  

		Populator populator = new Populator(gymRepository, coachRepository, courseRepository, membershipPlanRepository, memberRepository, modelMapper, restClient, port);
		populator.populateFullBrand(createdBrandDto, null);
		
		Gym gymToDelete = gymRepository.findByBrandUuidAndCode(createdBrandDto.getUuid(), "boucherville").get();

		// Act
		GymDto _ = 
				this.restClient.delete()
					.uri(HttpUtils.createURL(URI.create(String.format(deleteURI, createdBrandDto.getUuid(), gymToDelete.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isAccepted() 
					.expectBody(GymDto.class) 
					.returnResult()
				    .getResponseBody(); 


		// Assert
		ErrorDto _ = 
				this.restClient.get()
					.uri(HttpUtils.createURL(URI.create(String.format(getURI, createdBrandDto.getUuid(), gymToDelete.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isNotFound() 
					.expectBody(ErrorDto.class) 
					.returnResult()
				    .getResponseBody(); 
	}
	
	@Test
	void testDeleteReferencesSuccess() throws JsonProcessingException, MalformedURLException {
		//DELETE REFERENCES SUCCESS: when a coach is deleted, it should be removed from all gym plans that reference it. 
		
		// Arrange
		List<Coach> coachs = new ArrayList<>();
		
		for (int i = 0; i < 2; i++) {
			Coach item = CoachBuilder.build(brand.getUuid());
			item = coachRepository.save(item);
			coachs.add(item);
		}
		
		for (int i = 0; i < 2; i++) {
			Gym item = GymBuilder.build(brand.getUuid(), faker.code().isbn10(), faker.address().cityName(), null);
			item = gymRepository.save(item);
			gyms.add(item);
		}
		
		Gym gymReferences1 = GymBuilder.build(brand.getUuid(), faker.code().isbn10(), faker.address().cityName(), coachs);
		gymRepository.save(gymReferences1);
		
		Gym gymReferences2 = GymBuilder.build(brand.getUuid(), faker.code().isbn10(), faker.address().cityName(), coachs);
		gymRepository.save(gymReferences2);
		
		// Act
		CoachDto _ = 
				this.restClient.delete()
					.uri(HttpUtils.createURL(URI.create(String.format(deleteCoachURI, brand.getUuid(), coachs.getFirst().getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isAccepted() 
					.expectBody(CoachDto.class) 
					.returnResult()
				    .getResponseBody(); 
		
		// Assert
		PageResult<Gym> pageGym = gymRepository.findAllByBrandUuidAndDeletedIsFalse(brand.getUuid(),  PageRequest.of(0, 1000));
		
		pageGym.getContent().forEach(gym -> {
			Assertions.assertFalse(gym.getCoachs().stream().filter(coach -> coach.getUuid().equals(coachs.getFirst().getUuid())).findFirst().isPresent(),
					String.format("Deleted coach %s still present in gym %s", coachs.getFirst().getUuid(), gym.getUuid()));
		});
		
		gymReferences1 = gymRepository.findByBrandUuidAndUuidAndDeletedIsFalse(brand.getUuid(),  gymReferences1.getUuid()).get();
		Assertions.assertTrue(gymReferences1.getCoachs().size() == 1 && gymReferences1.getCoachs().get(0).getUuid().equals(coachs.getLast().getUuid()),
				String.format("Non deleted coach %s not found in gym %s", coachs.getLast().getUuid(), gym.getUuid()));
	
		gymReferences2 = gymRepository.findByBrandUuidAndUuidAndDeletedIsFalse(brand.getUuid(),  gymReferences2.getUuid()).get();
		Assertions.assertTrue(gymReferences2.getCoachs().size() == 1 && gymReferences2.getCoachs().get(0).getUuid().equals(coachs.getLast().getUuid()),
				String.format("Non deleted coach %s not found in gym %s", coachs.getLast().getUuid(), gym.getUuid()));
	}
	
	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testAssignCoachSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Gym updatedGym = GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.company().name(), coachs.subList(0, 2));
		updatedGym.setActive(true);
		updatedGym = gymRepository.save(updatedGym);
		
		// Act
		GymDto gymAssignedDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(postAssignCoachURI, updatedGym.getBrandUuid(), updatedGym.getUuid(), coachs.getLast().getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(GymDto.class) 
					.returnResult()
				    .getResponseBody();  
		
		// Assert
		Assertions.assertTrue(gymAssignedDto.getCoachs().stream().filter(coach -> coach.getUuid().equals(coachs.getLast().getUuid())).findFirst().isPresent(),
				String.format("Assigned coach %s not found in gym %s", coachs.getLast().getUuid(), gym.getUuid()));

	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testAssignCoachFailureNotFound(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Gym updatedGym = GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.company().name(), coachs.subList(0, 2));
		updatedGym.setActive(true);
		updatedGym = gymRepository.save(updatedGym);
		
		ErrorDto _ = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(postAssignCoachURI, updatedGym.getBrandUuid(), updatedGym.getUuid(), "non-existing-coach")), port, null))
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
	void testAssignCoachAlreadyAssigned(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Gym updatedGym = GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.company().name(), coachs.subList(0, 2));
		updatedGym.setActive(true);
		updatedGym = gymRepository.save(updatedGym);
		
		// Act
		GymDto gymAssignedDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(postAssignCoachURI, updatedGym.getBrandUuid(), updatedGym.getUuid(), coachs.getFirst().getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(GymDto.class) 
					.returnResult()
				    .getResponseBody();  
	
		// Assert
		Assertions.assertEquals(GymException.COACH_ALREADY_ASSIGNED, gymAssignedDto.getMessages().getFirst().getCode(),
				String.format("Coach already assigned error, missing message: %s", gymAssignedDto.getMessages().getFirst().getCode()));
	}
	
	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testUnassignCoachSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Gym updatedGym = GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.company().name(), coachs.subList(0, 2));
		updatedGym.setActive(true);
		updatedGym = gymRepository.save(updatedGym);
		
		// Act
		GymDto gymUnassignedDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(postUnassignCoachURI, updatedGym.getBrandUuid(), updatedGym.getUuid(), coachs.getFirst().getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(GymDto.class) 
					.returnResult()
				    .getResponseBody();  
		
		// Assert
		Assertions.assertTrue(gymUnassignedDto.getCoachs().stream().filter(coach -> coach.getUuid().equals(coachs.getLast().getUuid())).findFirst().isEmpty(),
				String.format("Unassigned coach %s found in gym %s", coachs.getFirst().getUuid(), gym.getUuid()));

	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testUnassignCoachFailureNotFound(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Gym updatedGym = GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.company().name(), coachs.subList(0, 2));
		updatedGym.setActive(true);
		updatedGym = gymRepository.save(updatedGym);
		
		// Act
		ErrorDto _ = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(postUnassignCoachURI, updatedGym.getBrandUuid(), updatedGym.getUuid(), "non-existing-coach")), port, null))
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
	void testUnassignCoachAlreadyUnassigned(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Gym updatedGym = GymBuilder.build(brand.getUuid(), faker.code().isbn10(),faker.company().name(), coachs.subList(0, 2));
		updatedGym.setActive(true);
		updatedGym = gymRepository.save(updatedGym);
		
		// Act
		GymDto gymUnassignedDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(postUnassignCoachURI, updatedGym.getBrandUuid(), updatedGym.getUuid(), coachs.getLast().getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
				//	.body(putDto)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(GymDto.class) 
					.returnResult()
				    .getResponseBody();  
	
		// Assert
		Assertions.assertEquals(GymException.COACH_NOT_ASSIGNED, gymUnassignedDto.getMessages().getFirst().getCode(),
				String.format("Coach already unassigned error, missing message: %s", gymUnassignedDto.getMessages().getFirst().getCode()));
	}
	
	private void assertSearch(String criteria, int minimumNumberOfElements, int maximumNumberOfElements) throws JsonProcessingException, MalformedURLException {
		// Arrange
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(searchCriteria, criteria);
		params.add(pageNumber, "0");
		params.add(pageSize, "4");
		
		// Act
		await()
        .atMost(20, TimeUnit.SECONDS)
        .pollInterval(200, TimeUnit.MILLISECONDS)
        .untilAsserted(() -> {
        	PageResultDto<GymSearchDto> page = 
    				this.restClient.get()
    					.uri(HttpUtils.createURL(URI.create(String.format(searchURI, brand.getUuid()) ), port, params))
    					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
    					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
    					.exchange() 
    				    .expectStatus().isOk() 
    					.expectBody(new ParameterizedTypeReference<PageResultDto<GymSearchDto>>() {}) 
    					.returnResult()
    				    .getResponseBody(); 
        	
        		// Assert
				Assertions.assertTrue(page.getTotalElements() >= minimumNumberOfElements &&
						page.getTotalElements() <= maximumNumberOfElements,
						String.format("Gym search return invalid number of results [%s]: %d",
							criteria, page.getTotalElements()));
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
						.filter(item -> item.getFirstname().equals(contact.getFirstname()) && item.getLastname().equals(contact.getLastname())).findFirst();
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
		
		if (expected.getCoachs() != null) {
			for (CoachDto coach : expected.getCoachs()) {
				Assertions.assertTrue(result.getCoachs().stream().filter(g -> g.getUuid().equals(coach.getUuid())).findFirst().isPresent(),
						String.format("Coach %s is missing in result", coach.getUuid()));
			}
		}
	}
	
}
