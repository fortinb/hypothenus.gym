package com.isoceles.hypothenus.gym.admin.papi;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.isoceles.hypothenus.gym.admin.papi.dto.GymDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.GymSearchDto;
import com.isoceles.hypothenus.gym.domain.model.Address;
import com.isoceles.hypothenus.gym.domain.model.PhoneNumber;
import com.isoceles.hypothenus.gym.domain.model.PhoneNumberTypeEnum;
import com.isoceles.hypothenus.gym.domain.model.SocialMediaAccount;
import com.isoceles.hypothenus.gym.domain.model.SocialMediaTypeEnum;
import com.isoceles.hypothenus.gym.domain.model.aggregate.Gym;
import com.isoceles.hypothenus.gym.domain.repository.GymRepository;
import com.isoceles.hypothenus.tests.http.HttpUtils;
import com.isoceles.hypothenus.tests.security.Roles;
import com.isoceles.hypothenus.tests.security.SecurityUtils;
import com.isoceles.hypothenus.tests.security.Users;
import com.isoceles.hypothenus.tests.utils.StringUtils;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
class GymControllerTests {

	public static final String searchURI = "/v1/admin/gyms/search";
	public static final String listURI = "/v1/admin/gyms/list";
	public static final String searchCriteria = "criteria";
	public static final String pageNumber = "page";
	public static final String pageSize = "pageSize";

	private static Faker faker = new Faker();

	@LocalServerPort
	private int port;

	@Autowired
	GymRepository gymRepository;
	
	@Autowired
	ObjectMapper objectMapper;

	private TestRestTemplate restTemplate = new TestRestTemplate();

	private Gym gym;
	private List<Gym> gyms = new ArrayList<Gym>();

	@BeforeAll
	void arrange() {
		// Arrange
		gymRepository.deleteAll();
		
		gym = createGym();
		gymRepository.save(gym);

		for (int i = 0; i < 10; i++) {
			Gym item = createGym();

			gymRepository.save(item);
			gyms.add(item);
		}
	}

	@AfterAll
	void cleanup() {
		// Cleanup
	//	gymRepository.deleteAll();
	}

	// @ParameterizedTest
	// @ValueSource(strings = { "racecar", "radar", "able was I ere I saw elba" })

	@Test
	void testSearchAutocompleteCitySuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(gym.getAddress().getCity(), 3);
		ResponseEntity<List<GymSearchDto>> response = search(criteria);

