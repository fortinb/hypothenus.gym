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
import com.iso.hypo.admin.papi.dto.model.BrandDto;
import com.iso.hypo.admin.papi.dto.patch.PatchBrandDto;
import com.iso.hypo.admin.papi.dto.post.PostBrandDto;
import com.iso.hypo.admin.papi.dto.put.PutBrandDto;
import com.iso.hypo.admin.papi.dto.search.BrandSearchDto;
import com.iso.hypo.domain.BrandBuilder;
import com.iso.hypo.domain.aggregate.Brand;
import com.iso.hypo.repositories.BrandRepository;
import com.iso.hypo.services.exception.BrandException;
import com.iso.hypo.tests.http.HttpUtils;
import com.iso.hypo.tests.security.Roles;
import com.iso.hypo.tests.security.Users;
import com.iso.hypo.tests.utils.StringUtils;
import com.iso.hypo.tests.utils.TestResponseUtils;

import net.datafaker.Faker;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
class BrandControllerTests {

	public static final String searchURI = "/v1/brands/search";
	public static final String listURI = "/v1/brands";
	public static final String postURI = "/v1/brands";
	public static final String getURI = "/v1/brands/%s";
	public static final String putURI = "/v1/brands/%s";
	public static final String patchURI = "/v1/brands/%s";
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
	ObjectMapper objectMapper;

	@Autowired
	ModelMapper modelMapper;

	private Faker faker = new Faker();
	
	private RestTemplateBuilder restTemplateBuilder;
	private TestRestTemplate testRestTemplate;
	private Brand brand;
	private Brand brandIsDeleted;
	private List<Brand> brands = new ArrayList<Brand>();

