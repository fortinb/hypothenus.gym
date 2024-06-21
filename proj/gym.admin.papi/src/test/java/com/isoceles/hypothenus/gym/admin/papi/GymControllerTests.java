package com.isoceles.hypothenus.gym.admin.papi;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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
import com.isoceles.hypothenus.gym.admin.papi.dto.GymDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.GymSearchDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.PhoneNumberDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.SocialMediaAccountDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.patch.PatchGymDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.post.PostGymDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.put.PutGymDto;
import com.isoceles.hypothenus.gym.domain.model.aggregate.Gym;
import com.isoceles.hypothenus.gym.domain.repository.GymRepository;
import com.isoceles.hypothenus.tests.http.HttpUtils;
import com.isoceles.hypothenus.tests.model.GymBuilder;
import com.isoceles.hypothenus.tests.security.Roles;
import com.isoceles.hypothenus.tests.security.Users;
import com.isoceles.hypothenus.tests.utils.StringUtils;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
class GymControllerTests {

	public static final String searchURI = "/v1/admin/gyms/search";
	public static final String listURI = "/v1/admin/gyms";
	public static final String postURI = "/v1/admin/gyms";
	public static final String getURI = "/v1/admin/gyms/%s";
	public static final String putURI = "/v1/admin/gyms/%s";
	public static final String patchURI = "/v1/admin/gyms/%s";
	public static final String searchCriteria = "criteria";
	public static final String pageNumber = "page";
	public static final String pageSize = "pageSize";

	@LocalServerPort
	private int port;

	@Autowired
	GymRepository gymRepository;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	ModelMapper modelMapper;

	private Faker faker = new Faker();
	
	private TestRestTemplate restTemplate = new TestRestTemplate();

	private Gym gym;
	private Gym gymIsDeleted;
	private List<Gym> gyms = new ArrayList<Gym>();

	@BeforeAll
	void arrange() {
		restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
		
		gymRepository.deleteAll();

		gym = GymBuilder.build(faker.code().isbn10());
		gymRepository.save(gym);
		
		gymIsDeleted = GymBuilder.build(faker.code().isbn10());
		gymIsDeleted.setDeleted(true);
		gymIsDeleted = gymRepository.save(gymIsDeleted);

		for (int i = 0; i < 10; i++) {
			Gym item = GymBuilder.build(faker.code().isbn10());

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
		ResponseEntity<List<GymSearchDto>> response = search(criteria);

		Assertions.assertTrue(response.getBody().size() == 0,
				String.format("Gym search by name return results for isDeleted [%s]", criteria));
	}
	
	@Test
	void testSearchAutocompleteCitySuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(gym.getAddress().getCity(), 3);
		ResponseEntity<List<GymSearchDto>> response = search(criteria);

		// Assert
		Assertions.assertTrue(response.getBody().size() > 0,
				String.format("Gym search by city return no results [%s]", criteria));
	}

	@Test
	void testSearchAutocompleteStateSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(gym.getAddress().getState(), 2);
		ResponseEntity<List<GymSearchDto>> response = search(criteria);

