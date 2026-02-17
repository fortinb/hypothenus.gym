package com.iso.hypo.admin.papi;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iso.hypo.admin.papi.dto.ErrorDto;
import com.iso.hypo.admin.papi.dto.enumeration.RoleEnum;
import com.iso.hypo.admin.papi.dto.model.UserDto;
import com.iso.hypo.admin.papi.dto.patch.PatchUserDto;
import com.iso.hypo.admin.papi.dto.post.PostUserDto;
import com.iso.hypo.admin.papi.dto.put.PutUserDto;
import com.iso.hypo.domain.UserBuilder;
import com.iso.hypo.domain.aggregate.User;
import com.iso.hypo.domain.security.Roles;
import com.iso.hypo.repositories.UserRepository;
import com.iso.hypo.services.UserService;
import com.iso.hypo.services.exception.UserException;
import com.iso.hypo.services.mappers.UserMapper;
import com.iso.hypo.tests.http.HttpUtils;
import com.iso.hypo.tests.security.Users;
import com.iso.hypo.tests.utils.TestResponseUtils;

import net.datafaker.Faker;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "app.test.run=true")
@TestInstance(Lifecycle.PER_CLASS)
class UserControllerTests {

	public static final String searchURI = "/v1/users/search";
	public static final String listURI = "/v1/users";
	public static final String postURI = "/v1/users";
	public static final String getURI = "/v1/users/%s";
	public static final String putURI = "/v1/users/%s";
	public static final String patchURI = "/v1/users/%s";
	public static final String deleteURI = "/v1/users/%s";
	public static final String postActivateURI = "/v1/users/%s/activate";
	public static final String postDeactivateURI = "/v1/users/%s/deactivate";
	public static final String searchCriteria = "criteria";
	public static final String pageNumber = "page";
	public static final String pageSize = "pageSize";

	public static final String codeUser_1 = "User1";

	@LocalServerPort
	private int port;

	@Autowired
	UserRepository userRepository;
	@Autowired
	UserService userService;
	@Autowired
	UserMapper userMapper;
	@Autowired
	ObjectMapper objectMapper;
	@Autowired
	ModelMapper modelMapper;

	private Faker faker = new Faker();

	private RestTemplateBuilder restTemplateBuilder;
	private TestRestTemplate testRestTemplate;
	private User user;
	private User userIsDeleted;
	private List<User> users = new ArrayList<User>();

