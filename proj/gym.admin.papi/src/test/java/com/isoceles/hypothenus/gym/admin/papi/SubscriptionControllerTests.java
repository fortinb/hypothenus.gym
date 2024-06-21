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

import com.isoceles.hypothenus.gym.admin.papi.dto.SubscriptionDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.LocalizedStringDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.patch.PatchSubscriptionDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.post.PostSubscriptionDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.put.PutSubscriptionDto;
import com.isoceles.hypothenus.gym.domain.model.aggregate.Subscription;
import com.isoceles.hypothenus.gym.domain.repository.SubscriptionRepository;
import com.isoceles.hypothenus.tests.http.HttpUtils;
import com.isoceles.hypothenus.tests.model.SubscriptionBuilder;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
class SubscriptionControllerTests {

	public static final String listURI = "/v1/admin/gyms/%s/subscriptions";
	public static final String postURI = "/v1/admin/gyms/%s/subscriptions";
	public static final String getURI = "/v1/admin/gyms/%s/subscriptions/%s";
	public static final String putURI = "/v1/admin/gyms/%s/subscriptions/%s";
	public static final String postActivateURI = "/v1/admin/gyms/%s/subscriptions/%s/activate";
	public static final String postDeactivateURI = "/v1/admin/gyms/%s/subscriptions/%s/deactivate";
	public static final String patchURI = "/v1/admin/gyms/%s/subscriptions/%s";
	public static final String pageNumber = "page";
	public static final String pageSize = "pageSize";
	public static final String isActive = "isActive";

	public static final String gymId_16034 = "16034";
	public static final String gymId_16035 = "16035";

	@LocalServerPort
	private int port;

	@Autowired
	SubscriptionRepository subscriptionRepository;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	ModelMapper modelMapper;

	private Faker faker = new Faker();

	private TestRestTemplate restTemplate = new TestRestTemplate();

	private Subscription subscription;
	private Subscription subscriptionIsDeleted;
	private List<Subscription> subscriptions = new ArrayList<Subscription>();

	@BeforeAll
	void arrange() {
		restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());

		subscriptionRepository.deleteAll();

		subscription = SubscriptionBuilder.build(gymId_16034);
		subscriptionRepository.save(subscription);

		subscriptionIsDeleted = SubscriptionBuilder.build(gymId_16034);
		subscriptionIsDeleted.setDeleted(true);
		subscriptionIsDeleted = subscriptionRepository.save(subscriptionIsDeleted);

		for (int i = 0; i < 10; i++) {
			Subscription item = SubscriptionBuilder.build(gymId_16034);
			subscriptionRepository.save(item);
			subscriptions.add(item);
		}

		for (int i = 0; i < 4; i++) {
			Subscription item = SubscriptionBuilder.build(gymId_16035);
			subscriptionRepository.save(item);
			subscriptions.add(item);
		}

