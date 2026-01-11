package com.iso.hypo.gym.admin.papi;

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
import com.iso.hypo.gym.admin.papi.dto.contact.ContactDto;
import com.iso.hypo.gym.admin.papi.dto.contact.PhoneNumberDto;
import com.iso.hypo.gym.admin.papi.dto.model.GymDto;
import com.iso.hypo.gym.admin.papi.dto.patch.PatchGymDto;
import com.iso.hypo.gym.admin.papi.dto.post.PostGymDto;
import com.iso.hypo.gym.admin.papi.dto.put.PutGymDto;
import com.iso.hypo.gym.admin.papi.dto.search.GymSearchDto;
import com.iso.hypo.gym.exception.GymException;
import com.iso.hypo.gym.domain.aggregate.Gym;
import com.iso.hypo.gym.repository.GymRepository;
import com.iso.hypo.tests.http.HttpUtils;
import com.iso.hypo.tests.model.GymBuilder;
import com.iso.hypo.tests.security.Roles;
import com.iso.hypo.tests.security.Users;
import com.iso.hypo.tests.utils.StringUtils;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
class GymControllerTests {

	public static final String searchURI = "/v1/brands/%s/gyms/search";
	public static final String listURI = "/v1/brands/%s/gyms";
	public static final String postURI = "/v1/brands/%s/gyms";
	public static final String getURI = "/v1/brands/%s/gyms/%s";
	public static final String putURI = "/v1/brands/%s/gyms/%s";
	public static final String patchURI = "/v1/brands/%s/gyms/%s";
	public static final String postActivateURI = "/v1/brands/%s/gyms/%s/activate";
	public static final String postDeactivateURI = "/v1/brands/%s/gyms/%s/deactivate";
	public static final String searchCriteria = "criteria";
	public static final String pageNumber = "page";
	public static final String pageSize = "pageSize";

	public static final String brandId_FitnessBoxing = "FitnessBoxing";
	public static final String brandId_CrossfitExtreme= "CrossfitExtreme";
	
	public static final String gymId_Boucherville = "BoxingBoucherville";
	public static final String gymId_Longueuil = "CrossfitLongueuil";
	
	@LocalServerPort
	private int port;

	@Autowired
	GymRepository gymRepository;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	ModelMapper modelMapper;

	private Faker faker = new Faker();
	
	private RestTemplateBuilder restTemplateBuilder;
	private TestRestTemplate testRestTemplate;
	private Gym gym;
	private Gym gymIsDeleted;
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

		gym = GymBuilder.build(brandId_FitnessBoxing, gymId_Boucherville, "Fitness Boxing Boucherville");
		gymRepository.save(gym);
		
		gym = GymBuilder.build(brandId_FitnessBoxing, gymId_Longueuil, "Crossfit Longueuil");
		gymRepository.save(gym);
		
		gymIsDeleted = GymBuilder.build(brandId_FitnessBoxing, faker.code().isbn10(),faker.company().name());
		gymIsDeleted.setDeleted(true);
		gymIsDeleted = gymRepository.save(gymIsDeleted);

		for (int i = 0; i < 10; i++) {
			Gym item = GymBuilder.build(brandId_FitnessBoxing, faker.code().isbn10(),faker.company().name());
			
			gymRepository.save(item);
			gyms.add(item);
		}
		
		for (int i = 0; i < 5; i++) {
			Gym item = GymBuilder.build(brandId_FitnessBoxing, faker.code().isbn10(),faker.company().name());
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
		Page<GymSearchDto> response = search(criteria);

		Assertions.assertTrue(response.getNumberOfElements() == 0,
				String.format("Gym search by name return results for isDeleted [%s]", criteria));
	}
	
	@Test
	void testSearchAutocompleteCitySuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(gym.getAddress().getCity(), 3);
		Page<GymSearchDto> response = search(criteria);

