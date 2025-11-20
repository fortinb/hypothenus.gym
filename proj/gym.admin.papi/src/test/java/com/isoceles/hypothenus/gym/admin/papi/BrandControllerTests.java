package com.isoceles.hypothenus.gym.admin.papi;

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
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.isoceles.hypothenus.gym.admin.papi.dto.contact.ContactDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.contact.PhoneNumberDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.model.BrandDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.patch.PatchBrandDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.post.PostBrandDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.put.PutBrandDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.search.BrandSearchDto;
import com.isoceles.hypothenus.gym.domain.exception.DomainException;
import com.isoceles.hypothenus.gym.domain.model.aggregate.Brand;
import com.isoceles.hypothenus.gym.domain.repository.BrandRepository;
import com.isoceles.hypothenus.tests.http.HttpUtils;
import com.isoceles.hypothenus.tests.model.BrandBuilder;
import com.isoceles.hypothenus.tests.security.Roles;
import com.isoceles.hypothenus.tests.security.Users;
import com.isoceles.hypothenus.tests.utils.StringUtils;

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

	public static final String brandId_FitnessBoxing = "FitnessBoxing";
	public static final String brandId_CrossfitExtreme= "CrossfitExtreme";
	
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

		brand = BrandBuilder.build(brandId_FitnessBoxing, "Fitness Boxing");
		brandRepository.save(brand);
		
		brand = BrandBuilder.build(brandId_CrossfitExtreme, "Crossfit Extreme");
		brandRepository.save(brand);
		
		brand = BrandBuilder.build(faker.code().isbn10(),faker.company().name());
		brandRepository.save(brand);
		
		brandIsDeleted = BrandBuilder.build(faker.code().isbn10(), faker.company().name());
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
		Page<BrandSearchDto> response = search(criteria);

		Assertions.assertTrue(response.getNumberOfElements() == 0,
				String.format("Brand search by name return results for isDeleted [%s]", criteria));
	}
	
	@Test
	void testSearchAutocompleteCitySuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(brand.getAddress().getCity(), 3);
		Page<BrandSearchDto> response = search(criteria);

		// Assert
		Assertions.assertTrue(response.getNumberOfElements() > 0,
				String.format("Brand search by city return no results [%s]", criteria));
	}

	@Test
	void testSearchAutocompleteStateSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(brand.getAddress().getState(), 2);
		Page<BrandSearchDto> response = search(criteria);

		// Assert
		Assertions.assertTrue(response.getNumberOfElements() > 0,
				String.format("Brand search by state return no results [%s]", criteria));
	}

	@Test
	void testSearchAutocompleteStreetNameSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(brand.getAddress().getStreetName(), 3);
		Page<BrandSearchDto> response = search(criteria);

		// Assert
		Assertions.assertTrue(response.getNumberOfElements() > 0,
				String.format("Brand search by streetName return no results [%s]", criteria));
	}

	@Test
	void testSearchAutocompleteZipCodeSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(brand.getAddress().getZipCode(), 3);
		Page<BrandSearchDto> response = search(criteria);

		// Assert
		Assertions.assertTrue(response.getNumberOfElements() > 0,
				String.format("Brand search by zipCode return no results [%s]", criteria));
	}

	@Test
	void testSearchAutocompleteNameSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(brand.getName(), 3);
		Page<BrandSearchDto> response = search(criteria);

		// Assert
		Assertions.assertTrue(response.getNumberOfElements() > 0,
				String.format("Brand search by name return no results [%s]", criteria));
	}

	@Test
	void testSearchAutocompleteEmailSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(brand.getEmail(), 3);
		Page<BrandSearchDto> response = search(criteria);

		// Assert
		Assertions.assertTrue(response.getNumberOfElements() > 0,
				String.format("Brand search by email return no results [%s]", criteria));
	}

	@Test
	void testListFirstPageSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		HttpEntity<String> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, null);

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "0");
		params.add(pageSize, "4");

		// Act
		ResponseEntity<Page<BrandDto>> response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(listURI), port, params),
				HttpMethod.GET, httpEntity, new ParameterizedTypeReference<Page<BrandDto>>() {
				});

		// Assert
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		// Assert
		Assertions.assertEquals(0, response.getBody().getPageable().getPageNumber(),
				String.format("Brand list first page number invalid: %d", response.getBody().getPageable().getPageNumber()));
		Assertions.assertEquals(4, response.getBody().getNumberOfElements(),
				String.format("Brand list first page number of elements invalid: %d", response.getBody().getNumberOfElements()));
	}

	@Test
	void testListSecondPageSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		HttpEntity<String> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, "");

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "1");
		params.add(pageSize, "4");

		// Act
		ResponseEntity<Page<BrandDto>> response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(listURI), port, params),
				HttpMethod.GET, httpEntity, new ParameterizedTypeReference<Page<BrandDto>>() {
				});


		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		// Assert
		Assertions.assertEquals(1, response.getBody().getPageable().getPageNumber(),
				String.format("Brand list second page number invalid: %d", response.getBody().getPageable().getPageNumber()));
		Assertions.assertEquals(4, response.getBody().getNumberOfElements(),
				String.format("Brand list second page number of elements invalid: %d", response.getBody().getNumberOfElements()));
	}

	@Test
	void testPostSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostBrandDto postBrand = modelMapper.map(BrandBuilder.build(faker.code().isbn10(),faker.company().name()), PostBrandDto.class);
		HttpEntity<PostBrandDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postBrand);

		// Act
		ResponseEntity<BrandDto> response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(postURI), port, null),
				HttpMethod.POST, httpEntity, BrandDto.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));

		assertBrand(modelMapper.map(postBrand, BrandDto.class), response.getBody());
	}
	
	@Test
	void testPostDuplicateFailure() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostBrandDto postBrand = modelMapper.map(BrandBuilder.build(faker.code().isbn10(),faker.company().name()), PostBrandDto.class);
		HttpEntity<PostBrandDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postBrand);

		ResponseEntity<BrandDto> response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(postURI), port, null),
				HttpMethod.POST, httpEntity, BrandDto.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));

		// Act
		response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(postURI), port, null),
				HttpMethod.POST, httpEntity, BrandDto.class);
		
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
		
		Assertions.assertEquals(1, response.getBody().getMessages().size(),
				String.format("Duplicate error ,missing message: %s", response.getBody().getMessages().size()));
		
		Assertions.assertEquals(DomainException.BRAND_CODE_ALREADY_EXIST, response.getBody().getMessages().getFirst().getCode(),
				String.format("Duplicate error, missing message: %s", response.getBody().getMessages().getFirst().getCode()));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis", "Member, Guillaume Fortin", })
	void testGetSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostBrandDto postBrand = modelMapper.map(BrandBuilder.build(faker.code().isbn10(),faker.company().name()), PostBrandDto.class);
		HttpEntity<PostBrandDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postBrand);

		ResponseEntity<BrandDto> responsePost = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(postURI), port, null), HttpMethod.POST, httpEntity, BrandDto.class);

		Assertions.assertEquals(HttpStatus.CREATED, responsePost.getStatusCode(),
				String.format("Post error: %s", responsePost.getStatusCode()));

		// Act
		httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<BrandDto> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(getURI, responsePost.getBody().getBrandId())), port, null),
				HttpMethod.GET, httpEntity, BrandDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));

		assertBrand(modelMapper.map(postBrand, BrandDto.class), response.getBody());
	}

	@Test
	void testPutSuccess() throws JsonProcessingException, MalformedURLException {
		// Arrange
		Brand updatedBrand = BrandBuilder.build(faker.code().isbn10(),faker.company().name());
		updatedBrand.setActive(true);
		updatedBrand = brandRepository.save(updatedBrand);

		PutBrandDto putBrand = modelMapper.map(updatedBrand, PutBrandDto.class);
		putBrand.getContacts().remove(1);

		// Act
		HttpEntity<PutBrandDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, putBrand);
		ResponseEntity<BrandDto> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, updatedBrand.getBrandId())), port, null),
				HttpMethod.PUT, httpEntity, BrandDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put error: %s", response.getStatusCode()));

		assertBrand(modelMapper.map(putBrand, BrandDto.class), response.getBody());
	}
	
	@Test
	void testPutNullSuccess() throws JsonProcessingException, MalformedURLException {
		// Arrange
		Brand updatedBrand = BrandBuilder.build(faker.code().isbn10(),faker.company().name());
		updatedBrand.setActive(true);
		updatedBrand = brandRepository.save(updatedBrand);
		
		PutBrandDto putBrand = modelMapper.map(updatedBrand, PutBrandDto.class);
		
		putBrand.setEmail(null);
		putBrand.setAddress(null);
		putBrand.setName(null);
		putBrand.setPhoneNumbers(null);
		putBrand.setContacts(null);
		
		// Act
		HttpEntity<PutBrandDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, putBrand);
		ResponseEntity<BrandDto> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, updatedBrand.getBrandId())), port, null),
				HttpMethod.PUT, httpEntity, BrandDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put null error: %s", response.getStatusCode()));

 		assertBrand(modelMapper.map(putBrand, BrandDto.class), response.getBody());
	}

	@Test
	void testPatchSuccess() throws JsonProcessingException, MalformedURLException {
		// Arrange
		Brand brandToPatch = BrandBuilder.build(faker.code().isbn10(),faker.company().name());
		brandToPatch.setActive(true);
		brandToPatch = brandRepository.save(brandToPatch);
		
		PatchBrandDto patchBrand = modelMapper.map(brandToPatch, PatchBrandDto.class);
		patchBrand.getAddress().setStreetName(null);
		patchBrand.setEmail(null);
		patchBrand.setName(null);
		
		// Act
		HttpEntity<PatchBrandDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, patchBrand);
		ResponseEntity<BrandDto> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(patchURI, brandToPatch.getBrandId())), port, null),
				HttpMethod.PATCH, httpEntity, BrandDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));

		patchBrand.setEmail(brandToPatch.getEmail());
		patchBrand.setName(brandToPatch.getName());
		patchBrand.getAddress().setStreetName(brandToPatch.getAddress().getStreetName());
		
 		assertBrand(modelMapper.map(patchBrand, BrandDto.class), response.getBody());
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
		ResponseEntity<BrandDto> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postActivateURI, brandToActivate.getBrandId())),
						port, null),
				HttpMethod.POST, httpEntity, BrandDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Brand activation error: %s", response.getStatusCode()));

		assertBrand(modelMapper.map(brandToActivate, BrandDto.class), response.getBody());
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testActivateFailure(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange

		// Act
		HttpEntity<PutBrandDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<Object> response = testRestTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(postActivateURI, faker.code().isbn10())), port, null),
				HttpMethod.POST, httpEntity, Object.class);
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																											
		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Brand activation error: %s", response.getStatusCode()));
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
		ResponseEntity<BrandDto> response = testRestTemplate.exchange(HttpUtils.createURL(
				URI.create(String.format(postDeactivateURI, brandToDeactivate.getBrandId())), port, null),
				HttpMethod.POST, httpEntity, BrandDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Brand deactivation error: %s", response.getStatusCode()));

		assertBrand(modelMapper.map(brandToDeactivate, BrandDto.class), response.getBody());
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testDeactivateFailure(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange

		// Act
		HttpEntity<PutBrandDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<Object> response = testRestTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(postDeactivateURI, faker.code().ean13())), port, null),
				HttpMethod.POST, httpEntity, Object.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Brand activation error: %s", response.getStatusCode()));
	}

	private Page<BrandSearchDto> search(String criteria)
			throws JsonProcessingException, MalformedURLException {
		// Arrange
		HttpEntity<String> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, "");

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(searchCriteria, criteria);
		params.add(pageNumber, "0");
		params.add(pageSize, "4");
		
		// Act
		ResponseEntity<Page<BrandSearchDto>> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(searchURI), port, params), HttpMethod.GET, httpEntity,
				new ParameterizedTypeReference<Page<BrandSearchDto>>() {
				});

		Assertions.assertEquals(response.getStatusCode(), HttpStatus.OK,
				String.format("Search error: %s", response.getStatusCode()));
		return response.getBody();
	}

	public static final void assertBrand(BrandDto expected, BrandDto result) {
		Assertions.assertEquals(expected.getBrandId(), result.getBrandId());
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
