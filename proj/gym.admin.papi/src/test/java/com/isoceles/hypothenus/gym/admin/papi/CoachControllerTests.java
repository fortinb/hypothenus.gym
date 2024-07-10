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

	public static final String listURI = "/v1/admin/gyms/%s/coachs";
	public static final String postURI = "/v1/admin/gyms/%s/coachs";
	public static final String getURI = "/v1/admin/gyms/%s/coachs/%s";
	public static final String putURI = "/v1/admin/gyms/%s/coachs/%s";
	public static final String postActivateURI = "/v1/admin/gyms/%s/coachs/%s/activate";
	public static final String postDeactivateURI = "/v1/admin/gyms/%s/coachs/%s/deactivate";
	public static final String patchURI = "/v1/admin/gyms/%s/coachs/%s";
	public static final String pageNumber = "page";
	public static final String pageSize = "pageSize";
	public static final String isActive = "isActive";
	
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

		coach = CoachBuilder.build(gymId_16034);
		coachRepository.save(coach);
		
		coachIsDeleted = CoachBuilder.build(gymId_16034);
		coachIsDeleted.setDeleted(true);
		coachIsDeleted = coachRepository.save(coachIsDeleted);

		for (int i = 0; i < 10; i++) {
			Coach item = CoachBuilder.build(gymId_16034);
			coachRepository.save(item);
			coachs.add(item);
		}
		
		for (int i = 0; i < 4; i++) {
			Coach item = CoachBuilder.build(gymId_16035);
			coachRepository.save(item);
			coachs.add(item);
		}
		
		Coach item = CoachBuilder.build(gymId_16035);
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
		params.add(isActive, "true");

		// Act
		ResponseEntity<String> response = restTemplate.exchange(HttpUtils.createURL(URI.create(String.format(listURI, gymId_16035)), port, params),
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
		params.add(isActive, "false");
		
		// Act
		ResponseEntity<String> response = restTemplate.exchange(HttpUtils.createURL(URI.create(String.format(listURI, gymId_16035)), port, params),
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
		ResponseEntity<String> response = restTemplate.exchange(HttpUtils.createURL(URI.create(String.format(listURI, gymId_16035)), port, params),
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
		page.get().forEach(coach ->Assertions.assertTrue(coach.isDeleted() == false));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPostSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostCoachDto postCoach = modelMapper.map(CoachBuilder.build(gymId_16034), PostCoachDto.class);
		
		HttpEntity<PostCoachDto> httpEntity = HttpUtils.createHttpEntity(role, user, postCoach);

		// Act
		ResponseEntity<CoachDto> response = restTemplate.exchange(HttpUtils.createURL(URI.create(String.format(postURI, gymId_16034)), port, null),
				HttpMethod.POST, httpEntity, CoachDto.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));

		assertCoach(modelMapper.map(postCoach, CoachDto.class), response.getBody());
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testGetSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostCoachDto postCoach = modelMapper.map(CoachBuilder.build(gymId_16034), PostCoachDto.class);

		HttpEntity<PostCoachDto> httpEntity = HttpUtils.createHttpEntity(role, user, postCoach);

		ResponseEntity<CoachDto> responsePost = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postURI, gymId_16034)), port, null), HttpMethod.POST, httpEntity, CoachDto.class);

		Assertions.assertEquals(HttpStatus.CREATED, responsePost.getStatusCode(),
				String.format("Post error: %s", responsePost.getStatusCode()));

		// Act
		httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<CoachDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(getURI, gymId_16034, responsePost.getBody().getId())), port, null),
				HttpMethod.GET, httpEntity, CoachDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));

		assertCoach(modelMapper.map(postCoach, CoachDto.class), response.getBody());
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPutSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Coach coachToUpdate = CoachBuilder.build(gymId_16034);
		coachToUpdate.setActive(false);
		coachToUpdate.setActivatedOn(null);
		coachToUpdate.setDeactivatedOn(null);
		coachToUpdate = coachRepository.save(coachToUpdate);
		
		Coach updatedCoach = CoachBuilder.build(gymId_16034);
		updatedCoach.setId(coachToUpdate.getId());
		updatedCoach.setActive(false);
		updatedCoach.setActivatedOn(null);
		updatedCoach.setDeactivatedOn(null);
		
		PutCoachDto putCoach = modelMapper.map(updatedCoach, PutCoachDto.class);
		putCoach.setId(coachToUpdate.getId());

		// Act
		HttpEntity<PutCoachDto> httpEntity = HttpUtils.createHttpEntity(role, user, putCoach);
		ResponseEntity<CoachDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, gymId_16034, putCoach.getId())), port, null),
				HttpMethod.PUT, httpEntity, CoachDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put error: %s", response.getStatusCode()));

		assertCoach(modelMapper.map(updatedCoach, CoachDto.class), response.getBody());
	}
	
	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPutNullSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Coach coachToUpdate = CoachBuilder.build(gymId_16034);
		coachToUpdate.setActive(false);
		coachToUpdate.setActivatedOn(null);
		coachToUpdate.setDeactivatedOn(null);
		coachToUpdate = coachRepository.save(coachToUpdate);
		
		Coach updatedCoach = CoachBuilder.build(gymId_16034);

		PutCoachDto putCoach = modelMapper.map(updatedCoach, PutCoachDto.class);
		putCoach.setId(coachToUpdate.getId());
		putCoach.setEmail(null);
		putCoach.setLanguage(null);
		putCoach.setFirstname(null);
		putCoach.setLastname(null);
		putCoach.setPhoneNumbers(null);
		
		// Act
		HttpEntity<PutCoachDto> httpEntity = HttpUtils.createHttpEntity(role, user, putCoach);
		ResponseEntity<CoachDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, gymId_16034, putCoach.getId())), port, null),
				HttpMethod.PUT, httpEntity, CoachDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put null error: %s", response.getStatusCode()));
		
		coachToUpdate.setEmail(null);
		coachToUpdate.setLanguage(null);
		coachToUpdate.setFirstname(null);
		coachToUpdate.setLastname(null);
		coachToUpdate.setPhoneNumbers(null);
		
 		assertCoach(modelMapper.map(coachToUpdate, CoachDto.class), response.getBody());
	}
	
	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testActivateSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Coach coachToActivate = CoachBuilder.build(gymId_16034);
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
				HttpUtils.createURL(URI.create(String.format(postActivateURI, gymId_16034, coachToActivate.getId())), port, null),
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
				HttpUtils.createURL(URI.create(String.format(postActivateURI, gymId_16034, faker.code().isbn10())), port, null),
				HttpMethod.POST, httpEntity, CoachDto.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Coach activation error: %s", response.getStatusCode()));
	}
	
	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testDeactivateSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Coach coachToDeactivate = CoachBuilder.build(gymId_16034);
		coachToDeactivate.setGymId(gymId_16034);
		coachToDeactivate.setActive(true);
		coachToDeactivate.setActivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		coachToDeactivate = coachRepository.save(coachToDeactivate);
		coachToDeactivate.setActive(false);
		coachToDeactivate.setDeactivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		
		// Act
		HttpEntity<PutCoachDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<CoachDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postDeactivateURI, gymId_16034, coachToDeactivate.getId())), port, null),
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
				HttpUtils.createURL(URI.create(String.format(postDeactivateURI, gymId_16034, faker.code().ean13())), port, null),
				HttpMethod.POST, httpEntity, CoachDto.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Coach activation error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPatchSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Coach coachToPatch = CoachBuilder.build(gymId_16034);
		coachToPatch = coachRepository.save(coachToPatch);

		PatchCoachDto patchCoach = modelMapper.map(coachToPatch, PatchCoachDto.class);
		patchCoach.setId(coachToPatch.getId());
		patchCoach.setEmail(null);
		patchCoach.setLanguage(null);
		patchCoach.setFirstname(null);
		patchCoach.setLastname(faker.name().lastName());
		patchCoach.setLanguage("en-US");
		
		// Act
		HttpEntity<PatchCoachDto> httpEntity = HttpUtils.createHttpEntity(role, user, patchCoach);
		ResponseEntity<CoachDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(patchURI, gymId_16034, patchCoach.getId())), port, null),
				HttpMethod.PATCH, httpEntity, CoachDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));

		coachToPatch.setLastname(patchCoach.getLastname());
		coachToPatch.setLanguage(patchCoach.getLanguage());
		
 		assertCoach(modelMapper.map(coachToPatch, CoachDto.class), response.getBody());
	}

	public static final void assertCoach(CoachDto expected, CoachDto result) {
		Assertions.assertEquals(expected.getId(), result.getId());
		Assertions.assertEquals(expected.getFirstname(), result.getFirstname());
		Assertions.assertEquals(expected.getLastname(), result.getLastname());
		Assertions.assertEquals(expected.getEmail(), result.getEmail());
		Assertions.assertEquals(expected.getLanguage(), result.getLanguage());
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
		
		if (expected.getPhoneNumbers() != null) {
			Assertions.assertNotNull(result.getPhoneNumbers());

			Assertions.assertEquals(expected.getPhoneNumbers().size(), result.getPhoneNumbers().size());
			expected.getPhoneNumbers().forEach(phone -> {
				Optional<PhoneNumberDto> previous = result.getPhoneNumbers().stream()
						.filter(item -> item.getType().equals(phone.getType())).findFirst();
				Assertions.assertTrue(previous.isPresent());
			});
		}

		if (expected.getPhoneNumbers() == null) {
			Assertions.assertNull(result.getPhoneNumbers());
		}
	}
}