		// Assert
		Assertions.assertTrue(response.getNumberOfElements() > 0,
				String.format("Gym search by city return no results [%s]", criteria));
	}

	@Test
	void testSearchAutocompleteStateSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(gym.getAddress().getState(), 2);
		Page<GymSearchDto> response = search(criteria);

		// Assert
		Assertions.assertTrue(response.getNumberOfElements() > 0,
				String.format("Gym search by state return no results [%s]", criteria));
	}

	@Test
	void testSearchAutocompleteStreetNameSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(gym.getAddress().getStreetName(), 3);
		Page<GymSearchDto> response = search(criteria);

		// Assert
		Assertions.assertTrue(response.getNumberOfElements() > 0,
				String.format("Gym search by streetName return no results [%s]", criteria));
	}

	@Test
	void testSearchAutocompleteZipCodeSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(gym.getAddress().getZipCode(), 3);
		Page<GymSearchDto> response = search(criteria);

		// Assert
		Assertions.assertTrue(response.getNumberOfElements() > 0,
				String.format("Gym search by zipCode return no results [%s]", criteria));
	}

	@Test
	void testSearchAutocompleteNameSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(gym.getName(), 3);
		Page<GymSearchDto> response = search(criteria);

		// Assert
		Assertions.assertTrue(response.getNumberOfElements() > 0,
				String.format("Gym search by name return no results [%s]", criteria));
	}

	@Test
	void testSearchAutocompleteEmailSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(gym.getEmail(), 3);
		Page<GymSearchDto> response = search(criteria);

		// Assert
		Assertions.assertTrue(response.getNumberOfElements() > 0,
				String.format("Gym search by email return no results [%s]", criteria));
	}

	@Test
	void testListFirstPageSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		HttpEntity<String> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, null);

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "0");
		params.add(pageSize, "4");

		// Act
		ResponseEntity<Page<GymDto>> response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(String.format(listURI, brandId_FitnessBoxing)), port, params),
				HttpMethod.GET, httpEntity, new ParameterizedTypeReference<Page<GymDto>>() {
				});

		// Assert
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		// Assert
		Assertions.assertEquals(0, response.getBody().getPageable().getPageNumber(),
				String.format("Gym list first page number invalid: %d", response.getBody().getPageable().getPageNumber()));
		Assertions.assertEquals(4, response.getBody().getNumberOfElements(),
				String.format("Gym list first page number of elements invalid: %d", response.getBody().getNumberOfElements()));
	}

	@Test
	void testListSecondPageSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		HttpEntity<String> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, "");

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "1");
		params.add(pageSize, "4");

		// Act
		ResponseEntity<Page<GymDto>> response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(String.format(listURI, brandId_FitnessBoxing)), port, params),
				HttpMethod.GET, httpEntity, new ParameterizedTypeReference<Page<GymDto>>() {
				});


		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		// Assert
		Assertions.assertEquals(1, response.getBody().getPageable().getPageNumber(),
				String.format("Gym list second page number invalid: %d", response.getBody().getPageable().getPageNumber()));
		Assertions.assertEquals(4, response.getBody().getNumberOfElements(),
				String.format("Gym list second page number of elements invalid: %d", response.getBody().getNumberOfElements()));
	}

	@Test
	void testPostSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostGymDto postGym = modelMapper.map(GymBuilder.build(brandId_FitnessBoxing, faker.code().isbn10(),faker.company().name()), PostGymDto.class);
		HttpEntity<PostGymDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postGym);

		// Act
		ResponseEntity<GymDto> response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(String.format(postURI, brandId_FitnessBoxing)), port, null),
				HttpMethod.POST, httpEntity, GymDto.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));

		assertGym(modelMapper.map(postGym, GymDto.class), response.getBody());
	}
	
	@Test
	void testPostDuplicateFailure() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostGymDto postGym = modelMapper.map(GymBuilder.build(brandId_FitnessBoxing, faker.code().isbn10(),faker.company().name()), PostGymDto.class);
		HttpEntity<PostGymDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postGym);

		ResponseEntity<GymDto> response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(String.format(postURI, brandId_FitnessBoxing)), port, null),
				HttpMethod.POST, httpEntity, GymDto.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));

		// Act
		response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(String.format(postURI, brandId_FitnessBoxing)), port, null),
				HttpMethod.POST, httpEntity, GymDto.class);
		
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
		
		Assertions.assertEquals(1, response.getBody().getMessages().size(),
				String.format("Duplicate error ,missing message: %s", response.getBody().getMessages().size()));
		
		Assertions.assertEquals(GymException.GYM_CODE_ALREADY_EXIST, response.getBody().getMessages().getFirst().getCode(),
				String.format("Duplicate error, missing message: %s", response.getBody().getMessages().getFirst().getCode()));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis", "Member, Guillaume Fortin", })
	void testGetSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostGymDto postGym = modelMapper.map(GymBuilder.build(brandId_FitnessBoxing, faker.code().isbn10(),faker.company().name()), PostGymDto.class);
		HttpEntity<PostGymDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postGym);

		ResponseEntity<GymDto> responsePost = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postURI, brandId_FitnessBoxing)), port, null), HttpMethod.POST, httpEntity, GymDto.class);

		Assertions.assertEquals(HttpStatus.CREATED, responsePost.getStatusCode(),
				String.format("Post error: %s", responsePost.getStatusCode()));

		// Act
		httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<GymDto> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(getURI, responsePost.getBody().getBrandId(), responsePost.getBody().getGymId())), port, null),
				HttpMethod.GET, httpEntity, GymDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));

		assertGym(modelMapper.map(postGym, GymDto.class), response.getBody());
	}

	@Test
	void testPutSuccess() throws JsonProcessingException, MalformedURLException {
		// Arrange
		Gym updatedGym = GymBuilder.build(brandId_FitnessBoxing, faker.code().isbn10(),faker.company().name());
		updatedGym.setActive(true);
		updatedGym = gymRepository.save(updatedGym);

		PutGymDto putGym = modelMapper.map(updatedGym, PutGymDto.class);
		putGym.getContacts().remove(1);

		// Act
		HttpEntity<PutGymDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, putGym);
		ResponseEntity<GymDto> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, updatedGym.getBrandId(), updatedGym.getGymId())), port, null),
				HttpMethod.PUT, httpEntity, GymDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put error: %s", response.getStatusCode()));

		assertGym(modelMapper.map(putGym, GymDto.class), response.getBody());
	}
	
	@Test
	void testPutNullSuccess() throws JsonProcessingException, MalformedURLException {
		// Arrange
		Gym updatedGym = GymBuilder.build(brandId_FitnessBoxing, faker.code().isbn10(),faker.company().name());
		updatedGym.setActive(true);
		updatedGym = gymRepository.save(updatedGym);
		
		PutGymDto putGym = modelMapper.map(updatedGym, PutGymDto.class);
		
		putGym.setEmail(null);
		putGym.setAddress(null);
		putGym.setPhoneNumbers(null);
		putGym.setContacts(null);
		
		// Act
		HttpEntity<PutGymDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, putGym);
		ResponseEntity<GymDto> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, updatedGym.getBrandId(), updatedGym.getGymId())), port, null),
				HttpMethod.PUT, httpEntity, GymDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put null error: %s", response.getStatusCode()));

 		assertGym(modelMapper.map(putGym, GymDto.class), response.getBody());
	}

	@Test
	void testPatchSuccess() throws JsonProcessingException, MalformedURLException {
		// Arrange
		Gym gymToPatch = GymBuilder.build(brandId_FitnessBoxing, faker.code().isbn10(),faker.company().name());
		gymToPatch.setActive(true);
		gymToPatch = gymRepository.save(gymToPatch);
		
		PatchGymDto patchGym = modelMapper.map(gymToPatch, PatchGymDto.class);
		patchGym.getAddress().setStreetName(null);
		patchGym.setEmail(null);
		
		// Act
		HttpEntity<PatchGymDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, patchGym);
		ResponseEntity<GymDto> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(patchURI, gymToPatch.getBrandId(), gymToPatch.getGymId())), port, null),
				HttpMethod.PATCH, httpEntity, GymDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));

		patchGym.setEmail(gymToPatch.getEmail());
		patchGym.getAddress().setStreetName(gymToPatch.getAddress().getStreetName());
		
 		assertGym(modelMapper.map(patchGym, GymDto.class), response.getBody());
	}
	
	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testActivateSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Gym gymToActivate = GymBuilder.build(brandId_FitnessBoxing, faker.code().isbn10(),faker.company().name());
		gymToActivate.setActive(false);
		gymToActivate.setActivatedOn(null);
		gymToActivate.setDeactivatedOn(null);
		gymToActivate = gymRepository.save(gymToActivate);

		gymToActivate.setActive(true);
		gymToActivate.setActivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		gymToActivate.setDeactivatedOn(null);

		// Act
		HttpEntity<PutGymDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<GymDto> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postActivateURI, gymToActivate.getBrandId(), gymToActivate.getGymId())),
						port, null),
				HttpMethod.POST, httpEntity, GymDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Gym activation error: %s", response.getStatusCode()));

		assertGym(modelMapper.map(gymToActivate, GymDto.class), response.getBody());
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testActivateFailure(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange

		// Act
		HttpEntity<PutGymDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<Object> response = testRestTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(postActivateURI, brandId_FitnessBoxing, faker.code().isbn10())), port, null),
				HttpMethod.POST, httpEntity, Object.class);
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																											
		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Gym activation error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testDeactivateSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Gym gymToDeactivate = GymBuilder.build(brandId_FitnessBoxing, faker.code().isbn10(),faker.company().name());
		gymToDeactivate.setActive(true);
		gymToDeactivate.setActivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		gymToDeactivate = gymRepository.save(gymToDeactivate);

		gymToDeactivate.setActive(false);
		gymToDeactivate.setDeactivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));

		// Act
		HttpEntity<PutGymDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<GymDto> response = testRestTemplate.exchange(HttpUtils.createURL(
				URI.create(String.format(postDeactivateURI, brandId_FitnessBoxing, gymToDeactivate.getGymId())), port, null),
				HttpMethod.POST, httpEntity, GymDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Gym deactivation error: %s", response.getStatusCode()));

		assertGym(modelMapper.map(gymToDeactivate, GymDto.class), response.getBody());
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testDeactivateFailure(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange

		// Act
		HttpEntity<PutGymDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<Object> response = testRestTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(postDeactivateURI, brandId_FitnessBoxing, faker.code().ean13())), port, null),
				HttpMethod.POST, httpEntity, Object.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Gym activation error: %s", response.getStatusCode()));
	}

	private Page<GymSearchDto> search(String criteria)
			throws JsonProcessingException, MalformedURLException {
		// Arrange
		HttpEntity<String> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, "");

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(searchCriteria, criteria);
		params.add(pageNumber, "0");
		params.add(pageSize, "4");
		
		// Act
		ResponseEntity<Page<GymSearchDto>> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(searchURI, brandId_FitnessBoxing) ), port, params), HttpMethod.GET, httpEntity,
				new ParameterizedTypeReference<Page<GymSearchDto>>() {
				});

		Assertions.assertEquals(response.getStatusCode(), HttpStatus.OK,
				String.format("Search error: %s", response.getStatusCode()));
		return response.getBody();
	}

	public static final void assertGym(GymDto expected, GymDto result) {
		if (expected.getId() != null) {
			Assertions.assertEquals(expected.getId(), result.getId());
		}
		
		Assertions.assertEquals(expected.getGymId(), result.getGymId());
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
