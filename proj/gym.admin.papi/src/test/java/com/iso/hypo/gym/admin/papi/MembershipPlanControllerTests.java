package com.iso.hypo.gym.admin.papi;

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
import com.iso.hypo.gym.admin.papi.dto.LocalizedStringDto;
import com.iso.hypo.gym.admin.papi.dto.model.MembershipPlanDto;
import com.iso.hypo.gym.admin.papi.dto.patch.PatchMembershipPlanDto;
import com.iso.hypo.gym.admin.papi.dto.post.PostMembershipPlanDto;
import com.iso.hypo.gym.admin.papi.dto.pricing.OneTimeFeeDto;
import com.iso.hypo.gym.admin.papi.dto.put.PutMembershipPlanDto;
import com.iso.hypo.gym.domain.model.aggregate.MembershipPlan;
import com.iso.hypo.gym.domain.repository.MembershipPlanRepository;
import com.iso.hypo.tests.http.HttpUtils;
import com.iso.hypo.tests.model.MembershipPlanBuilder;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
class MembershipPlanControllerTests {

	public static final String listURI = "/v1/brands/%s/membership/plans";
	public static final String postURI = "/v1/brands/%s/membership/plans";
	public static final String getURI = "/v1/brands/%s/membership/plans/%s";
	public static final String putURI = "/v1/brands/%s/membership/plans/%s";
	public static final String postActivateURI = "/v1/brands/%s/membership/plans/%s/activate";
	public static final String postDeactivateURI = "/v1/brands/%s/membership/plans/%s/deactivate";
	public static final String patchURI = "/v1/brands/%s/membership/plans/%s";
	public static final String pageNumber = "page";
	public static final String pageSize = "pageSize";
	public static final String includeInactive = "includeInactive";

	public static final String brandId_FitnessBoxing = "FitnessBoxing";
	public static final String brandId_CrossfitExtreme= "CrossfitExtreme";
	
	@LocalServerPort
	private int port;

	@Autowired
	MembershipPlanRepository membershipPlanRepository;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	ModelMapper modelMapper;

	private Faker faker = new Faker();

	private TestRestTemplate restTemplate = new TestRestTemplate();

	private MembershipPlan membershipPlan;
	private MembershipPlan membershipPlanIsDeleted;
	private List<MembershipPlan> membershipPlans = new ArrayList<MembershipPlan>();

	@BeforeAll
	void arrange() {
		restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());

		membershipPlanRepository.deleteAll();

		membershipPlan = MembershipPlanBuilder.build(brandId_FitnessBoxing);
		membershipPlanRepository.save(membershipPlan);

		membershipPlanIsDeleted = MembershipPlanBuilder.build(brandId_FitnessBoxing);
		membershipPlanIsDeleted.setDeleted(true);
		membershipPlanIsDeleted = membershipPlanRepository.save(membershipPlanIsDeleted);

		for (int i = 0; i < 10; i++) {
			MembershipPlan item = MembershipPlanBuilder.build(brandId_FitnessBoxing);
			membershipPlanRepository.save(item);
			membershipPlans.add(item);
		}

		for (int i = 0; i < 4; i++) {
			MembershipPlan item = MembershipPlanBuilder.build(brandId_CrossfitExtreme);
			membershipPlanRepository.save(item);
			membershipPlans.add(item);
		}

