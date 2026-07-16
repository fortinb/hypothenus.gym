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
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.iso.hypo.admin.papi.dto.ErrorDto;
import com.iso.hypo.admin.papi.dto.model.UserDto;
import com.iso.hypo.admin.papi.dto.patch.PatchUserDto;
import com.iso.hypo.admin.papi.dto.post.PostUserDto;
import com.iso.hypo.admin.papi.dto.put.PutUserDto;
import com.iso.hypo.brand.application.exception.UserException;
import com.iso.hypo.brand.application.mapper.UserDtoMapper;
import com.iso.hypo.brand.application.usecase.UserService;
import com.iso.hypo.brand.domain.model.User;
import com.iso.hypo.brand.domain.repository.UserRepository;
import com.iso.hypo.common.application.dto.PageResultDto;
import com.iso.hypo.common.application.security.RoleEnum;
import com.iso.hypo.common.application.security.Roles;
import com.iso.hypo.domain.UserBuilder;
import com.iso.hypo.tests.http.HttpUtils;
import com.iso.hypo.tests.security.Users;

import net.datafaker.Faker;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper.Builder;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "app.test.run=true")
@TestInstance(Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class UserControllerTests {

	public static final String searchURI = "/v1/users/search";
	public static final String listURI = "/v1/users";
	public static final String postURI = "/v1/users/admin";
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
	UserDtoMapper userMapper;
	@Autowired
	Builder objectMapper;
	@Autowired
	ModelMapper modelMapper;

	private Faker faker = new Faker();

	private RestTestClient restClient;
	
	private User user;
	private User userDeleted;
	private List<User> users = new ArrayList<User>();

	@BeforeAll
	void arrange() {
    	restClient = RestTestClient.bindToServer()
		        .baseUrl("http://localhost:" + port)
		        .configureMessageConverters(converters -> 
		        	converters.addCustomConverter(
		        			new JacksonJsonHttpMessageConverter(
		        			objectMapper
		        			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
		        			.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false))))
		        .build();

		userRepository.deleteAll();

		user = UserBuilder.build();
		userRepository.save(user);

		userDeleted = UserBuilder.build();
		userDeleted.setDeleted(true);
		userDeleted = userRepository.save(userDeleted);

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

	@Test
	void testListFirstPageSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "0");
		params.add(pageSize, "4");

		// Act
		PageResultDto<UserDto> page = 
				this.restClient.get()
					.uri(HttpUtils.createURL(URI.create(listURI), port, params))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(new ParameterizedTypeReference<PageResultDto<UserDto>>() {}) 
					.returnResult()
				    .getResponseBody(); 
		
		// Assert
		Assertions.assertEquals(0, page.getPageNumber(),
				String.format("User list first page number invalid: %d", page.getPageNumber()));
		Assertions.assertEquals(4, page.getContent().size(),
				String.format("User list first page number of elements invalid: %d", page.getContent().size()));
	}

	@Test
	void testListSecondPageSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "1");
		params.add(pageSize, "4");

		// Act
		PageResultDto<UserDto> page = 
				this.restClient.get()
					.uri(HttpUtils.createURL(URI.create(listURI), port, params))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(new ParameterizedTypeReference<PageResultDto<UserDto>>() {}) 
					.returnResult()
				    .getResponseBody(); 

		// Assert
		Assertions.assertEquals(1, page.getPageNumber(),
				String.format("User list second page number invalid: %d", page.getPageNumber()));
		Assertions.assertEquals(4, page.getContent().size(),
				String.format("User list second page number of elements invalid: %d", page.getContent().size()));
	}

	@Test
	void testPostSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostUserDto postDto = modelMapper.map(UserBuilder.build(), PostUserDto.class);
		postDto.setRoles(new ArrayList<RoleEnum>());
		postDto.getRoles().add(RoleEnum.member);

		// Act
		UserDto createdDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(postURI), port, null))			
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(postDto)
					.exchange() 
				    .expectStatus().isCreated() 
					.expectBody(UserDto.class) 
					.returnResult()
				    .getResponseBody();

		// Assert
		assertUser(modelMapper.map(postDto, UserDto.class), createdDto);
	}

	@Test
	void testPostDuplicateFailure() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostUserDto postDto = modelMapper.map(UserBuilder.build(), PostUserDto.class);

		UserDto _ = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(postURI), port, null))			
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(postDto)
					.exchange() 
				    .expectStatus().isCreated() 
					.expectBody(UserDto.class) 
					.returnResult()
				    .getResponseBody();
		
		// Act
		UserDto dupDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(postURI), port, null))			
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(postDto)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(UserDto.class) 
					.returnResult()
				    .getResponseBody();
		// Assert
		Assertions.assertEquals(1, dupDto.getMessages().size(),
				String.format("Duplicate error ,missing message: %s", dupDto.getMessages().size()));

		Assertions.assertEquals(UserException.USER_ALREADY_EXIST, dupDto.getMessages().getFirst().getCode(),
				String.format("Duplicate error, missing message: %s", dupDto.getMessages().getFirst().getCode()));
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testGetSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostUserDto postDto = modelMapper.map(UserBuilder.build(), PostUserDto.class);

		UserDto createdDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(postURI), port, null))			
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(postDto)
					.exchange() 
				    .expectStatus().isCreated() 
					.expectBody(UserDto.class) 
					.returnResult()
				    .getResponseBody();
		
		// Act
		UserDto fetchedDto = 
				this.restClient.get()
					.uri(HttpUtils.createURL(URI.create(String.format(getURI, createdDto.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(UserDto.class) 
					.returnResult()
				    .getResponseBody();
        
		// Assert
		assertUser(modelMapper.map(postDto, UserDto.class), fetchedDto);
	}

	@Test
	void testGetFailureNotFound() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
    	ErrorDto _ = 
				this.restClient.get()
					.uri(HttpUtils.createURL(URI.create(String.format(getURI, faker.code().isbn10())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isNotFound() 
					.expectBody(ErrorDto.class) 
					.returnResult()
				    .getResponseBody(); 
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPutSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
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
		UserDto updatedDto = 
				this.restClient.put()
					.uri(HttpUtils.createURL(URI.create(String.format(putURI, updatedUser.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(putDto)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(UserDto.class) 
					.returnResult()
				    .getResponseBody();
		
		// Assert
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
		UserDto updatedDto = 
				this.restClient.put()
					.uri(HttpUtils.createURL(URI.create(String.format(putURI, updatedUser.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(putDto)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(UserDto.class) 
					.returnResult()
				    .getResponseBody();
		
		// Assert
		assertUser(modelMapper.map(putDto, UserDto.class), updatedDto);
	}

	@Test
	void testPutFailureNotFound() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PutUserDto putDto = modelMapper.map(UserBuilder.build(), PutUserDto.class);

		// Act
		ErrorDto _ = 
				this.restClient.put()
					.uri(HttpUtils.createURL(URI.create(String.format(putURI, putDto.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(putDto)
					.exchange() 
				    .expectStatus().isNotFound() 
					.expectBody(ErrorDto.class) 
					.returnResult()
				    .getResponseBody(); 
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPatchSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		User userToPatch = UserBuilder.build();
		userToPatch.setActive(true);
		userToPatch = userRepository.save(userToPatch);

		PatchUserDto patchDto = modelMapper.map(userToPatch, PatchUserDto.class);
		patchDto.setFirstname(null);

		// Act
		UserDto patchedDto = 
				this.restClient.patch()
					.uri(HttpUtils.createURL(URI.create(String.format(patchURI, userToPatch.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(patchDto)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(UserDto.class) 
					.returnResult()
				    .getResponseBody(); 
		
		// Assert
		patchDto.setFirstname(userToPatch.getFirstname());
		assertUser(modelMapper.map(patchDto, UserDto.class), patchedDto);
	}

	@Test
	void testPatchFailureNotFound() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PatchUserDto patchDto = modelMapper.map(UserBuilder.build(), PatchUserDto.class);

		// Act
		ErrorDto _ = 
				this.restClient.patch()
					.uri(HttpUtils.createURL(URI.create(String.format(patchURI, patchDto.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(patchDto)
					.exchange() 
				    .expectStatus().isNotFound() 
					.expectBody(ErrorDto.class) 
					.returnResult()
				    .getResponseBody(); 
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
		UserDto activatedDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(postActivateURI, userToActivate.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(UserDto.class) 
					.returnResult()
				    .getResponseBody(); 

		// Assert
		assertUser(modelMapper.map(userToActivate, UserDto.class), activatedDto);
	}

	@Test
	void testActivateFailureNotFound() throws JsonProcessingException, MalformedURLException, Exception {
		// Act
		ErrorDto _ = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(postActivateURI, faker.code().isbn10())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isNotFound() 
					.expectBody(ErrorDto.class) 
					.returnResult()
				    .getResponseBody(); 
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
		UserDto deactivatedDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(postDeactivateURI, userToDeactivate.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(UserDto.class) 
					.returnResult()
				    .getResponseBody();

		// Assert
		assertUser(modelMapper.map(userToDeactivate, UserDto.class), deactivatedDto);
	}

	@Test
	void testDeactivateFailureNotFound() throws JsonProcessingException, MalformedURLException, Exception {
		// Act
		ErrorDto _ = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(postDeactivateURI, faker.code().isbn10())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isNotFound() 
					.expectBody(ErrorDto.class) 
					.returnResult()
				    .getResponseBody();
	}

	@Test
	void testDeleteSuccess() throws JsonProcessingException, MalformedURLException {
		// Arrange
		PostUserDto postDto = modelMapper.map(UserBuilder.build(), PostUserDto.class);
		
		UserDto createdDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(postURI), port, null))			
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(postDto)
					.exchange() 
				    .expectStatus().isCreated() 
					.expectBody(UserDto.class) 
					.returnResult()
				    .getResponseBody();

		// Act
		UserDto _ = 
				this.restClient.delete()
					.uri(HttpUtils.createURL(URI.create(String.format(deleteURI, createdDto.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isAccepted() 
					.expectBody(UserDto.class) 
					.returnResult()
				    .getResponseBody();
		
		// Assert
		ErrorDto _ = 
				this.restClient.get()
					.uri(HttpUtils.createURL(URI.create(String.format(getURI, createdDto.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isNotFound() 
					.expectBody(ErrorDto.class) 
					.returnResult()
				    .getResponseBody();
	}


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
