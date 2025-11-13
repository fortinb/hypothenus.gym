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
import com.isoceles.hypothenus.gym.admin.papi.dto.CoachDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.ContactDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.PhoneNumberDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.patch.PatchCoachDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.post.PostCoachDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.put.PutCoachDto;
import com.isoceles.hypothenus.gym.domain.model.aggregate.Coach;
import com.isoceles.hypothenus.gym.domain.repository.CoachRepository;
import com.isoceles.hypothenus.tests.http.HttpUtils;
import com.isoceles.hypothenus.tests.model.CoachBuilder;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
class CoachControllerTests {

	public static final String listURI = "/v1/admin/brands/%s/gyms/%s/coachs";
	public static final String postURI = "/v1/admin/brands/%s/gyms/%s/coachs";
	public static final String getURI = "/v1/admin/brands/%s/gyms/%s/coachs/%s";
	public static final String putURI = "/v1/admin/brands/%s/gyms/%s/coachs/%s";
	public static final String postActivateURI = "/v1/admin/brands/%s/gyms/%s/coachs/%s/activate";
	public static final String postDeactivateURI = "/v1/admin/brands/%s/gyms/%s/coachs/%s/deactivate";
	public static final String patchURI = "/v1/admin/brands/%s/gyms/%s/coachs/%s";
	public static final String pageNumber = "page";
	public static final String pageSize = "pageSize";
	public static final String includeInactive = "includeInactive";
	
	public static final String brandId_FitnessBoxing = "FitnessBoxing";
	public static final String brandId_CrossfitExtreme= "CrossfitExtreme";
	
	public static final String gymId_16034 = "16034";
	public static final String gymId_16035 = "16035";
	
	@LocalServerPort
	private int port;

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
	private List<Coach> coachs = new ArrayList<Coach>();

	@BeforeAll
	void arrange() {
		restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
		
		coachRepository.deleteAll();

		coach = CoachBuilder.build(brandId_FitnessBoxing, gymId_16034);
		coach.setActive(true);
		coachRepository.save(coach);
		
		coachIsDeleted = CoachBuilder.build(brandId_FitnessBoxing, gymId_16034);
		coachIsDeleted.setDeleted(true);
		coachIsDeleted = coachRepository.save(coachIsDeleted);

		for (int i = 0; i < 10; i++) {
			Coach item = CoachBuilder.build(brandId_FitnessBoxing, gymId_16034);
			item.setActive(true);
			coachRepository.save(item);
			coachs.add(item);
		}
		
		for (int i = 0; i < 4; i++) {
			Coach item = CoachBuilder.build(brandId_CrossfitExtreme, gymId_16035);
			item.setActive(true);
			coachRepository.save(item);
			coachs.add(item);
		}
		
		Coach item = CoachBuilder.build(brandId_CrossfitExtreme, gymId_16035);
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
		ResponseEntity<String> response = restTemplate.exchange(HttpUtils.createURL(URI.create(String.format(listURI, brandId_CrossfitExtreme, gymId_16035)), port, params),
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
		ResponseEntity<String> response = restTemplate.exchange(HttpUtils.createURL(URI.create(String.format(listURI, brandId_CrossfitExtreme, gymId_16035)), port, params),
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
		Assertions.assertEquals(5, page.getNumberOfElements(),
				String.format("Coach list first page number of elements invalid: %d", page.getNumberOfElements()));
		Assertions.assertEquals(5, page.getTotalElements(),
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
		ResponseEntity<String> response = restTemplate.exchange(HttpUtils.createURL(URI.create(String.format(listURI, brandId_CrossfitExtreme, gymId_16035)), port, params),
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
		PostCoachDto postCoach = modelMapper.map(CoachBuilder.build(brandId_FitnessBoxing, gymId_16034), PostCoachDto.class);
		
		HttpEntity<PostCoachDto> httpEntity = HttpUtils.createHttpEntity(role, user, postCoach);

		// Act
		ResponseEntity<CoachDto> response = restTemplate.exchange(HttpUtils.createURL(URI.create(String.format(postURI, brandId_FitnessBoxing, gymId_16034)), port, null),
				HttpMethod.POST, httpEntity, CoachDto.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));

		assertCoach(modelMapper.map(postCoach, CoachDto.class), response.getBody());
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testGetSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostCoachDto postCoach = modelMapper.map(CoachBuilder.build(brandId_FitnessBoxing , gymId_16034), PostCoachDto.class);

		HttpEntity<PostCoachDto> httpEntity = HttpUtils.createHttpEntity(role, user, postCoach);

		ResponseEntity<CoachDto> responsePost = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postURI, brandId_FitnessBoxing, gymId_16034)), port, null), HttpMethod.POST, httpEntity, CoachDto.class);

		Assertions.assertEquals(HttpStatus.CREATED, responsePost.getStatusCode(),
				String.format("Post error: %s", responsePost.getStatusCode()));

