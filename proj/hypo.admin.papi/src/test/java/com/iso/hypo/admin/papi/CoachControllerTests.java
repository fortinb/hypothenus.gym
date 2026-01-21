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
import net.datafaker.Faker;
import com.iso.hypo.model.aggregate.Brand;
import com.iso.hypo.model.repository.BrandRepository;
import com.iso.hypo.admin.papi.dto.contact.ContactDto;
import com.iso.hypo.admin.papi.dto.contact.PhoneNumberDto;
import com.iso.hypo.admin.papi.dto.model.CoachDto;
import com.iso.hypo.admin.papi.dto.patch.PatchCoachDto;
import com.iso.hypo.admin.papi.dto.post.PostCoachDto;
import com.iso.hypo.admin.papi.dto.put.PutCoachDto;
import com.iso.hypo.model.aggregate.Coach;
import com.iso.hypo.model.aggregate.Gym;
import com.iso.hypo.model.repository.CoachRepository;
import com.iso.hypo.model.repository.GymRepository;
import com.iso.hypo.tests.http.HttpUtils;
import com.iso.hypo.model.BrandBuilder;
import com.iso.hypo.model.CoachBuilder;
import com.iso.hypo.model.GymBuilder;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
class CoachControllerTests {

	public static final String listURI = "/v1/brands/%s/gyms/%s/coachs";
	public static final String postURI = "/v1/brands/%s/gyms/%s/coachs";
	public static final String getURI = "/v1/brands/%s/gyms/%s/coachs/%s";
	public static final String putURI = "/v1/brands/%s/gyms/%s/coachs/%s";
	public static final String postActivateURI = "/v1/brands/%s/gyms/%s/coachs/%s/activate";
	public static final String postDeactivateURI = "/v1/brands/%s/gyms/%s/coachs/%s/deactivate";
	public static final String patchURI = "/v1/brands/%s/gyms/%s/coachs/%s";
	public static final String pageNumber = "page";
	public static final String pageSize = "pageSize";
	public static final String includeInactive = "includeInactive";
	
	public static final String brandCode_1 = "CoachBrand1";
	public static final String brandCode_2= "CoachBrand2";
	
	public static final String gymCode_1 = "CoachGym1";
	public static final String gymCode_2 = "CoachGym2";
	
	@LocalServerPort
	private int port;

	@Autowired
	BrandRepository brandRepository;
	
	@Autowired
	GymRepository gymRepository;
	
	@Autowired
	CoachRepository coachRepository;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	ModelMapper modelMapper;
	
	private Faker faker = new Faker();

	private TestRestTemplate restTemplate = new TestRestTemplate();

	private Coach coach;
	private Coach coachIsDeleted;
	private Brand brand_1;
	private Brand brand_2;
	private Gym gym_1;
	private Gym gym_2;
	private List<Coach> coachs = new ArrayList<Coach>();

	@BeforeAll
	void arrange() {
		restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
		
		coachRepository.deleteAll();

		brand_1 = BrandBuilder.build(brandCode_1, faker.company().name());
		brandRepository.save(brand_1);
		
		brand_2 = BrandBuilder.build(brandCode_2, faker.company().name());
		brandRepository.save(brand_1);
		
		gym_1 = GymBuilder.build(brand_1.getUuid(), gymCode_1, faker.address().cityName());
		gymRepository.save(gym_1);
		
		gym_2 = GymBuilder.build(brand_2.getUuid(), gymCode_2, faker.address().cityName());
		gymRepository.save(gym_2);
		
		coach = CoachBuilder.build(brand_1.getUuid(), gym_1.getUuid());
		coach.setActive(true);
		coachRepository.save(coach);
		
		coachIsDeleted = CoachBuilder.build(brand_1.getUuid(), gym_1.getUuid());
		coachIsDeleted.setDeleted(true);
		coachIsDeleted = coachRepository.save(coachIsDeleted);

		for (int i = 0; i < 10; i++) {
			Coach item = CoachBuilder.build(brand_1.getUuid(), gym_1.getUuid());
			item.setActive(true);
			coachRepository.save(item);
			coachs.add(item);
		}
		
		for (int i = 0; i < 4; i++) {
			Coach item = CoachBuilder.build(brand_2.getUuid(), gym_2.getUuid());
			item.setActive(true);
			coachRepository.save(item);
			coachs.add(item);
		}
		
		Coach item = CoachBuilder.build(brandCode_2, gym_2.getUuid());
		item.setActive(false);
		coachRepository.save(item);
	}