		Subscription item = SubscriptionBuilder.build(gymId_16035);
		item.setActive(false);
		subscriptionRepository.save(item);
	}

	@AfterAll
	void cleanup() {
		// Cleanup
		// subscriptionRepository.deleteAll();
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testListActiveSuccess(String role, String user)
			throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		HttpEntity<String> httpEntity = HttpUtils.createHttpEntity(role, user, null);

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "0");
		params.add(pageSize, "5");
		params.add(isActive, "true");

		// Act
		ResponseEntity<String> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(listURI, gymId_16035)), port, params), HttpMethod.GET,
				httpEntity, new ParameterizedTypeReference<String>() {
				});

		// Assert
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		Page<SubscriptionDto> page = objectMapper.readValue(response.getBody(), new TypeReference<Page<SubscriptionDto>>() {
		});

		// Assert
		Assertions.assertEquals(0, page.getPageable().getPageNumber(),
				String.format("Subscription list first page number invalid: %d", page.getPageable().getPageNumber()));
		Assertions.assertEquals(4, page.getNumberOfElements(),
				String.format("Subscription list first page number of elements invalid: %d", page.getNumberOfElements()));
		Assertions.assertEquals(4, page.getTotalElements(),
				String.format("Subscription total number of elements invalid: %d", page.getTotalElements()));

		page.get().forEach(subscription -> Assertions.assertTrue(subscription.isActive()));
		page.get().forEach(subscription -> Assertions.assertTrue(subscription.isDeleted() == false));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testListFirstPageSuccess(String role, String user)
			throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		HttpEntity<String> httpEntity = HttpUtils.createHttpEntity(role, user, null);

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "0");
		params.add(pageSize, "5");
		params.add(isActive, "false");

		// Act
		ResponseEntity<String> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(listURI, gymId_16035)), port, params), HttpMethod.GET,
				httpEntity, new ParameterizedTypeReference<String>() {
				});

		// Assert
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		Page<SubscriptionDto> page = objectMapper.readValue(response.getBody(), new TypeReference<Page<SubscriptionDto>>() {
		});

		// Assert
		Assertions.assertEquals(0, page.getPageable().getPageNumber(),
				String.format("Subscription list first page number invalid: %d", page.getPageable().getPageNumber()));
		Assertions.assertEquals(5, page.getNumberOfElements(),
				String.format("Subscription list first page number of elements invalid: %d", page.getNumberOfElements()));
		Assertions.assertEquals(5, page.getTotalElements(),
				String.format("Subscription total number of elements invalid: %d", page.getTotalElements()));

		page.get().forEach(subscription -> Assertions.assertTrue(subscription.isDeleted() == false));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testListSecondPageSuccess(String role, String user)
			throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		HttpEntity<String> httpEntity = HttpUtils.createHttpEntity(role, user, "");

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "1");
		params.add(pageSize, "2");

		// Act
		ResponseEntity<String> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(listURI, gymId_16035)), port, params), HttpMethod.GET,
				httpEntity, new ParameterizedTypeReference<String>() {
				});

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		Page<SubscriptionDto> page = objectMapper.readValue(response.getBody(), new TypeReference<Page<SubscriptionDto>>() {
		});

		// Assert
		Assertions.assertEquals(1, page.getPageable().getPageNumber(),
				String.format("Subscription list second page number invalid: %d", page.getPageable().getPageNumber()));
		Assertions.assertEquals(2, page.getNumberOfElements(),
				String.format("Subscription list second page number of elements invalid: %d", page.getNumberOfElements()));
		page.get().forEach(subscription -> Assertions.assertTrue(subscription.isDeleted() == false));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPostSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostSubscriptionDto postSubscription = modelMapper.map(SubscriptionBuilder.build(gymId_16034), PostSubscriptionDto.class);

		HttpEntity<PostSubscriptionDto> httpEntity = HttpUtils.createHttpEntity(role, user, postSubscription);

		// Act
		ResponseEntity<SubscriptionDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postURI, gymId_16034)), port, null), HttpMethod.POST,
				httpEntity, SubscriptionDto.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));

		assertSubscription(modelMapper.map(postSubscription, SubscriptionDto.class), response.getBody());
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testGetSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostSubscriptionDto postSubscription = modelMapper.map(SubscriptionBuilder.build(gymId_16034), PostSubscriptionDto.class);

		HttpEntity<PostSubscriptionDto> httpEntity = HttpUtils.createHttpEntity(role, user, postSubscription);

		ResponseEntity<SubscriptionDto> responsePost = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postURI, gymId_16034)), port, null), HttpMethod.POST,
				httpEntity, SubscriptionDto.class);

		Assertions.assertEquals(HttpStatus.CREATED, responsePost.getStatusCode(),
				String.format("Post error: %s", responsePost.getStatusCode()));

		// Act
		httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<SubscriptionDto> response = restTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(getURI, gymId_16034, responsePost.getBody().getId())), port, null),
				HttpMethod.GET, httpEntity, SubscriptionDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));

		assertSubscription(modelMapper.map(postSubscription, SubscriptionDto.class), response.getBody());
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPutSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Subscription subscriptionToUpdate = SubscriptionBuilder.build(gymId_16034);
		subscriptionToUpdate.setActive(false);
		subscriptionToUpdate.setActivatedOn(null);
		subscriptionToUpdate.setDeactivatedOn(null);
		subscriptionToUpdate = subscriptionRepository.save(subscriptionToUpdate);

		Subscription updatedSubscription = SubscriptionBuilder.build(gymId_16034);
		updatedSubscription.setId(subscriptionToUpdate.getId());
		updatedSubscription.setActive(false);
		updatedSubscription.setActivatedOn(null);
		updatedSubscription.setDeactivatedOn(null);

		PutSubscriptionDto putSubscription = modelMapper.map(updatedSubscription, PutSubscriptionDto.class);
		putSubscription.setId(subscriptionToUpdate.getId());

		// Act
		HttpEntity<PutSubscriptionDto> httpEntity = HttpUtils.createHttpEntity(role, user, putSubscription);
		ResponseEntity<SubscriptionDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, gymId_16034, putSubscription.getId())), port, null),
				HttpMethod.PUT, httpEntity, SubscriptionDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put error: %s", response.getStatusCode()));

		assertSubscription(modelMapper.map(updatedSubscription, SubscriptionDto.class), response.getBody());
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPutNullSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Subscription subscriptionToUpdate = SubscriptionBuilder.build(gymId_16034);
		subscriptionToUpdate.setActive(false);
		subscriptionToUpdate.setActivatedOn(null);
		subscriptionToUpdate.setDeactivatedOn(null);
		subscriptionToUpdate = subscriptionRepository.save(subscriptionToUpdate);

		Subscription updatedSubscription = SubscriptionBuilder.build(gymId_16034);
		updatedSubscription.setId(subscriptionToUpdate.getId());
		updatedSubscription.setActive(false);
		updatedSubscription.setActivatedOn(null);
		updatedSubscription.setDeactivatedOn(null);
		
		PutSubscriptionDto putSubscription = modelMapper.map(updatedSubscription, PutSubscriptionDto.class);
		putSubscription.setId(subscriptionToUpdate.getId());
		putSubscription.setCode(null);
		putSubscription.setDescription(null);
		putSubscription.setName(null);

		// Act
		HttpEntity<PutSubscriptionDto> httpEntity = HttpUtils.createHttpEntity(role, user, putSubscription);
		ResponseEntity<SubscriptionDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, gymId_16034, putSubscription.getId())), port, null),
				HttpMethod.PUT, httpEntity, SubscriptionDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put null error: %s", response.getStatusCode()));

		updatedSubscription.setCode(null);
		updatedSubscription.setDescription(null);
		updatedSubscription.setName(null);

		assertSubscription(modelMapper.map(updatedSubscription, SubscriptionDto.class), response.getBody());
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testActivateSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Subscription subscriptionToActivate = SubscriptionBuilder.build(gymId_16034);
		subscriptionToActivate.setActive(false);
		subscriptionToActivate.setActivatedOn(null);
		subscriptionToActivate.setDeactivatedOn(null);
		subscriptionToActivate = subscriptionRepository.save(subscriptionToActivate);

		subscriptionToActivate.setActive(true);
		subscriptionToActivate.setActivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		subscriptionToActivate.setDeactivatedOn(null);

		// Act
		HttpEntity<PutSubscriptionDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<SubscriptionDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postActivateURI, gymId_16034, subscriptionToActivate.getId())),
						port, null),
				HttpMethod.POST, httpEntity, SubscriptionDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Subscription activation error: %s", response.getStatusCode()));

		assertSubscription(modelMapper.map(subscriptionToActivate, SubscriptionDto.class), response.getBody());
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testActivateFailure(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange

		// Act
		HttpEntity<PutSubscriptionDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<Object> response = restTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(postActivateURI, gymId_16034, faker.code().isbn10())), port, null),
				HttpMethod.POST, httpEntity, Object.class);
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																											
		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Subscription activation error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testDeactivateSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Subscription subscriptionToDeactivate = SubscriptionBuilder.build(gymId_16034);
		subscriptionToDeactivate.setActive(true);
		subscriptionToDeactivate.setActivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		subscriptionToDeactivate = subscriptionRepository.save(subscriptionToDeactivate);

		subscriptionToDeactivate.setActive(false);
		subscriptionToDeactivate.setDeactivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));

		// Act
		HttpEntity<PutSubscriptionDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<SubscriptionDto> response = restTemplate.exchange(HttpUtils.createURL(
				URI.create(String.format(postDeactivateURI, gymId_16034, subscriptionToDeactivate.getId())), port, null),
				HttpMethod.POST, httpEntity, SubscriptionDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Subscription deactivation error: %s", response.getStatusCode()));

		assertSubscription(modelMapper.map(subscriptionToDeactivate, SubscriptionDto.class), response.getBody());
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testDeactivateFailure(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange

		// Act
		HttpEntity<PutSubscriptionDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<Object> response = restTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(postDeactivateURI, gymId_16034, faker.code().ean13())), port, null),
				HttpMethod.POST, httpEntity, Object.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Subscription activation error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPatchSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Subscription subscriptionToPatch = SubscriptionBuilder.build(gymId_16034);
		subscriptionToPatch = subscriptionRepository.save(subscriptionToPatch);

		PatchSubscriptionDto patchSubscription = modelMapper.map(subscriptionToPatch, PatchSubscriptionDto.class);
		patchSubscription.setId(subscriptionToPatch.getId());
		patchSubscription.setDescription(null);
		patchSubscription.setName(null);

		// Act
		HttpEntity<PatchSubscriptionDto> httpEntity = HttpUtils.createHttpEntity(role, user, patchSubscription);
		ResponseEntity<SubscriptionDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(patchURI, gymId_16034, patchSubscription.getId())), port, null),
				HttpMethod.PATCH, httpEntity, SubscriptionDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));

		subscriptionToPatch.setCode(patchSubscription.getCode());

		assertSubscription(modelMapper.map(subscriptionToPatch, SubscriptionDto.class), response.getBody());
	}

	public static final void assertSubscription(SubscriptionDto expected, SubscriptionDto result) {
		Assertions.assertEquals(expected.getId(), result.getId());
		Assertions.assertEquals(expected.getCode(), result.getCode());
		Assertions.assertEquals(expected.getDurationInMonths(), result.getDurationInMonths());
		Assertions.assertEquals(expected.getMaxNumberOfClassesPerPeriod(), result.getMaxNumberOfClassesPerPeriod());
		Assertions.assertEquals(expected.getPrice(), result.getPrice());
		Assertions.assertEquals(expected.getPeriod(), result.getPeriod());
		Assertions.assertEquals(expected.getPaymentOption(), result.getPaymentOption());

		Assertions.assertEquals(expected.isActive(), result.isActive());

		if (expected.getActivatedOn() != null) {
			Assertions.assertNotNull(result.getActivatedOn());
			Assertions.assertTrue(expected.getActivatedOn().truncatedTo(ChronoUnit.DAYS)
					.equals(result.getActivatedOn().truncatedTo(ChronoUnit.DAYS)));
		}
		if (expected.getActivatedOn() == null) {
			Assertions.assertNull(result.getActivatedOn());
		}

		if (expected.getDeactivatedOn() != null) {
			Assertions.assertNotNull(result.getDeactivatedOn());
			Assertions.assertTrue(expected.getDeactivatedOn().truncatedTo(ChronoUnit.DAYS)
					.equals(result.getDeactivatedOn().truncatedTo(ChronoUnit.DAYS)));
		}
		if (expected.getDeactivatedOn() == null) {
			Assertions.assertNull(result.getDeactivatedOn());
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