	@BeforeAll
	void arrange() {
		restTemplateBuilder = new RestTemplateBuilder()
					.additionalMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper));
					//.requestFactory(new HttpComponentsClientHttpRequestFactory());
				    //.build();
		
		testRestTemplate = new TestRestTemplate(restTemplateBuilder);
		//testRestTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
		brandRepository.deleteAll();

		brand = BrandBuilder.build(codeBrand_1, faker.company().name());
		brandRepository.save(brand);
		
		brandIsDeleted = BrandBuilder.build(faker.code().isbn10(), faker.code().isbn10());
		brandIsDeleted.setDeleted(true);
		brandIsDeleted = brandRepository.save(brandIsDeleted);

		for (int i = 0; i < 10; i++) {
			Brand item = BrandBuilder.build(faker.code().isbn10(), faker.company().name());
			
			brandRepository.save(item);
			brands.add(item);
		}
		
		for (int i = 0; i < 5; i++) {
			Brand item = BrandBuilder.build(faker.code().isbn10(),faker.company().name());
			item.setActive(false);
			brandRepository.save(item);
			brands.add(item);
		}
	}

	@AfterAll
	void cleanup() {
		// Cleanup
	//	brandRepository.deleteAll();
	}

	@Test
	void testSearchAutocompleteIsDeletedSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(brandIsDeleted.getName(), 10);
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
		HttpEntity<String> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, null);

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "0");
		params.add(pageSize, "4");

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(listURI), port, params),
				HttpMethod.GET, httpEntity, JsonNode.class);

		// Assert
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		Page<BrandDto> page = TestResponseUtils.toPage(response, new TypeReference<Page<BrandDto>>() {}, objectMapper);

		// Assert
		Assertions.assertEquals(0, page.getPageable().getPageNumber(),
				String.format("Brand list first page number invalid: %d", page.getPageable().getPageNumber()));
		Assertions.assertEquals(4, page.getNumberOfElements(),
				String.format("Brand list first page number of elements invalid: %d", page.getNumberOfElements()));
	}

	@Test
	void testListSecondPageSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		HttpEntity<String> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, "");

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "1");
		params.add(pageSize, "4");

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(listURI), port, params),
				HttpMethod.GET, httpEntity, JsonNode.class);


		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));
		
		Page<BrandDto> page = TestResponseUtils.toPage(response, new TypeReference<Page<BrandDto>>() {}, objectMapper);

		// Assert
		Assertions.assertEquals(1, page.getPageable().getPageNumber(),
				String.format("Brand list second page number invalid: %d", page.getPageable().getPageNumber()));
		Assertions.assertEquals(4, page.getNumberOfElements(),
				String.format("Brand list second page number of elements invalid: %d", page.getNumberOfElements()));
	}

	@Test
	void testPostSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostBrandDto postDto = modelMapper.map(BrandBuilder.build(faker.code().isbn10(),faker.company().name()), PostBrandDto.class);
		HttpEntity<PostBrandDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(postURI), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));

		BrandDto createdDto = TestResponseUtils.toDto(response, BrandDto.class, objectMapper);

		assertBrand(modelMapper.map(postDto, BrandDto.class), createdDto);
	}
	
	@Test
	void testPostDuplicateFailure() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostBrandDto postDto = modelMapper.map(BrandBuilder.build(faker.code().isbn10(),faker.company().name()), PostBrandDto.class);
		HttpEntity<PostBrandDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postDto);

		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(postURI), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));

		// Act
		response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(postURI), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);
		
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
		
		BrandDto dupDto = TestResponseUtils.toDto(response, BrandDto.class, objectMapper);

		Assertions.assertEquals(1, dupDto.getMessages().size(),
				String.format("Duplicate error ,missing message: %s", dupDto.getMessages().size()));
		
		Assertions.assertEquals(BrandException.BRAND_CODE_ALREADY_EXIST, dupDto.getMessages().getFirst().getCode(),
				String.format("Duplicate error, missing message: %s", dupDto.getMessages().getFirst().getCode()));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis", "Member, Guillaume Fortin", })
	void testGetSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostBrandDto postDto = modelMapper.map(BrandBuilder.build(faker.code().isbn10(),faker.company().name()), PostBrandDto.class);
		HttpEntity<PostBrandDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postDto);

		ResponseEntity<JsonNode> responsePost = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(postURI), port, null), HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.CREATED, responsePost.getStatusCode(),
				String.format("Post error: %s", responsePost.getStatusCode()));
		
		BrandDto createdDto = TestResponseUtils.toDto(responsePost, BrandDto.class, objectMapper);

		// Act
		httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(getURI, createdDto.getUuid())), port, null),
				HttpMethod.GET, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));
		
		BrandDto fetchedDto = TestResponseUtils.toDto(response, BrandDto.class, objectMapper);

		assertBrand(modelMapper.map(postDto, BrandDto.class), fetchedDto);
	}
	
	@Test
	void testGetFailureNotFound() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		HttpEntity<Object> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, null);
 		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(getURI, faker.code().isbn10())), port, null),
				HttpMethod.GET, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));
		
		if (response.getBody() != null && !response.getBody().isEmpty()) {
			ErrorDto err = TestResponseUtils.toError(response, objectMapper);
			Assertions.assertEquals(BrandException.BRAND_NOT_FOUND, err.getCode());
		}
	}

	@Test
	void testPutSuccess() throws JsonProcessingException, MalformedURLException {
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
		HttpEntity<PutBrandDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, putDto);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, updatedBrand.getUuid())), port, null),
				HttpMethod.PUT, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put error: %s", response.getStatusCode()));
		
		BrandDto updatedDto = TestResponseUtils.toDto(response, BrandDto.class, objectMapper);

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
		HttpEntity<PutBrandDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, putDto);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, updatedBrand.getUuid())), port, null),
				HttpMethod.PUT, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put null error: %s", response.getStatusCode()));
		
		BrandDto updatedDto = TestResponseUtils.toDto(response, BrandDto.class, objectMapper);

	 	assertBrand(modelMapper.map(putDto, BrandDto.class), updatedDto);
	}
	
	@Test
	void testPutFailureNotFound() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		Brand updatedBrand = BrandBuilder.build(faker.code().isbn10(),faker.company().name());
		PutBrandDto putDto = modelMapper.map(updatedBrand, PutBrandDto.class);
		
		HttpEntity<PutBrandDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, putDto);
 		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, updatedBrand.getUuid())), port, null),
				HttpMethod.PUT, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));
		
		if (response.getBody() != null && !response.getBody().isEmpty()) {
			ErrorDto err = TestResponseUtils.toError(response, objectMapper);
			Assertions.assertEquals(BrandException.BRAND_NOT_FOUND, err.getCode());
		}
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPatchSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Brand brandToPatch = BrandBuilder.build(faker.code().isbn10(),faker.company().name());
		brandToPatch.setActive(true);
		brandToPatch = brandRepository.save(brandToPatch);
		
		PatchBrandDto patchDto = modelMapper.map(brandToPatch, PatchBrandDto.class);
		patchDto.getAddress().setStreetName(null);
		patchDto.setEmail(null);
		
		// Act
		HttpEntity<PatchBrandDto> httpEntity = HttpUtils.createHttpEntity(role, user, patchDto);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(patchURI, brandToPatch.getUuid())), port, null),
				HttpMethod.PATCH, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));
		
		patchDto.setEmail(brandToPatch.getEmail());
		patchDto.getAddress().setStreetName(brandToPatch.getAddress().getStreetName());
		
	 	BrandDto patchedDto = TestResponseUtils.toDto(response, BrandDto.class, objectMapper);
	 	assertBrand(modelMapper.map(patchDto, BrandDto.class), patchedDto);
	}
	
	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPatchFailureNotFound(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		Brand patchTarget = BrandBuilder.build(faker.code().isbn10(),faker.company().name());
		PatchBrandDto patchDto = modelMapper.map(patchTarget, PatchBrandDto.class);
		
		HttpEntity<PatchBrandDto> httpEntity = HttpUtils.createHttpEntity(role, user, patchDto);
		
		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(patchURI, patchTarget.getUuid())), port, null),
				HttpMethod.PATCH, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Patch error: %s", response.getStatusCode()));
		
		if (response.getBody() != null && !response.getBody().isEmpty()) {
			ErrorDto err = TestResponseUtils.toError(response, objectMapper);
			Assertions.assertEquals(BrandException.BRAND_NOT_FOUND, err.getCode());
		}
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testActivateSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
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
		HttpEntity<PutBrandDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postActivateURI, brandToActivate.getUuid())),
						port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Brand activation error: %s", response.getStatusCode()));
		
		BrandDto activatedDto = TestResponseUtils.toDto(response, BrandDto.class, objectMapper);

		assertBrand(modelMapper.map(brandToActivate, BrandDto.class), activatedDto);
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testActivateFailureNotFound(String role, String user) throws JsonProcessingException, MalformedURLException, Exception {
		// Arrange
		HttpEntity<PutBrandDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		
		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(postActivateURI, faker.code().isbn10())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);
							
		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Brand activation error: %s", response.getStatusCode()));
		
		if (response.getBody() != null && response.getBody().size() > 0) {
			ErrorDto err = TestResponseUtils.toError(response, objectMapper);
			Assertions.assertEquals(BrandException.BRAND_NOT_FOUND, err.getCode());
		}
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testDeactivateSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Brand brandToDeactivate = BrandBuilder.build(faker.code().isbn10(),faker.company().name());
		brandToDeactivate.setActive(true);
		brandToDeactivate.setActivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		brandToDeactivate = brandRepository.save(brandToDeactivate);

		brandToDeactivate.setActive(false);
		brandToDeactivate.setDeactivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));

		// Act
		HttpEntity<PutBrandDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(
				URI.create(String.format(postDeactivateURI, brandToDeactivate.getUuid())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Brand deactivation error: %s", response.getStatusCode()));
		
		BrandDto deactivatedDto = TestResponseUtils.toDto(response, BrandDto.class, objectMapper);

		assertBrand(modelMapper.map(brandToDeactivate, BrandDto.class), deactivatedDto);
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testDeactivateFailureNotFound(String role, String user) throws JsonProcessingException, MalformedURLException, Exception {
		// Arrange
		HttpEntity<PutBrandDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		
		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(postDeactivateURI, faker.code().ean13())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);
		
		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Brand activation error: %s", response.getStatusCode()));
		
		if (response.getBody() != null && response.getBody().size() > 0) {
			ErrorDto err = TestResponseUtils.toError(response, objectMapper);
			Assertions.assertEquals(BrandException.BRAND_NOT_FOUND, err.getCode());
		}
	}

	private void assertSearch(String criteria, int minimumNumberOfElements, int maximumNumberOfElements) 
				throws MalformedURLException, JsonProcessingException, Exception
			{
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
						HttpUtils.createURL(URI.create(searchURI), port, params), HttpMethod.GET, httpEntity,
						JsonNode.class);

				Assertions.assertEquals(response.getStatusCode(), HttpStatus.OK,
						String.format("Search error: %s", response.getStatusCode()));
				
				Page<BrandSearchDto> page = TestResponseUtils.toPage(response, new TypeReference<Page<BrandSearchDto>>() {}, objectMapper);
				
				Assertions.assertTrue(page.getNumberOfElements() >= minimumNumberOfElements && 
										page.getNumberOfElements() <= maximumNumberOfElements,
					String.format("Brand search return invalid number of results [%s]: %d", 
							criteria, page.getNumberOfElements()));
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