	@BeforeAll
	void arrange() {
		restTemplateBuilder = new RestTemplateBuilder()
				.additionalMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper));

		testRestTemplate = new TestRestTemplate(restTemplateBuilder);

		userRepository.deleteAll();

		user = UserBuilder.build();
		userRepository.save(user);

		userIsDeleted = UserBuilder.build();
		userIsDeleted.setDeleted(true);
		userIsDeleted = userRepository.save(userIsDeleted);

		for (int i = 0; i < 10; i++) {
			User item = UserBuilder.build();

			userRepository.save(item);
			users.add(item);
		}

		for (int i = 0; i < 5; i++) {
			User item = UserBuilder.build();
			item.setActive(false);
			userRepository.save(item);
			users.add(item);
		}
	}

	@AfterAll
	void cleanup() {
		// Cleanup
		// userRepository.deleteAll();
	}

	/*
	 * @Test void testSearchAutocompleteIsDeletedSuccess() throws
	 * MalformedURLException, JsonProcessingException, Exception { // Act String
	 * criteria = StringUtils.extractRandomWordPartial(userIsDeleted.getFirstname(),
	 * 10); assertSearch(criteria, 0, 0); }
	 */
	 /* 		 
	 * @Test 
	 * void testSearchAutocompleteFirstnameSuccess() throws MalformedURLException,
	 * JsonProcessingException, Exception { // Act String criteria =
	 * StringUtils.extractRandomWordPartial(user.getFirstname(), 3);
	 * assertSearch(criteria, 1, 1000); }
	 * 
	 * @Test void testSearchAutocompleteLastnameSuccess() throws
	 * MalformedURLException, JsonProcessingException, Exception { // Act String
	 * criteria = StringUtils.extractRandomWordPartial(user.getLastname(), 3);
	 * assertSearch(criteria, 1, 1000); }
	 * 
	 * @Test void testSearchAutocompleteEmailSuccess() throws MalformedURLException,
	 * JsonProcessingException, Exception { // Act String criteria =
	 * StringUtils.extractRandomWordPartial(user.getEmail(), 3);
	 * assertSearch(criteria, 1, 1000); }
	 */

	@Test
	void testListFirstPageSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		HttpEntity<String> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, null);

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "0");
		params.add(pageSize, "4");

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(listURI), port, params), HttpMethod.GET, httpEntity, JsonNode.class);

		// Assert
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		Page<UserDto> page = TestResponseUtils.toPage(response, new TypeReference<Page<UserDto>>() {
		}, objectMapper);

		// Assert
		Assertions.assertEquals(0, page.getPageable().getPageNumber(),
				String.format("User list first page number invalid: %d", page.getPageable().getPageNumber()));
		Assertions.assertEquals(4, page.getNumberOfElements(),
				String.format("User list first page number of elements invalid: %d", page.getNumberOfElements()));
	}

	@Test
	void testListSecondPageSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		HttpEntity<String> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, "");

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "1");
		params.add(pageSize, "4");

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(listURI), port, params), HttpMethod.GET, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		Page<UserDto> page = TestResponseUtils.toPage(response, new TypeReference<Page<UserDto>>() {
		}, objectMapper);

		// Assert
		Assertions.assertEquals(1, page.getPageable().getPageNumber(),
				String.format("User list second page number invalid: %d", page.getPageable().getPageNumber()));
		Assertions.assertEquals(4, page.getNumberOfElements(),
				String.format("User list second page number of elements invalid: %d", page.getNumberOfElements()));
	}

	@Test
	void testPostSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostUserDto postDto = modelMapper.map(UserBuilder.build(), PostUserDto.class);
		postDto.setRoles(new ArrayList<RoleEnum>());
		postDto.getRoles().add(RoleEnum.member);
		HttpEntity<PostUserDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(postURI), port, null), HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));

		UserDto createdDto = TestResponseUtils.toDto(response, UserDto.class, objectMapper);

		assertUser(modelMapper.map(postDto, UserDto.class), createdDto);
	}

	@Test
	void testPostDuplicateFailure() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostUserDto postDto = modelMapper.map(UserBuilder.build(), PostUserDto.class);
		HttpEntity<PostUserDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postDto);

		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(postURI), port, null), HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));

		// Act
		response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(postURI), port, null), HttpMethod.POST,
				httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));

		UserDto dupDto = TestResponseUtils.toDto(response, UserDto.class, objectMapper);

		Assertions.assertEquals(1, dupDto.getMessages().size(),
				String.format("Duplicate error ,missing message: %s", dupDto.getMessages().size()));

		Assertions.assertEquals(UserException.USER_ALREADY_EXIST, dupDto.getMessages().getFirst().getCode(),
				String.format("Duplicate error, missing message: %s", dupDto.getMessages().getFirst().getCode()));
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testGetSuccess(String role, String userName) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostUserDto postDto = modelMapper.map(UserBuilder.build(), PostUserDto.class);
		HttpEntity<PostUserDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postDto);

		ResponseEntity<JsonNode> responsePost = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(postURI), port, null), HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.CREATED, responsePost.getStatusCode(),
				String.format("Post error: %s", responsePost.getStatusCode()));

		UserDto createdDto = TestResponseUtils.toDto(responsePost, UserDto.class, objectMapper);

		// Act
		httpEntity = HttpUtils.createHttpEntity(role, userName, null);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(getURI, createdDto.getUuid())), port, null),
				HttpMethod.GET, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));

		UserDto fetchedDto = TestResponseUtils.toDto(response, UserDto.class, objectMapper);

		assertUser(modelMapper.map(postDto, UserDto.class), fetchedDto);
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
			Assertions.assertEquals(UserException.USER_NOT_FOUND, err.getCode());
		}
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPutSuccess(String role, String userName) throws JsonProcessingException, MalformedURLException {
		// Arrange
		User updatedUser = UserBuilder.build();
		updatedUser.setActive(true);
		updatedUser.setIdpId(UUID.randomUUID().toString());
		updatedUser.setUpn(updatedUser.getEmail());
		updatedUser = userRepository.save(updatedUser);

		PutUserDto putDto = modelMapper.map(updatedUser, PutUserDto.class);

		putDto.setEmail(faker.internet().emailAddress());

		if (putDto.getFirstname() != null && !putDto.getFirstname().isEmpty()) {
			putDto.setFirstname(putDto.getFirstname() + " - updated");
		}

		if (putDto.getLastname() != null && !putDto.getLastname().isEmpty()) {
			putDto.setLastname(putDto.getLastname() + " - updated");
		}

		// Act
		HttpEntity<PutUserDto> httpEntity = HttpUtils.createHttpEntity(role, userName, putDto);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, updatedUser.getUuid())), port, null),
				HttpMethod.PUT, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put error: %s", response.getStatusCode()));

		UserDto updatedDto = TestResponseUtils.toDto(response, UserDto.class, objectMapper);

		assertUser(modelMapper.map(putDto, UserDto.class), updatedDto);
	}

	@Test
	void testPutNullSuccess() throws JsonProcessingException, MalformedURLException {
		// Arrange
		User updatedUser = UserBuilder.build();
		updatedUser.setActive(true);
		updatedUser.setIdpId(UUID.randomUUID().toString());
		updatedUser.setUpn(updatedUser.getEmail());
		updatedUser = userRepository.save(updatedUser);

		PutUserDto putDto = modelMapper.map(updatedUser, PutUserDto.class);

		putDto.setRoles(null);

		// Act
		HttpEntity<PutUserDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, putDto);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, updatedUser.getUuid())), port, null),
				HttpMethod.PUT, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put null error: %s", response.getStatusCode()));

		UserDto updatedDto = TestResponseUtils.toDto(response, UserDto.class, objectMapper);

		assertUser(modelMapper.map(putDto, UserDto.class), updatedDto);
	}

	@Test
	void testPutFailureNotFound() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		User updatedUser = UserBuilder.build();
		PutUserDto putDto = modelMapper.map(updatedUser, PutUserDto.class);

		HttpEntity<PutUserDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, putDto);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, updatedUser.getUuid())), port, null),
				HttpMethod.PUT, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));

		if (response.getBody() != null && response.getBody().isEmpty()) {
			ErrorDto err = TestResponseUtils.toError(response, objectMapper);
			Assertions.assertEquals(UserException.USER_NOT_FOUND, err.getCode());
		}
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPatchSuccess(String role, String userName) throws JsonProcessingException, MalformedURLException {
		// Arrange
		User userToPatch = UserBuilder.build();
		userToPatch.setActive(true);
		userToPatch = userRepository.save(userToPatch);

		PatchUserDto patchDto = modelMapper.map(userToPatch, PatchUserDto.class);
		patchDto.setFirstname(null);

		// Act
		HttpEntity<PatchUserDto> httpEntity = HttpUtils.createHttpEntity(role, userName, patchDto);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(patchURI, userToPatch.getUuid())), port, null),
				HttpMethod.PATCH, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));

		patchDto.setFirstname(userToPatch.getFirstname());

		UserDto patchedDto = TestResponseUtils.toDto(response, UserDto.class, objectMapper);
		assertUser(modelMapper.map(patchDto, UserDto.class), patchedDto);
	}

	@Test
	void testPatchFailureNotFound() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		User patchTarget = UserBuilder.build();
		PatchUserDto patchDto = modelMapper.map(patchTarget, PatchUserDto.class);

		HttpEntity<PatchUserDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, patchDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(patchURI, patchTarget.getUuid())), port, null),
				HttpMethod.PATCH, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Patch error: %s", response.getStatusCode()));

		if (response.getBody() != null && response.getBody().isEmpty()) {
			ErrorDto err = TestResponseUtils.toError(response, objectMapper);
			Assertions.assertEquals(UserException.USER_NOT_FOUND, err.getCode());
		}
	}

	@Test
	void testActivateSuccess() throws JsonProcessingException, MalformedURLException {
		// Arrange
		User userToActivate = UserBuilder.build();
		userToActivate.setActive(false);
		userToActivate.setActivatedOn(null);
		userToActivate.setDeactivatedOn(null);
		userToActivate = userRepository.save(userToActivate);

		userToActivate.setActive(true);
		userToActivate.setActivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		userToActivate.setDeactivatedOn(null);

		// Act
		HttpEntity<PutUserDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, null);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postActivateURI, userToActivate.getUuid())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("User activation error: %s", response.getStatusCode()));

		UserDto activatedDto = TestResponseUtils.toDto(response, UserDto.class, objectMapper);

		assertUser(modelMapper.map(userToActivate, UserDto.class), activatedDto);
	}

	@Test
	void testActivateFailureNotFound() throws JsonProcessingException, MalformedURLException, Exception {
		// Arrange
		HttpEntity<PutUserDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, null);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postActivateURI, faker.code().isbn10())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("User activation error: %s", response.getStatusCode()));

		if (response.getBody() != null && response.getBody().size() > 0) {
			ErrorDto err = TestResponseUtils.toError(response, objectMapper);
			Assertions.assertEquals(UserException.USER_NOT_FOUND, err.getCode());
		}
	}

	@Test
	void testDeactivateSuccess() throws JsonProcessingException, MalformedURLException {
		// Arrange
		User userToDeactivate = UserBuilder.build();
		userToDeactivate.setActive(true);
		userToDeactivate.setActivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		userToDeactivate = userRepository.save(userToDeactivate);

		userToDeactivate.setActive(false);
		userToDeactivate.setDeactivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));

		// Act
		HttpEntity<PutUserDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, null);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(postDeactivateURI, userToDeactivate.getUuid())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("User deactivation error: %s", response.getStatusCode()));

		UserDto deactivatedDto = TestResponseUtils.toDto(response, UserDto.class, objectMapper);

		assertUser(modelMapper.map(userToDeactivate, UserDto.class), deactivatedDto);
	}

	@Test
	void testDeactivateFailureNotFound() throws JsonProcessingException, MalformedURLException, Exception {
		// Arrange
		HttpEntity<PutUserDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, null);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postDeactivateURI, faker.code().ean13())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("User activation error: %s", response.getStatusCode()));

		if (response.getBody() != null && response.getBody().size() > 0) {
			ErrorDto err = TestResponseUtils.toError(response, objectMapper);
			Assertions.assertEquals(UserException.USER_NOT_FOUND, err.getCode());
		}
	}

	@Test
	void testDeleteSuccess() throws JsonProcessingException, MalformedURLException {
		// Arrange
		PostUserDto postDto = modelMapper.map(UserBuilder.build(), PostUserDto.class);
		HttpEntity<PostUserDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(postURI), port, null), HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));

		UserDto userToDeleteDto = TestResponseUtils.toDto(response, UserDto.class, objectMapper);

		// Act
		httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, null);
		response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(deleteURI, userToDeleteDto.getUuid())), port, null),
				HttpMethod.DELETE, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.ACCEPTED, response.getStatusCode(),
				String.format("User delete error: %s", response.getStatusCode()));

		httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, null);
		response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(getURI, userToDeleteDto.getUuid())), port, null),
				HttpMethod.GET, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));
	}

	/*
	 * private void assertSearch(String criteria, int minimumNumberOfElements, int
	 * maximumNumberOfElements) throws MalformedURLException,
	 * JsonProcessingException, Exception { // Arrange HttpEntity<String> httpEntity
	 * = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, "");
	 * 
	 * MultiValueMap<String, String> params = new LinkedMultiValueMap<String,
	 * String>(); params.add(searchCriteria, criteria); params.add(pageNumber, "0");
	 * params.add(pageSize, "4");
	 * 
	 * // Act await().atMost(10, TimeUnit.SECONDS).pollInterval(200,
	 * TimeUnit.MILLISECONDS).untilAsserted(() -> { ResponseEntity<JsonNode>
	 * response = testRestTemplate.exchange(
	 * HttpUtils.createURL(URI.create(searchURI), port, params), HttpMethod.GET,
	 * httpEntity, JsonNode.class);
	 * 
	 * Assertions.assertEquals(response.getStatusCode(), HttpStatus.OK,
	 * String.format("Search error: %s", response.getStatusCode()));
	 * 
	 * Page<UserSearchDto> page = TestResponseUtils.toPage(response, new
	 * TypeReference<Page<UserSearchDto>>() { }, objectMapper);
	 * 
	 * Assertions.assertTrue( page.getNumberOfElements() >= minimumNumberOfElements
	 * && page.getNumberOfElements() <= maximumNumberOfElements,
	 * String.format("User search return invalid number of results [%s]: %d",
	 * criteria, page.getNumberOfElements())); }); }
	 */

	public static final void assertUser(UserDto expected, UserDto result) {
		
		Assertions.assertEquals(expected.getFirstname(), result.getFirstname());
		Assertions.assertEquals(expected.getLastname(), result.getLastname());
		Assertions.assertEquals(expected.getEmail(), result.getEmail());
		
		if (expected.getRoles() != null) {
			for (RoleEnum role : expected.getRoles()) {
				Assertions.assertTrue(result.getRoles().contains(role),
						String.format("User role %s is missing in result", role));
			}
		}
	}
}