		// Assert
		Assert.isTrue(response.getStatusCode() == HttpStatus.OK,
				String.format("Search error: %s", response.getStatusCode()));
		Assert.isTrue(response.getBody().size() > 0, String.format("Gym search by city return no results [%s]", criteria));
	}

	@Test
	void testSearchAutocompleteStateSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(gym.getAddress().getState(), 2);
		ResponseEntity<List<GymSearchDto>> response = search(criteria);

		// Assert
		Assert.isTrue(response.getStatusCode() == HttpStatus.OK,
				String.format("Search error: %s", response.getStatusCode()));
		Assert.isTrue(response.getBody().size() > 0, String.format("Gym search by state return no results [%s]", criteria));
	}

	@Test
	void testSearchAutocompleteStreetNameSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(gym.getAddress().getStreetName(), 3);
		ResponseEntity<List<GymSearchDto>> response = search(criteria);

		// Assert
		Assert.isTrue(response.getStatusCode() == HttpStatus.OK,
				String.format("Search error: %s", response.getStatusCode()));
		Assert.isTrue(response.getBody().size() > 0, String.format("Gym search by streetName return no results [%s]", criteria));
	}

	@Test
	void testSearchAutocompleteZipCodeSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(gym.getAddress().getZipCode(), 3);
		ResponseEntity<List<GymSearchDto>> response = search(criteria);

		// Assert
		Assert.isTrue(response.getStatusCode() == HttpStatus.OK,
				String.format("Search error: %s", response.getStatusCode()));
		Assert.isTrue(response.getBody().size() > 0, String.format("Gym search by zipCode return no results [%s]", criteria));
	}

	@Test
	void testSearchAutocompleteNameSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(gym.getName(), 3);
		ResponseEntity<List<GymSearchDto>> response = search(criteria);
		
		// Assert
		Assert.isTrue(response.getStatusCode() == HttpStatus.OK,
				String.format("Search error: %s", response.getStatusCode()));
		Assert.isTrue(response.getBody().size() > 0, String.format("Gym search by name return no results [%s]", criteria));
	}

	@Test
	void testSearchAutocompleteEmailSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(gym.getEmail(), 3);
		ResponseEntity<List<GymSearchDto>> response = search(criteria);
		
		// Assert
		Assert.isTrue(response.getStatusCode() == HttpStatus.OK,
				String.format("Search error: %s", response.getStatusCode()));
		Assert.isTrue(response.getBody().size() > 0, String.format("Gym search by email return no results [%s]", criteria));
	}

	@Test
	void testListFirstPageSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange

		// Act
		HttpHeaders httpHeaders = SecurityUtils.getHeaders(Roles.Admin, Users.Admin);
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "0");
		params.add(pageSize, "4");

		HttpEntity<String> entity = new HttpEntity<>(null, httpHeaders);

		ResponseEntity<String> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(listURI), port, params), HttpMethod.GET, entity,
				new ParameterizedTypeReference<String>() {
				});
		
		Assert.isTrue(response.getStatusCode() == HttpStatus.OK,
				String.format("List error: %s", response.getStatusCode()));
		
		Page<GymDto> page = objectMapper.readValue(response.getBody(), new TypeReference<Page<GymDto>>(){});
		
		// Assert
		Assert.isTrue(page.getPageable().getPageNumber() == 0, String
				.format("Gym list first page number invalid: %d", page.getPageable().getPageNumber()));
		Assert.isTrue(page.getNumberOfElements() == 4, String.format(
				"Gym list first page number of elements invalid: %d", page.getNumberOfElements()));
	}

	@Test
	void testListSecondPageSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange

		// Act
		HttpHeaders httpHeaders = SecurityUtils.getHeaders(Roles.Admin, Users.Admin);
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "1");
		params.add(pageSize, "4");

		HttpEntity<String> entity = new HttpEntity<>(null, httpHeaders);

		ResponseEntity<String> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(listURI), port, params), HttpMethod.GET, entity,
				new ParameterizedTypeReference<String>() {
				});
		
		Assert.isTrue(response.getStatusCode() == HttpStatus.OK,
				String.format("List error: %s", response.getStatusCode()));
		
		Page<GymDto> page = objectMapper.readValue(response.getBody(), new TypeReference<Page<GymDto>>(){});

		// Assert
		Assert.isTrue(page.getPageable().getPageNumber() == 1, String
				.format("Gym list second page number invalid: %d", page.getPageable().getPageNumber()));
		Assert.isTrue(page.getNumberOfElements() == 4, String.format(
				"Gym list second page number of elements invalid: %d", page.getNumberOfElements()));
	}

	private ResponseEntity<List<GymSearchDto>> search(String criteria)
			throws JsonProcessingException, MalformedURLException {
		HttpHeaders httpHeaders = SecurityUtils.getHeaders(Roles.Admin, Users.Admin);
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(searchCriteria, criteria);

		HttpEntity<String> entity = new HttpEntity<>(null, httpHeaders);

		ResponseEntity<List<GymSearchDto>> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(searchURI), port, params), HttpMethod.GET, entity,
				new ParameterizedTypeReference<List<GymSearchDto>>() {
				});

		return response;
	}

	static public Gym createGym() {
		Gym entity = new Gym(faker.code().isbn10(), faker.company().name(), createAddress(),
				faker.internet().emailAddress(), "fr-CA", createPhoneNumbers(), createSocialMediaAccounts());
		return entity;

	}

	static public Address createAddress() {
		return new Address(faker.address().buildingNumber(), faker.address().streetName(), "35",
				faker.address().cityName(), faker.address().stateAbbr(), faker.address().zipCode());
	}

	static public List<PhoneNumber> createPhoneNumbers() {
		ArrayList<PhoneNumber> phoneNumbers = new ArrayList<PhoneNumber>();
		phoneNumbers.add(new PhoneNumber("514", faker.phoneNumber().cellPhone(), PhoneNumberTypeEnum.Mobile));
		phoneNumbers.add(new PhoneNumber("514", faker.phoneNumber().phoneNumber(), PhoneNumberTypeEnum.Home));

		return phoneNumbers;
	}

	static public List<SocialMediaAccount> createSocialMediaAccounts() {
		ArrayList<SocialMediaAccount> socialMediaAccount = new ArrayList<SocialMediaAccount>();
		socialMediaAccount.add(new SocialMediaAccount(SocialMediaTypeEnum.Facebook, faker.company().name(),
				URI.create(faker.company().url())));
		socialMediaAccount.add(new SocialMediaAccount(SocialMediaTypeEnum.Instagram, faker.company().name(),
				URI.create(faker.company().url())));

		return socialMediaAccount;
	}

}