	@AfterAll
	void cleanup() {
		// Cleanup
	//	coachRepository.deleteAll();
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testListActiveSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		HttpEntity<String> httpEntity = HttpUtils.createHttpEntity(role, user, null);

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "0");
		params.add(pageSize, "5");
		params.add(includeInactive, "false");

		// Act
		ResponseEntity<String> response = restTemplate.exchange(HttpUtils.createURL(URI.create(String.format(listURI, brand_2.getUuid(), gym_2.getUuid())), port, params),
				HttpMethod.GET, httpEntity, new ParameterizedTypeReference<String>() {
				});

		// Assert
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		Page<CoachDto> page = objectMapper.readValue(response.getBody(), new TypeReference<Page<CoachDto>>() {
		});

		// Assert
		Assertions.assertEquals(0, page.getPageable().getPageNumber(),
				String.format("Coach list first page number invalid: %d", page.getPageable().getPageNumber()));
		Assertions.assertEquals(4, page.getNumberOfElements(),
				String.format("Coach list first page number of elements invalid: %d", page.getNumberOfElements()));
		Assertions.assertEquals(4, page.getTotalElements(),
				String.format("Coach total number of elements invalid: %d", page.getTotalElements()));
		
		page.get().forEach(coach ->Assertions.assertTrue(coach.isActive()));
		page.get().forEach(coach ->Assertions.assertTrue(coach.isDeleted() == false));
	}
	
	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testListFirstPageSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		HttpEntity<String> httpEntity = HttpUtils.createHttpEntity(role, user, null);

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "0");
		params.add(pageSize, "5");
		params.add(includeInactive, "true");
		
		// Act
		ResponseEntity<String> response = restTemplate.exchange(HttpUtils.createURL(URI.create(String.format(listURI, brand_2.getUuid(), gym_2.getUuid())), port, params),
				HttpMethod.GET, httpEntity, new ParameterizedTypeReference<String>() {
				});

		// Assert
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		Page<CoachDto> page = objectMapper.readValue(response.getBody(), new TypeReference<Page<CoachDto>>() {
		});

		// Assert
		Assertions.assertEquals(0, page.getPageable().getPageNumber(),
				String.format("Coach list first page number invalid: %d", page.getPageable().getPageNumber()));
		Assertions.assertEquals(4, page.getNumberOfElements(),
				String.format("Coach list first page number of elements invalid: %d", page.getNumberOfElements()));
		Assertions.assertEquals(4, page.getTotalElements(),
				String.format("Coach total number of elements invalid: %d", page.getTotalElements()));
		
		page.get().forEach(coach ->Assertions.assertTrue(coach.isDeleted() == false));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testListSecondPageSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		HttpEntity<String> httpEntity = HttpUtils.createHttpEntity(role, user, "");

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "1");
		params.add(pageSize, "2");

		// Act
		ResponseEntity<String> response = restTemplate.exchange(HttpUtils.createURL(URI.create(String.format(listURI, brand_2.getUuid(), gym_2.getUuid())), port, params),
				HttpMethod.GET, httpEntity, new ParameterizedTypeReference<String>() {
				});

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		Page<CoachDto> page = objectMapper.readValue(response.getBody(), new TypeReference<Page<CoachDto>>() {
		});

		// Assert
		Assertions.assertEquals(1, page.getPageable().getPageNumber(),
				String.format("Coach list second page number invalid: %d", page.getPageable().getPageNumber()));
		Assertions.assertEquals(2, page.getNumberOfElements(),
				String.format("Coach list second page number of elements invalid: %d", page.getNumberOfElements()));
		
		page.get().forEach(coach ->Assertions.assertTrue(coach.isActive()));
		page.get().forEach(coach ->Assertions.assertTrue(coach.isDeleted() == false));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPostSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostCoachDto postCoach = modelMapper.map(CoachBuilder.build(brand_1.getUuid(), gym_1.getUuid()), PostCoachDto.class);
		
		HttpEntity<PostCoachDto> httpEntity = HttpUtils.createHttpEntity(role, user, postCoach);

		// Act
		ResponseEntity<CoachDto> response = restTemplate.exchange(HttpUtils.createURL(URI.create(String.format(postURI, brand_1.getUuid(), gym_1.getUuid())), port, null),
				HttpMethod.POST, httpEntity, CoachDto.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));

		assertCoach(modelMapper.map(postCoach, CoachDto.class), response.getBody());
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testGetSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostCoachDto postCoach = modelMapper.map(CoachBuilder.build(brand_1.getUuid() , gym_1.getUuid()), PostCoachDto.class);

		HttpEntity<PostCoachDto> httpEntity = HttpUtils.createHttpEntity(role, user, postCoach);

		ResponseEntity<CoachDto> responsePost = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postURI, brand_1.getUuid(), gym_1.getUuid())), port, null), HttpMethod.POST, httpEntity, CoachDto.class);

		Assertions.assertEquals(HttpStatus.CREATED, responsePost.getStatusCode(),
				String.format("Post error: %s", responsePost.getStatusCode()));

		// Act
		httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<CoachDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(getURI, brand_1.getUuid(), gym_1.getUuid(), responsePost.getBody().getUuid())), port, null),
				HttpMethod.GET, httpEntity, CoachDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));

		assertCoach(modelMapper.map(postCoach, CoachDto.class), response.getBody());
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPutSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Coach coachToUpdate = CoachBuilder.build(brand_1.getUuid(), gym_1.getUuid());
		coachToUpdate.setActive(false);
		coachToUpdate.setActivatedOn(null);
		coachToUpdate.setDeactivatedOn(null);
		coachToUpdate = coachRepository.save(coachToUpdate);
		
		Coach updatedCoach = CoachBuilder.build(brand_1.getUuid(), gym_1.getUuid());
		updatedCoach.setUuid(coachToUpdate.getUuid());
		updatedCoach.setActive(false);
		updatedCoach.setActivatedOn(null);
		updatedCoach.setDeactivatedOn(null);
		
		PutCoachDto putCoach = modelMapper.map(updatedCoach, PutCoachDto.class);
		putCoach.setUuid(coachToUpdate.getUuid());

		// Act
		HttpEntity<PutCoachDto> httpEntity = HttpUtils.createHttpEntity(role, user, putCoach);
		ResponseEntity<CoachDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, brand_1.getUuid(), gym_1.getUuid(), putCoach.getUuid())), port, null),
				HttpMethod.PUT, httpEntity, CoachDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put error: %s", response.getStatusCode()));

		assertCoach(modelMapper.map(updatedCoach, CoachDto.class), response.getBody());
	}
	
	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPutNullSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Coach coachToUpdate = CoachBuilder.build(brand_1.getUuid(), gym_1.getUuid());
		coachToUpdate = coachRepository.save(coachToUpdate);
		
		Coach updatedCoach = CoachBuilder.build(brand_1.getUuid(), gym_1.getUuid());
		
		PutCoachDto putCoach = modelMapper.map(updatedCoach, PutCoachDto.class);
		putCoach.setUuid(coachToUpdate.getUuid());
		putCoach.getPerson().setEmail(null);
				
		// Act
		HttpEntity<PutCoachDto> httpEntity = HttpUtils.createHttpEntity(role, user, putCoach);
		ResponseEntity<CoachDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, brand_1.getUuid(), gym_1.getUuid(), putCoach.getUuid())), port, null),
				HttpMethod.PUT, httpEntity, CoachDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put null error: %s", response.getStatusCode()));
		
		updatedCoach.setUuid(coachToUpdate.getUuid());
		updatedCoach.getPerson().setEmail(null);
		
 		assertCoach(modelMapper.map(updatedCoach, CoachDto.class), response.getBody());
	}
	
	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testActivateSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Coach coachToActivate = CoachBuilder.build(brand_1.getUuid(), gym_1.getUuid());
		coachToActivate.setActive(false);
		coachToActivate.setActivatedOn(null);
		coachToActivate.setDeactivatedOn(null);
		coachToActivate = coachRepository.save(coachToActivate);
		coachToActivate.setActive(true);
		coachToActivate.setActivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		coachToActivate.setDeactivatedOn(null);
		
		// Act
		HttpEntity<PutCoachDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<CoachDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postActivateURI, brand_1.getUuid(), gym_1.getUuid(), coachToActivate.getUuid())), port, null),
				HttpMethod.POST, httpEntity, CoachDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Coach activation error: %s", response.getStatusCode()));

 		assertCoach(modelMapper.map(coachToActivate, CoachDto.class), response.getBody());
	}
	
	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testActivateFailure(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		
		// Act
		HttpEntity<PutCoachDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<CoachDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postActivateURI, brand_1.getUuid(), gym_1.getUuid(), faker.code().isbn10())), port, null),
				HttpMethod.POST, httpEntity, CoachDto.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Coach activation error: %s", response.getStatusCode()));
	}
	
	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testDeactivateSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Coach coachToDeactivate = CoachBuilder.build(brand_1.getUuid(), gym_1.getUuid());
		coachToDeactivate.setGymUuid(gym_1.getUuid());
		coachToDeactivate.setActive(true);
		coachToDeactivate.setActivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		coachToDeactivate = coachRepository.save(coachToDeactivate);
		coachToDeactivate.setActive(false);
		coachToDeactivate.setDeactivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		
		// Act
		HttpEntity<PutCoachDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<CoachDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postDeactivateURI, brand_1.getUuid(), gym_1.getUuid(), coachToDeactivate.getUuid())), port, null),
				HttpMethod.POST, httpEntity, CoachDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Coach deactivation error: %s", response.getStatusCode()));

 		assertCoach(modelMapper.map(coachToDeactivate, CoachDto.class), response.getBody());
	}
	
	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testDeactivateFailure(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		
		// Act
		HttpEntity<PutCoachDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<CoachDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postDeactivateURI, brand_1.getUuid(), gym_1.getUuid(), faker.code().ean13())), port, null),
				HttpMethod.POST, httpEntity, CoachDto.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Coach activation error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPatchSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Coach coachToPatch = CoachBuilder.build(brand_1.getUuid(), gym_1.getUuid());
		coachToPatch.setActive(true);
		coachToPatch = coachRepository.save(coachToPatch);

		PatchCoachDto patchCoachDto = modelMapper.map(coachToPatch, PatchCoachDto.class);
		//patchCoachDto.setUuid(coachToPatch.getUuid());
		patchCoachDto.getPerson().setEmail(null);
		patchCoachDto.getPerson().setFirstname(null);
		patchCoachDto.getPerson().getAddress().setStreetName(null);
		patchCoachDto.getPerson().setLastname(faker.name().lastName());
		
		// Act
		HttpEntity<PatchCoachDto> httpEntity = HttpUtils.createHttpEntity(role, user, patchCoachDto);
		ResponseEntity<CoachDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(patchURI, brand_1.getUuid(), gym_1.getUuid(), patchCoachDto.getUuid())), port, null),
				HttpMethod.PATCH, httpEntity, CoachDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));

		coachToPatch.getPerson().setLastname(patchCoachDto.getPerson().getLastname());
		
 		assertCoach(modelMapper.map(coachToPatch, CoachDto.class), response.getBody());
	}

	public static final void assertCoach(CoachDto expected, CoachDto result) {
		if (expected.getUuid() != null) {
			Assertions.assertEquals(expected.getUuid(), result.getUuid());
		}
		
		Assertions.assertEquals(expected.getBrandUuid(), result.getBrandUuid());
		Assertions.assertEquals(expected.getGymUuid(), result.getGymUuid());
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
		if (expected.getActivatedOn() == null) {
			Assertions.assertNull(result.getActivatedOn());
		}
		
		if (expected.getDeactivatedOn() != null) {
			Assertions.assertNotNull(result.getDeactivatedOn());
			Assertions.assertTrue(expected.getDeactivatedOn().truncatedTo(ChronoUnit.DAYS).equals(result.getDeactivatedOn().truncatedTo(ChronoUnit.DAYS)));
		}
		if (expected.getDeactivatedOn() == null) {
			Assertions.assertNull(result.getDeactivatedOn());
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
}
