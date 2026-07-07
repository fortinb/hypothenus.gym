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
import com.iso.hypo.admin.papi.dto.contact.ContactDto;
import com.iso.hypo.admin.papi.dto.contact.PhoneNumberDto;
import com.iso.hypo.admin.papi.dto.model.BrandDto;
import com.iso.hypo.admin.papi.dto.patch.PatchBrandDto;
import com.iso.hypo.admin.papi.dto.post.PostBrandDto;
import com.iso.hypo.admin.papi.dto.put.PutBrandDto;
import com.iso.hypo.admin.papi.dto.search.BrandSearchDto;
import com.iso.hypo.brand.application.exception.BrandException;
import com.iso.hypo.brand.application.mapper.BrandDtoMapper;
import com.iso.hypo.brand.application.usecase.BrandService;
import com.iso.hypo.brand.domain.model.Brand;
import com.iso.hypo.brand.domain.model.Coach;
import com.iso.hypo.brand.domain.model.Course;
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
import com.iso.hypo.membership.domain.model.MembershipPlan;
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
class BrandControllerTests {

	public static final String searchURI = "/v1/brands/search";
	public static final String listURI = "/v1/brands";
	public static final String postURI = "/v1/brands";
	public static final String getURI = "/v1/brands/%s";
	public static final String putURI = "/v1/brands/%s";
	public static final String patchURI = "/v1/brands/%s";
	public static final String deleteURI = "/v1/brands/%s";
	public static final String postActivateURI = "/v1/brands/%s/activate";
	public static final String postDeactivateURI = "/v1/brands/%s/deactivate";
	public static final String searchCriteria = "criteria";
	public static final String pageNumber = "page";
	public static final String pageSize = "pageSize";

	public static final String codeBrand_1 = "Brand1";
	
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
	
	private Brand brand;
	private Brand brandDeleted;
	private List<Brand> brands = new ArrayList<Brand>();

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

		brandRepository.deleteAll();

		brand = BrandBuilder.build(codeBrand_1, faker.company().name());
		brandRepository.save(brand);
		
		brandDeleted = BrandBuilder.build(faker.code().isbn10(), faker.code().isbn10());
		brandDeleted.setDeleted(true);
		brandDeleted = brandRepository.save(brandDeleted);

		for (int i = 0; i < 10; i++) {
			Brand active_brand = BrandBuilder.build(faker.code().isbn10(), faker.company().name());
			
			brandRepository.save(active_brand);
			brands.add(active_brand);
		}
		