		MembershipPlan item = MembershipPlanBuilder.build(brandId_CrossfitExtreme);
		item.setActive(false);
		membershipPlanRepository.save(item);
	}

	@AfterAll
	void cleanup() {
		// Cleanup
		// membershipPlanRepository.deleteAll();
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
		params.add(includeInactive, "false");

		// Act
		ResponseEntity<String> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(listURI, brandId_CrossfitExtreme)), port, params), HttpMethod.GET,
				httpEntity, new ParameterizedTypeReference<String>() {
				});

		// Assert
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		Page<MembershipPlanDto> page = objectMapper.readValue(response.getBody(), new TypeReference<Page<MembershipPlanDto>>() {
		});

		// Assert
		Assertions.assertEquals(0, page.getPageable().getPageNumber(),
				String.format("Membership Plan list first page number invalid: %d", page.getPageable().getPageNumber()));
		Assertions.assertEquals(4, page.getNumberOfElements(),
				String.format("Membership Plan list first page number of elements invalid: %d", page.getNumberOfElements()));
		Assertions.assertEquals(4, page.getTotalElements(),
				String.format("Membership Plan total number of elements invalid: %d", page.getTotalElements()));

		page.get().forEach(membershipPlan -> Assertions.assertTrue(membershipPlan.isActive()));
		page.get().forEach(membershipPlan -> Assertions.assertTrue(membershipPlan.isDeleted() == false));
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
		params.add(includeInactive, "true");

		// Act
		ResponseEntity<String> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(listURI, brandId_CrossfitExtreme)), port, params), HttpMethod.GET,
				httpEntity, new ParameterizedTypeReference<String>() {
				});

		// Assert
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		Page<MembershipPlanDto> page = objectMapper.readValue(response.getBody(), new TypeReference<Page<MembershipPlanDto>>() {
		});

		// Assert
		Assertions.assertEquals(0, page.getPageable().getPageNumber(),
				String.format("Membership Plan list first page number invalid: %d", page.getPageable().getPageNumber()));
		Assertions.assertEquals(5, page.getNumberOfElements(),
				String.format("Membership Plan list first page number of elements invalid: %d", page.getNumberOfElements()));
		Assertions.assertEquals(5, page.getTotalElements(),
				String.format("Membership Plan total number of elements invalid: %d", page.getTotalElements()));

		page.get().forEach(membershipPlan -> Assertions.assertTrue(membershipPlan.isDeleted() == false));
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
				HttpUtils.createURL(URI.create(String.format(listURI, brandId_CrossfitExtreme)), port, params), HttpMethod.GET,
				httpEntity, new ParameterizedTypeReference<String>() {
				});

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		Page<MembershipPlanDto> page = objectMapper.readValue(response.getBody(), new TypeReference<Page<MembershipPlanDto>>() {
		});

		// Assert
		Assertions.assertEquals(1, page.getPageable().getPageNumber(),
				String.format("Membership Plan list second page number invalid: %d", page.getPageable().getPageNumber()));
		Assertions.assertEquals(2, page.getNumberOfElements(),
				String.format("Membership Plan list second page number of elements invalid: %d", page.getNumberOfElements()));
		
		page.get().forEach(membershipPlan -> Assertions.assertTrue(membershipPlan.isActive()));
		page.get().forEach(membershipPlan -> Assertions.assertTrue(membershipPlan.isDeleted() == false));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPostSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostMembershipPlanDto postMembershipPlan = modelMapper.map(MembershipPlanBuilder.build(brandId_FitnessBoxing), PostMembershipPlanDto.class);

		HttpEntity<PostMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, postMembershipPlan);

		// Act
		ResponseEntity<MembershipPlanDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postURI, brandId_FitnessBoxing)), port, null), HttpMethod.POST,
				httpEntity, MembershipPlanDto.class);

				String.format("Post error: %s", response.getStatusCode());

		assertMembershipPlan(modelMapper.map(postMembershipPlan, MembershipPlanDto.class), response.getBody());
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testGetSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostMembershipPlanDto postMembershipPlan = modelMapper.map(MembershipPlanBuilder.build(brandId_FitnessBoxing), PostMembershipPlanDto.class);

		HttpEntity<PostMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, postMembershipPlan);

		ResponseEntity<MembershipPlanDto> responsePost = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postURI, brandId_FitnessBoxing)), port, null), HttpMethod.POST,
				httpEntity, MembershipPlanDto.class);

		Assertions.assertEquals(HttpStatus.CREATED, responsePost.getStatusCode(),
				String.format("Post error: %s", responsePost.getStatusCode()));

		// Act
		httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<MembershipPlanDto> response = restTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(getURI, brandId_FitnessBoxing, responsePost.getBody().getId())), port, null),
				HttpMethod.GET, httpEntity, MembershipPlanDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));

		assertMembershipPlan(modelMapper.map(postMembershipPlan, MembershipPlanDto.class), response.getBody());
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPutSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		MembershipPlan membershipPlanToUpdate = MembershipPlanBuilder.build(brandId_FitnessBoxing);
		membershipPlanToUpdate.setActive(false);
		membershipPlanToUpdate.setActivatedOn(null);
		membershipPlanToUpdate.setDeactivatedOn(null);
		membershipPlanToUpdate = membershipPlanRepository.save(membershipPlanToUpdate);

		MembershipPlan updatedMembershipPlan = MembershipPlanBuilder.build(brandId_FitnessBoxing);
		updatedMembershipPlan.setId(membershipPlanToUpdate.getId());
		updatedMembershipPlan.setActive(false);
		updatedMembershipPlan.setActivatedOn(null);
		updatedMembershipPlan.setDeactivatedOn(null);

		PutMembershipPlanDto putMembershipPlan = modelMapper.map(updatedMembershipPlan, PutMembershipPlanDto.class);
		putMembershipPlan.setId(membershipPlanToUpdate.getId());

		// Act
		HttpEntity<PutMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, putMembershipPlan);
		ResponseEntity<MembershipPlanDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, brandId_FitnessBoxing, putMembershipPlan.getId())), port, null),
				HttpMethod.PUT, httpEntity, MembershipPlanDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put error: %s", response.getStatusCode()));

		assertMembershipPlan(modelMapper.map(updatedMembershipPlan, MembershipPlanDto.class), response.getBody());
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPutNullSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		MembershipPlan membershipPlanToUpdate = MembershipPlanBuilder.build(brandId_FitnessBoxing);
		membershipPlanToUpdate.setActive(false);
		membershipPlanToUpdate.setActivatedOn(null);
		membershipPlanToUpdate.setDeactivatedOn(null);
		membershipPlanToUpdate = membershipPlanRepository.save(membershipPlanToUpdate);

		MembershipPlan updatedMembershipPlan = MembershipPlanBuilder.build(brandId_FitnessBoxing);
		updatedMembershipPlan.setId(membershipPlanToUpdate.getId());
		updatedMembershipPlan.setCode(membershipPlanToUpdate.getCode());
		updatedMembershipPlan.setActive(false);
		updatedMembershipPlan.setActivatedOn(null);
		updatedMembershipPlan.setDeactivatedOn(null);
		
		PutMembershipPlanDto membershipPlanToUpdateDto = modelMapper.map(membershipPlanToUpdate, PutMembershipPlanDto.class);
		PutMembershipPlanDto putMembershipPlan = modelMapper.map(updatedMembershipPlan, PutMembershipPlanDto.class);
		putMembershipPlan.setId(membershipPlanToUpdate.getId());
		putMembershipPlan.setCode(membershipPlanToUpdateDto.getCode());
		putMembershipPlan.setDescription(null);
		putMembershipPlan.setName(null);

		// Act
		HttpEntity<PutMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, putMembershipPlan);
		ResponseEntity<MembershipPlanDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, brandId_FitnessBoxing, putMembershipPlan.getId())), port, null),
				HttpMethod.PUT, httpEntity, MembershipPlanDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put null error: %s", response.getStatusCode()));

		updatedMembershipPlan.setDescription(null);
		updatedMembershipPlan.setName(null);

		assertMembershipPlan(modelMapper.map(updatedMembershipPlan, MembershipPlanDto.class), response.getBody());
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testActivateSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		MembershipPlan membershipPlanToActivate = MembershipPlanBuilder.build(brandId_FitnessBoxing);
		membershipPlanToActivate.setActive(false);
		membershipPlanToActivate.setActivatedOn(null);
		membershipPlanToActivate.setDeactivatedOn(null);
		membershipPlanToActivate = membershipPlanRepository.save(membershipPlanToActivate);

		membershipPlanToActivate.setActive(true);
		membershipPlanToActivate.setActivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		membershipPlanToActivate.setDeactivatedOn(null);

		// Act
		HttpEntity<PutMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<MembershipPlanDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postActivateURI, brandId_FitnessBoxing, membershipPlanToActivate.getId())),
						port, null),
				HttpMethod.POST, httpEntity, MembershipPlanDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Membership Plan activation error: %s", response.getStatusCode()));

		assertMembershipPlan(modelMapper.map(membershipPlanToActivate, MembershipPlanDto.class), response.getBody());
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testActivateFailure(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange

		// Act
		HttpEntity<PutMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<Object> response = restTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(postActivateURI, brandId_FitnessBoxing, faker.code().isbn10())), port, null),
				HttpMethod.POST, httpEntity, Object.class);
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																											
		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Membership Plan activation error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testDeactivateSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		MembershipPlan membershipPlanToDeactivate = MembershipPlanBuilder.build(brandId_FitnessBoxing);
		membershipPlanToDeactivate.setActive(true);
		membershipPlanToDeactivate.setActivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		membershipPlanToDeactivate = membershipPlanRepository.save(membershipPlanToDeactivate);

		membershipPlanToDeactivate.setActive(false);
		membershipPlanToDeactivate.setDeactivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));

		// Act
		HttpEntity<PutMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<MembershipPlanDto> response = restTemplate.exchange(HttpUtils.createURL(
				URI.create(String.format(postDeactivateURI, brandId_FitnessBoxing, membershipPlanToDeactivate.getId())), port, null),
				HttpMethod.POST, httpEntity, MembershipPlanDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Membership Plan deactivation error: %s", response.getStatusCode()));

		assertMembershipPlan(modelMapper.map(membershipPlanToDeactivate, MembershipPlanDto.class), response.getBody());
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testDeactivateFailure(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange

		// Act
		HttpEntity<PutMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<Object> response = restTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(postDeactivateURI, brandId_FitnessBoxing, faker.code().ean13())), port, null),
				HttpMethod.POST, httpEntity, Object.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Membership Plan activation error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis" })
	void testPatchSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		MembershipPlan membershipPlanToPatch = MembershipPlanBuilder.build(brandId_FitnessBoxing);
		membershipPlanToPatch = membershipPlanRepository.save(membershipPlanToPatch);

		PatchMembershipPlanDto patchMembershipPlan = modelMapper.map(membershipPlanToPatch, PatchMembershipPlanDto.class);
		patchMembershipPlan.setId(membershipPlanToPatch.getId());
		patchMembershipPlan.setDescription(null);
		patchMembershipPlan.setName(null);

		// Act
		HttpEntity<PatchMembershipPlanDto> httpEntity = HttpUtils.createHttpEntity(role, user, patchMembershipPlan);
		ResponseEntity<MembershipPlanDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(patchURI, brandId_FitnessBoxing, patchMembershipPlan.getId())), port, null),
				HttpMethod.PATCH, httpEntity, MembershipPlanDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));

		membershipPlanToPatch.setCode(patchMembershipPlan.getCode());

		assertMembershipPlan(modelMapper.map(membershipPlanToPatch, MembershipPlanDto.class), response.getBody());
	}

	public static final void assertMembershipPlan(MembershipPlanDto expected, MembershipPlanDto result) {
		if (expected.getId() != null) {
			Assertions.assertEquals(expected.getId(), result.getId());
		}
		
		Assertions.assertEquals(expected.getBrandId(), result.getBrandId());
		Assertions.assertEquals(expected.getCode(), result.getCode());
		Assertions.assertEquals(expected.getDurationInMonths(), result.getDurationInMonths());
		Assertions.assertEquals(expected.getNumberOfClasses(), result.getNumberOfClasses());
		Assertions.assertEquals(expected.isGuestPrivilege(), result.isGuestPrivilege());
		Assertions.assertEquals(expected.isPromotional(), result.isPromotional());
		Assertions.assertEquals(expected.isGiftCard(), result.isGiftCard());
		
		Assertions.assertEquals(expected.getPeriod(), result.getPeriod());
		Assertions.assertEquals(expected.getBillingFrequency(), result.getBillingFrequency());

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
		
		if (expected.getCost() == null) {
			Assertions.assertNull(result.getCost());
		}
		
		if (expected.getCost() != null) {
			Assertions.assertNotNull(result.getCost());
			
			Assertions.assertEquals(expected.getCost().getCost(), result.getCost().getCost());
			Assertions.assertNotNull(result.getCost().getCurrency());
			Assertions.assertEquals(expected.getCost().getCurrency().getCode(), result.getCost().getCurrency().getCode());
			Assertions.assertEquals(expected.getCost().getCurrency().getName(), result.getCost().getCurrency().getName());
			Assertions.assertEquals(expected.getCost().getCurrency().getSymbol(), result.getCost().getCurrency().getSymbol());
		}
		
		if (expected.getCost() == null) {
			Assertions.assertNull(result.getCost());
		}
		
		if (expected.getOneTimeFees() != null) {
			Assertions.assertNotNull(result.getOneTimeFees());

			Assertions.assertEquals(expected.getOneTimeFees().size(), result.getOneTimeFees().size());
			
			expected.getOneTimeFees().forEach(oneTimeFee -> {
				Optional<OneTimeFeeDto> previous = result.getOneTimeFees().stream()
				.filter(item -> item.getCode().equals(oneTimeFee.getCode())).findFirst();
				Assertions.assertEquals(previous.get().getCost().getCost(), oneTimeFee.getCost().getCost());
				
				Assertions.assertNotNull(previous.get().getCost().getCurrency());
				Assertions.assertEquals(previous.get().getCost().getCurrency().getCode(), oneTimeFee.getCost().getCurrency().getCode());
				Assertions.assertEquals(previous.get().getCost().getCurrency().getName(), oneTimeFee.getCost().getCurrency().getName());
				Assertions.assertEquals(previous.get().getCost().getCurrency().getSymbol(), oneTimeFee.getCost().getCurrency().getSymbol());

			
				oneTimeFee.getDescription().forEach(description -> {
					Optional<LocalizedStringDto> previousDescription = previous.get().getDescription().stream()
							.filter(item -> item.getLanguage().equals(description.getLanguage())).findFirst();
					Assertions.assertTrue(previousDescription.isPresent());
					Assertions.assertEquals(previousDescription.get().getText(), description.getText());
				});
			});
		}
	}
}