		// Act
		httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<CoachDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(getURI, brandId_FitnessBoxing, gymId_16034, responsePost.getBody().getId())), port, null),
				HttpMethod.GET, httpEntity, CoachDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));

		assertCoach(modelMapper.map(postCoach, CoachDto.class), response.getBody());
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPutSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Coach coachToUpdate = CoachBuilder.build(brandId_FitnessBoxing, gymId_16034);
		coachToUpdate.setActive(false);
		coachToUpdate.setActivatedOn(null);
		coachToUpdate.setDeactivatedOn(null);
		coachToUpdate = coachRepository.save(coachToUpdate);
		
		Coach updatedCoach = CoachBuilder.build(brandId_FitnessBoxing, gymId_16034);
		updatedCoach.setId(coachToUpdate.getId());
		updatedCoach.setActive(false);
		updatedCoach.setActivatedOn(null);
		updatedCoach.setDeactivatedOn(null);
		
		PutCoachDto putCoach = modelMapper.map(updatedCoach, PutCoachDto.class);
		putCoach.setId(coachToUpdate.getId());

		// Act
		HttpEntity<PutCoachDto> httpEntity = HttpUtils.createHttpEntity(role, user, putCoach);
		ResponseEntity<CoachDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, brandId_FitnessBoxing, gymId_16034, putCoach.getId())), port, null),
				HttpMethod.PUT, httpEntity, CoachDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put error: %s", response.getStatusCode()));

		assertCoach(modelMapper.map(updatedCoach, CoachDto.class), response.getBody());
	}
	
	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPutNullSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Coach coachToUpdate = CoachBuilder.build(brandId_FitnessBoxing, gymId_16034);
		coachToUpdate = coachRepository.save(coachToUpdate);
		
		Coach updatedCoach = CoachBuilder.build(brandId_FitnessBoxing, gymId_16034);
		
		PutCoachDto putCoach = modelMapper.map(updatedCoach, PutCoachDto.class);
		putCoach.setId(coachToUpdate.getId());
		putCoach.getPerson().setEmail(null);
		putCoach.getPerson().setFirstname(null);
		putCoach.getPerson().setLastname(null);
		putCoach.getPerson().setPhoneNumbers(null);
		
		// Act
		HttpEntity<PutCoachDto> httpEntity = HttpUtils.createHttpEntity(role, user, putCoach);
		ResponseEntity<CoachDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, brandId_FitnessBoxing, gymId_16034, putCoach.getId())), port, null),
				HttpMethod.PUT, httpEntity, CoachDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put null error: %s", response.getStatusCode()));
		
		updatedCoach.setId(coachToUpdate.getId());
		updatedCoach.getPerson().setEmail(null);
		updatedCoach.getPerson().setFirstname(null);
		updatedCoach.getPerson().setLastname(null);
		updatedCoach.getPerson().setPhoneNumbers(null);
		
 		assertCoach(modelMapper.map(updatedCoach, CoachDto.class), response.getBody());
	}
	
	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testActivateSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Coach coachToActivate = CoachBuilder.build(brandId_FitnessBoxing, gymId_16034);
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
				HttpUtils.createURL(URI.create(String.format(postActivateURI, brandId_FitnessBoxing, gymId_16034, coachToActivate.getId())), port, null),
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
				HttpUtils.createURL(URI.create(String.format(postActivateURI, brandId_FitnessBoxing, gymId_16034, faker.code().isbn10())), port, null),
				HttpMethod.POST, httpEntity, CoachDto.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Coach activation error: %s", response.getStatusCode()));
	}
	
	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testDeactivateSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Coach coachToDeactivate = CoachBuilder.build(brandId_FitnessBoxing, gymId_16034);
		coachToDeactivate.setGymId(gymId_16034);
		coachToDeactivate.setActive(true);
		coachToDeactivate.setActivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		coachToDeactivate = coachRepository.save(coachToDeactivate);
		coachToDeactivate.setActive(false);
		coachToDeactivate.setDeactivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		
		// Act
		HttpEntity<PutCoachDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<CoachDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postDeactivateURI, brandId_FitnessBoxing, gymId_16034, coachToDeactivate.getId())), port, null),
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
				HttpUtils.createURL(URI.create(String.format(postDeactivateURI, brandId_FitnessBoxing, gymId_16034, faker.code().ean13())), port, null),
				HttpMethod.POST, httpEntity, CoachDto.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Coach activation error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPatchSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Coach coachToPatch = CoachBuilder.build(brandId_FitnessBoxing, gymId_16034);
		coachToPatch.setActive(true);
		coachToPatch = coachRepository.save(coachToPatch);

		PatchCoachDto patchCoach = modelMapper.map(coachToPatch, PatchCoachDto.class);
		patchCoach.setId(coachToPatch.getId());
		patchCoach.getPerson().setEmail(null);
		patchCoach.getPerson().setFirstname(null);
		patchCoach.getPerson().getAddress().setStreetName(null);
		patchCoach.getPerson().setLastname(faker.name().lastName());
		
		// Act
		HttpEntity<PatchCoachDto> httpEntity = HttpUtils.createHttpEntity(role, user, patchCoach);
		ResponseEntity<CoachDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(patchURI, brandId_FitnessBoxing, gymId_16034, patchCoach.getId())), port, null),
				HttpMethod.PATCH, httpEntity, CoachDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));

		coachToPatch.getPerson().setLastname(patchCoach.getPerson().getLastname());
		
 		assertCoach(modelMapper.map(coachToPatch, CoachDto.class), response.getBody());
	}

	public static final void assertCoach(CoachDto expected, CoachDto result) {
		Assertions.assertEquals(expected.getId(), result.getId());
		Assertions.assertEquals(expected.getBrandId(), result.getBrandId());
		Assertions.assertEquals(expected.getGymId(), result.getGymId());
		Assertions.assertEquals(expected.getPerson().getFirstname(), result.getPerson().getFirstname());
		Assertions.assertEquals(expected.getPerson().getLastname(), result.getPerson().getLastname());
		Assertions.assertEquals(expected.getPerson().getEmail(), result.getPerson().getEmail());
		Assertions.assertEquals(expected.getPerson().getDateOfBirth(), result.getPerson().getDateOfBirth());
		Assertions.assertEquals(expected.getPerson().getPhotoUri(), result.getPerson().getPhotoUri());
		Assertions.assertEquals(expected.getPerson().getCommunicationLanguage(), result.getPerson().getCommunicationLanguage());
		Assertions.assertEquals(expected.getPerson().getNote(), result.getPerson().getNote());
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