		for (int i = 0; i < 5; i++) {
			Brand inactive_brand = BrandBuilder.build(faker.code().isbn10(),faker.company().name());
			inactive_brand.setActive(false);
			brandRepository.save(inactive_brand);
			brands.add(inactive_brand);
		}
	}

	@AfterAll
	void cleanup() {
		// Cleanup
	//	brandRepository.deleteAll();
	}

	@Test
	void testSearchAutocompleteDeletedSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(brandDeleted.getName(), 10);
		assertSearch(criteria,0,0);
	}
	
	@Test
	void testSearchAutocompleteCitySuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(brand.getAddress().getCity(), 3);
		assertSearch(criteria,1,1000);
	}

	@Test
	void testSearchAutocompleteStateSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(brand.getAddress().getState(), 2);
		assertSearch(criteria,1,1000);
	}

	@Test
	void testSearchAutocompleteStreetNameSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(brand.getAddress().getStreetName(), 3);
		assertSearch(criteria,1,1000);
	}

	@Test
	void testSearchAutocompleteZipCodeSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(brand.getAddress().getZipCode(), 3);
		assertSearch(criteria,1,1000);
	}

	@Test
	void testSearchAutocompleteNameSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(brand.getName(), 3);
		assertSearch(criteria,1,1000);
	}

	@Test
	void testSearchAutocompleteEmailSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(brand.getEmail(), 3);
		assertSearch(criteria,1,1000);
	}

	@Test
	void testListFirstPageSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "0");
		params.add(pageSize, "4");
		
		// Act
		PageResultDto<BrandDto> page = 
				this.restClient.get()
					.uri(HttpUtils.createURL(URI.create(listURI), port, params))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(new ParameterizedTypeReference<PageResultDto<BrandDto>>() {}) 
					.returnResult()
				    .getResponseBody();  
	
		// Assert
		Assertions.assertEquals(0, page.getPageNumber(),
				String.format("Brand list first page number invalid: %d", page.getPageNumber()));
		Assertions.assertEquals(4, page.getContent().size(),
				String.format("Brand list first page number of elements invalid: %d", page.getContent().size()));
	}

	@Test
	void testListSecondPageSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "1");
		params.add(pageSize, "4");

		// Act
		PageResultDto<BrandDto> page = 
				this.restClient.get()
					.uri(HttpUtils.createURL(URI.create(listURI), port, params))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(new ParameterizedTypeReference<PageResultDto<BrandDto>>() {}) 
					.returnResult()
				    .getResponseBody();  
		
		// Assert
		Assertions.assertEquals(1, page.getPageNumber(),
				String.format("Brand list second page number invalid: %d", page.getPageNumber()));
		Assertions.assertEquals(4, page.getContent().size(),
				String.format("Brand list second page number of elements invalid: %d", page.getContent().size()));
	}

	@Test
	void testPostSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostBrandDto postDto = modelMapper.map(BrandBuilder.build(faker.code().isbn10(),faker.company().name()), PostBrandDto.class);

		// Act
		BrandDto createdDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(postURI), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(postDto)
					.exchange() 
				    .expectStatus().isCreated() 
					.expectBody(BrandDto.class) 
					.returnResult()
				    .getResponseBody();  

		// Assert
		assertBrand(modelMapper.map(postDto, BrandDto.class), createdDto);
	}
	
	@Test
	void testPostDuplicateFailure() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostBrandDto postDto = modelMapper.map(BrandBuilder.build(faker.code().isbn10(),faker.company().name()), PostBrandDto.class);
		
		BrandDto _ = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(postURI), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(postDto)
					.exchange() 
				    .expectStatus().isCreated() 
					.expectBody(BrandDto.class) 
					.returnResult()
				    .getResponseBody();
		
		// Act
		BrandDto dupDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(postURI), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(postDto)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(BrandDto.class) 
					.returnResult()
				    .getResponseBody();
		
		// Assert
		Assertions.assertEquals(1, dupDto.getMessages().size(),
				String.format("Duplicate error ,missing message: %s", dupDto.getMessages().size()));
		
		Assertions.assertEquals(BrandException.BRAND_CODE_ALREADY_EXIST, dupDto.getMessages().getFirst().getCode(),
				String.format("Duplicate error, missing message: %s", dupDto.getMessages().getFirst().getCode()));
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis", "member, Guillaume Fortin", })
	void testGetSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostBrandDto postDto = modelMapper.map(BrandBuilder.build(faker.code().isbn10(),faker.company().name()), PostBrandDto.class);
		
		BrandDto createdDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(postURI), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(postDto)
					.exchange() 
				    .expectStatus().isCreated() 
					.expectBody(BrandDto.class) 
					.returnResult()
				    .getResponseBody();  

		// Act
		BrandDto fetchedDto = 
				this.restClient.get()
					.uri(HttpUtils.createURL(URI.create(String.format(getURI, createdDto.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(BrandDto.class) 
					.returnResult()
				    .getResponseBody();
		
		// Assert
		assertBrand(modelMapper.map(postDto, BrandDto.class), fetchedDto);
	}
	
	@Test
	void testGetFailureNotFound() throws MalformedURLException, JsonProcessingException, Exception {
		
		// Act
		BrandDto _ = 
				this.restClient.get()
					.uri(HttpUtils.createURL(URI.create(String.format(getURI, faker.code().isbn10())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isNotFound() 
					.expectBody(BrandDto.class) 
					.returnResult()
				    .getResponseBody();  
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPutSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Brand updatedBrand = BrandBuilder.build(faker.code().isbn10(),faker.company().name());
		updatedBrand.setActive(true);
		updatedBrand = brandRepository.save(updatedBrand);

		PutBrandDto putDto = modelMapper.map(updatedBrand, PutBrandDto.class);

		putDto.setEmail(faker.internet().emailAddress());

		if (putDto.getName() != null && !putDto.getName().isEmpty()) {
			putDto.setName(putDto.getName() + " - updated");
		}

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
		BrandDto updatedDto = 
				this.restClient.put()
					.uri(HttpUtils.createURL(URI.create(String.format(putURI, updatedBrand.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(putDto)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(BrandDto.class) 
					.returnResult()
				    .getResponseBody(); 

		// Assert
		assertBrand(modelMapper.map(putDto, BrandDto.class), updatedDto);
	}
	
	@Test
	void testPutNullSuccess() throws JsonProcessingException, MalformedURLException {
		// Arrange
		Brand updatedBrand = BrandBuilder.build(faker.code().isbn10(),faker.company().name());
		updatedBrand.setActive(true);
		updatedBrand = brandRepository.save(updatedBrand);
		
		PutBrandDto putDto = modelMapper.map(updatedBrand, PutBrandDto.class);
		
		putDto.setEmail(null);
		putDto.setAddress(null);
		putDto.setPhoneNumbers(null);
		putDto.setContacts(null);
		
		// Act
		BrandDto updatedDto = 
				this.restClient.put()
					.uri(HttpUtils.createURL(URI.create(String.format(putURI, updatedBrand.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(putDto)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(BrandDto.class) 
					.returnResult()
				    .getResponseBody(); 

		// Assert
	 	assertBrand(modelMapper.map(putDto, BrandDto.class), updatedDto);
	}
	
	@Test
	void testPutFailureNotFound() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		Brand updatedBrand = BrandBuilder.build(faker.code().isbn10(),faker.company().name());
		PutBrandDto putDto = modelMapper.map(updatedBrand, PutBrandDto.class);
		
		// Act
		BrandDto _ = 
				this.restClient.put()
					.uri(HttpUtils.createURL(URI.create(String.format(putURI, updatedBrand.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(putDto)
					.exchange() 
				    .expectStatus().isNotFound() 
					.expectBody(BrandDto.class) 
					.returnResult()
				    .getResponseBody(); 
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPatchSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Brand brandToPatch = BrandBuilder.build(faker.code().isbn10(),faker.company().name());
		brandToPatch.setActive(true);
		brandToPatch = brandRepository.save(brandToPatch);
		
		PatchBrandDto patchDto = modelMapper.map(brandToPatch, PatchBrandDto.class);
		patchDto.getAddress().setStreetName(null);
		patchDto.setEmail(null);
		
		// Act
		BrandDto patchedDto = 
				this.restClient.patch()
					.uri(HttpUtils.createURL(URI.create(String.format(patchURI, brandToPatch.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(patchDto)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(BrandDto.class) 
					.returnResult()
				    .getResponseBody(); 

		patchDto.setEmail(brandToPatch.getEmail());
		patchDto.getAddress().setStreetName(brandToPatch.getAddress().getStreetName());
		
		// Assert
	 	assertBrand(modelMapper.map(patchDto, BrandDto.class), patchedDto);
	}
	
	@Test
	void testPatchFailureNotFound() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		Brand patchTarget = BrandBuilder.build(faker.code().isbn10(),faker.company().name());
		PatchBrandDto patchDto = modelMapper.map(patchTarget, PatchBrandDto.class);
		
		// Act
		BrandDto _ = 
				this.restClient.patch()
					.uri(HttpUtils.createURL(URI.create(String.format(patchURI, patchTarget.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(patchDto)
					.exchange() 
				    .expectStatus().isNotFound() 
					.expectBody(BrandDto.class) 
					.returnResult()
				    .getResponseBody(); 
	}

	@Test
	void testActivateSuccess() throws JsonProcessingException, MalformedURLException {
		// Arrange
		Brand brandToActivate = BrandBuilder.build(faker.code().isbn10(),faker.company().name());
		brandToActivate.setActive(false);
		brandToActivate.setActivatedOn(null);
		brandToActivate.setDeactivatedOn(null);
		brandToActivate = brandRepository.save(brandToActivate);

		brandToActivate.setActive(true);
		brandToActivate.setActivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		brandToActivate.setDeactivatedOn(null);

		// Act
		BrandDto activatedDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(postActivateURI, brandToActivate.getUuid())),	port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(BrandDto.class) 
					.returnResult()
				    .getResponseBody(); 

		// Assert
		assertBrand(modelMapper.map(brandToActivate, BrandDto.class), activatedDto);
	}

	@Test
	void testActivateFailureNotFound() throws JsonProcessingException, MalformedURLException, Exception {
		// Act
		BrandDto _ = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(postActivateURI, faker.code().isbn10())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isNotFound() 
					.expectBody(BrandDto.class) 
					.returnResult()
				    .getResponseBody(); 
	}

	@Test
	void testDeactivateSuccess() throws JsonProcessingException, MalformedURLException {
		// Arrange
		Brand brandToDeactivate = BrandBuilder.build(faker.code().isbn10(),faker.company().name());
		brandToDeactivate.setActive(true);
		brandToDeactivate.setActivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		brandToDeactivate = brandRepository.save(brandToDeactivate);

		brandToDeactivate.setActive(false);
		brandToDeactivate.setDeactivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));

		// Act
		BrandDto deactivatedDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(postDeactivateURI, brandToDeactivate.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(BrandDto.class) 
					.returnResult()
				    .getResponseBody(); 

		// Assert
		assertBrand(modelMapper.map(brandToDeactivate, BrandDto.class), deactivatedDto);
	}

	@Test
	void testDeactivateFailureNotFound() throws JsonProcessingException, MalformedURLException, Exception {
		// Act
		BrandDto _ = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(postDeactivateURI, faker.code().isbn10())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isNotFound() 
					.expectBody(BrandDto.class) 
					.returnResult()
				    .getResponseBody();
	}

	@Test
	void testDeleteSuccess() throws JsonProcessingException, MalformedURLException {
		// Arrange
		PostBrandDto postDto = modelMapper.map(BrandBuilder.build(faker.code().isbn10(),faker.company().name()), PostBrandDto.class);
		
		BrandDto createdDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(postURI), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(postDto)
					.exchange() 
				    .expectStatus().isCreated() 
					.expectBody(BrandDto.class) 
					.returnResult()
				    .getResponseBody();  

		Populator populator = new Populator(gymRepository, coachRepository, courseRepository, membershipPlanRepository, memberRepository, modelMapper, restClient, port);
		BrandDto brandToDelete = populator.populateFullBrand(createdDto, null);

		// Act
		BrandDto _ = 
				this.restClient.delete()
					.uri(HttpUtils.createURL(URI.create(String.format(deleteURI, brandToDelete.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isAccepted() 
					.expectBody(BrandDto.class) 
					.returnResult()
				    .getResponseBody(); 

		BrandDto _ = 
				this.restClient.get()
					.uri(HttpUtils.createURL(URI.create(String.format(getURI, brandToDelete.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isNotFound() 
					.expectBody(BrandDto.class) 
					.returnResult()
				    .getResponseBody(); 

		PageResult<Gym> pageGym = gymRepository.findAllByBrandUuidAndDeletedIsFalse(brandToDelete.getUuid(),  PageRequest.of(0, 1000));
		Assertions.assertEquals(0, pageGym.getTotalElements(),
				String.format("Deleted brand gyms not deleted: %d", pageGym.getTotalElements()));
		
		PageResult<Coach> pageCoach = coachRepository.findAllByBrandUuidAndDeletedIsFalse(brandToDelete.getUuid(),  PageRequest.of(0, 1000));
		Assertions.assertEquals(0, pageCoach.getTotalElements(),
				String.format("Deleted brand coachs not deleted: %d", pageCoach.getTotalElements()));
		
		PageResult<Course> pageCourse = courseRepository.findAllByBrandUuidAndDeletedIsFalse(brandToDelete.getUuid(),  PageRequest.of(0, 1000));
		Assertions.assertEquals(0, pageCourse.getTotalElements(),
				String.format("Deleted brand courses not deleted: %d", pageCourse.getTotalElements()));
		
		PageResult<MembershipPlan> pageMembershipPlan = membershipPlanRepository.findAllByBrandUuidAndDeletedIsFalse(brandToDelete.getUuid(),  PageRequest.of(0, 1000));
		Assertions.assertEquals(0, pageMembershipPlan.getTotalElements(),
				String.format("Deleted brand membership plans not deleted: %d", pageMembershipPlan.getTotalElements()));
	}
	
	private void assertSearch(String criteria, int minimumNumberOfElements, int maximumNumberOfElements) 
				throws MalformedURLException, JsonProcessingException, Exception
			{
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
	        	// Act
	    		PageResultDto<BrandSearchDto> page = 
	    				this.restClient.get()
	    					.uri(HttpUtils.createURL(URI.create(searchURI), port, params))
	    					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
	    					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
	    					.exchange() 
	    				    .expectStatus().isOk() 
	    					.expectBody(new ParameterizedTypeReference<PageResultDto<BrandSearchDto>>() {}) 
	    					.returnResult()
	    				    .getResponseBody(); 
	    		// Assert
				Assertions.assertTrue(page.getContent().size() >= minimumNumberOfElements && 
									  page.getContent().size() <= maximumNumberOfElements,
					String.format("Brand search return invalid number of results [%s]: %d", 
							criteria, page.getContent().size()));
			});
		}

	public static final void assertBrand(BrandDto expected, BrandDto result) {
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
		
		if (expected.getCurrency() != null) {
			Assertions.assertEquals(expected.getCurrency().getCode(), result.getCurrency().getCode());
			Assertions.assertEquals(expected.getCurrency().getName(), result.getCurrency().getName());
			Assertions.assertEquals(expected.getCurrency().getSymbol(), result.getCurrency().getSymbol());
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
	}
}