		// Assert
		Assertions.assertTrue(response.getBody().size() > 0,
				String.format("Gym search by state return no results [%s]", criteria));
	}

	@Test
	void testSearchAutocompleteStreetNameSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(gym.getAddress().getStreetName(), 3);
		ResponseEntity<List<GymSearchDto>> response = search(criteria);

		// Assert
		Assertions.assertTrue(response.getBody().size() > 0,
				String.format("Gym search by streetName return no results [%s]", criteria));
	}

	@Test
	void testSearchAutocompleteZipCodeSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(gym.getAddress().getZipCode(), 3);
		ResponseEntity<List<GymSearchDto>> response = search(criteria);

		// Assert
		Assertions.assertTrue(response.getBody().size() > 0,
				String.format("Gym search by zipCode return no results [%s]", criteria));
	}

	@Test
	void testSearchAutocompleteNameSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(gym.getName(), 3);
		ResponseEntity<List<GymSearchDto>> response = search(criteria);

		// Assert
		Assertions.assertTrue(response.getBody().size() > 0,
				String.format("Gym search by name return no results [%s]", criteria));
	}

	@Test
	void testSearchAutocompleteEmailSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(gym.getEmail(), 3);
		ResponseEntity<List<GymSearchDto>> response = search(criteria);

		// Assert
		Assertions.assertTrue(response.getBody().size() > 0,
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
		ResponseEntity<String> response = restTemplate.exchange(HttpUtils.createURL(URI.create(listURI), port, params),
				HttpMethod.GET, httpEntity, new ParameterizedTypeReference<String>() {
				});

		// Assert
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		Page<GymDto> page = objectMapper.readValue(response.getBody(), new TypeReference<Page<GymDto>>() {
		});

		// Assert
		Assertions.assertEquals(0, page.getPageable().getPageNumber(),
				String.format("Gym list first page number invalid: %d", page.getPageable().getPageNumber()));
		Assertions.assertEquals(4, page.getNumberOfElements(),
				String.format("Gym list first page number of elements invalid: %d", page.getNumberOfElements()));
	}

	@Test
	void testListSecondPageSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		HttpEntity<String> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, "");

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "1");
		params.add(pageSize, "4");

		// Act
		ResponseEntity<String> response = restTemplate.exchange(HttpUtils.createURL(URI.create(listURI), port, params),
				HttpMethod.GET, httpEntity, new ParameterizedTypeReference<String>() {
				});

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		Page<GymDto> page = objectMapper.readValue(response.getBody(), new TypeReference<Page<GymDto>>() {
		});

		// Assert
		Assertions.assertEquals(1, page.getPageable().getPageNumber(),
				String.format("Gym list second page number invalid: %d", page.getPageable().getPageNumber()));
		Assertions.assertEquals(4, page.getNumberOfElements(),
				String.format("Gym list second page number of elements invalid: %d", page.getNumberOfElements()));
	}

	@Test
	void testPostSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostGymDto postGym = modelMapper.map(GymBuilder.build(faker.code().isbn10()), PostGymDto.class);
		HttpEntity<PostGymDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postGym);

		// Act
		ResponseEntity<GymDto> response = restTemplate.exchange(HttpUtils.createURL(URI.create(postURI), port, null),
				HttpMethod.POST, httpEntity, GymDto.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));

		assertGym(modelMapper.map(postGym, GymDto.class), response.getBody());
	}

	@ParameterizedTest
	@CsvSource({ "Admin, Bruno Fortin", "Manager, Liliane Denis", "Member, Guillaume Fortin", })
	void testGetSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostGymDto postGym = modelMapper.map(GymBuilder.build(faker.code().isbn10()), PostGymDto.class);
		HttpEntity<PostGymDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postGym);

		ResponseEntity<GymDto> responsePost = restTemplate.exchange(
				HttpUtils.createURL(URI.create(postURI), port, null), HttpMethod.POST, httpEntity, GymDto.class);

		Assertions.assertEquals(HttpStatus.CREATED, responsePost.getStatusCode(),
				String.format("Post error: %s", responsePost.getStatusCode()));

		// Act
		httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<GymDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(getURI, responsePost.getBody().getGymId())), port, null),
				HttpMethod.GET, httpEntity, GymDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));

		assertGym(modelMapper.map(postGym, GymDto.class), response.getBody());
	}

	@Test
	void testPutSuccess() throws JsonProcessingException, MalformedURLException {
		// Arrange
		Gym updatedGym = GymBuilder.build(faker.code().isbn10());

		PutGymDto putGym = modelMapper.map(updatedGym, PutGymDto.class);
		putGym.setGymId(gym.getGymId());

		// Act
		HttpEntity<PutGymDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, putGym);
		ResponseEntity<GymDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, updatedGym.getGymId())), port, null),
				HttpMethod.PUT, httpEntity, GymDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put error: %s", response.getStatusCode()));

		assertGym(modelMapper.map(putGym, GymDto.class), response.getBody());
	}
	
	@Test
	void testPutNullSuccess() throws JsonProcessingException, MalformedURLException {
		// Arrange
		Gym updatedGym = GymBuilder.build(faker.code().isbn10());

		PutGymDto putGym = modelMapper.map(updatedGym, PutGymDto.class);
		putGym.setGymId(gym.getGymId());
		
		putGym.setEmail(null);
		putGym.setAddress(null);
		putGym.setLanguage(null);
		putGym.setName(null);
		putGym.setPhoneNumbers(null);
		putGym.setSocialMediaAccounts(null);
		
		// Act
		HttpEntity<PutGymDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, putGym);
		ResponseEntity<GymDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, updatedGym.getGymId())), port, null),
				HttpMethod.PUT, httpEntity, GymDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put null error: %s", response.getStatusCode()));

 		assertGym(modelMapper.map(putGym, GymDto.class), response.getBody());
	}

	@Test
	void testPatchSuccess() throws JsonProcessingException, MalformedURLException {
		// Arrange
		Gym updatedGym = GymBuilder.build(faker.code().isbn10());

		PatchGymDto patchGym = modelMapper.map(updatedGym, PatchGymDto.class);
		patchGym.setGymId(gym.getGymId());
		patchGym.setEmail(null);
		patchGym.setLanguage(null);
		patchGym.setName(null);
		
		// Act
		HttpEntity<PatchGymDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, patchGym);
		ResponseEntity<GymDto> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(patchURI, updatedGym.getGymId())), port, null),
				HttpMethod.PATCH, httpEntity, GymDto.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));

		patchGym.setEmail(gym.getEmail());
		patchGym.setLanguage(gym.getLanguage());
		patchGym.setName(gym.getName());
		
 		assertGym(modelMapper.map(patchGym, GymDto.class), response.getBody());
	}
	
	private ResponseEntity<List<GymSearchDto>> search(String criteria)
			throws JsonProcessingException, MalformedURLException {
		// Arrange
		HttpEntity<String> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, "");

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(searchCriteria, criteria);

		// Act
		ResponseEntity<List<GymSearchDto>> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(searchURI), port, params), HttpMethod.GET, httpEntity,
				new ParameterizedTypeReference<List<GymSearchDto>>() {
				});

		Assertions.assertEquals(response.getStatusCode(), HttpStatus.OK,
				String.format("Search error: %s", response.getStatusCode()));
		return response;
	}

	public static final void assertGym(GymDto expected, GymDto result) {
		Assertions.assertEquals(expected.getGymId(), result.getGymId());
		Assertions.assertEquals(expected.getName(), result.getName());
		Assertions.assertEquals(expected.getEmail(), result.getEmail());
		Assertions.assertEquals(expected.getLanguage(), result.getLanguage());
		
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
				Assertions.assertEquals(previous.get().getRegionalCode(), phone.getRegionalCode());
				Assertions.assertEquals(previous.get().getNumber(), phone.getNumber());
			});
		}

		if (expected.getPhoneNumbers() == null) {
			Assertions.assertNull(result.getPhoneNumbers());
		}

		if (expected.getSocialMediaAccounts() != null) {
			Assertions.assertNotNull(result.getSocialMediaAccounts());

			Assertions.assertEquals(expected.getSocialMediaAccounts().size(), result.getSocialMediaAccounts().size());
			expected.getSocialMediaAccounts().forEach(account -> {
				Optional<SocialMediaAccountDto> previous = result.getSocialMediaAccounts().stream()
						.filter(item -> item.getSocialMedia().equals(account.getSocialMedia())).findFirst();
				Assertions.assertTrue(previous.isPresent());
				Assertions.assertEquals(previous.get().getAccountName(), account.getAccountName());
				Assertions.assertEquals(previous.get().getUrl(), account.getUrl());
			});
		}

		if (expected.getSocialMediaAccounts() == null) {
			Assertions.assertNull(result.getSocialMediaAccounts());
		}
	}
}
