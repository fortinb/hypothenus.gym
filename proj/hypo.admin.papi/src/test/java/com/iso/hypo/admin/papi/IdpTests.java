package com.iso.hypo.admin.papi;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;

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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iso.hypo.admin.papi.dto.enumeration.RoleEnum;
import com.iso.hypo.admin.papi.dto.model.BrandDto;
import com.iso.hypo.admin.papi.dto.model.UserDto;
import com.iso.hypo.admin.papi.dto.post.PostBrandDto;
import com.iso.hypo.admin.papi.dto.post.PostMemberDto;
import com.iso.hypo.admin.papi.dto.post.PostUserDto;
import com.iso.hypo.admin.papi.dto.put.PutMemberDto;
import com.iso.hypo.admin.papi.dto.put.PutUserDto;
import com.iso.hypo.domain.BrandBuilder;
import com.iso.hypo.domain.MemberBuilder;
import com.iso.hypo.domain.UserBuilder;
import com.iso.hypo.domain.aggregate.Brand;
import com.iso.hypo.domain.enumeration.MemberTypeEnum;
import com.iso.hypo.domain.security.Roles;
import com.iso.hypo.repositories.BrandRepository;
import com.iso.hypo.repositories.MemberRepository;
import com.iso.hypo.repositories.UserRepository;
import com.iso.hypo.services.UserService;
import com.iso.hypo.services.mappers.UserMapper;
import com.iso.hypo.tests.http.HttpUtils;
import com.iso.hypo.tests.security.Users;
import com.iso.hypo.tests.utils.TestResponseUtils;

import net.datafaker.Faker;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "app.test.run=false")
@TestInstance(Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class IdpTests {

	public static final String postBrandURI = "/v1/brands";
	
	public static final String userPostURI = "/v1/users";
	public static final String userGetURI = "/v1/users/%s";
	public static final String userPutURI = "/v1/users/%s";
	public static final String userPatchURI = "/v1/users/%s";
	public static final String userDeleteURI = "/v1/users/%s";

	public static final String memberPostURI = "/v1/brands/%s/members/register";
    public static final String memberGetURI = "/v1/brands/%s/members/%s";
    public static final String memberPutURI = "/v1/brands/%s/members/%s";
    public static final String memberPatchURI = "/v1/brands/%s/members/%s";
    public static final String memberDeleteURI = "/v1/brands/%s/members/%s";
    
	@LocalServerPort
	private int port;
	
	@Autowired
	BrandRepository brandRepository;
    @Autowired
    MemberRepository memberRepository;
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
	private Brand brand;

	public static final String codeBrand_1 = "Brand1";
	
	@BeforeAll
	void arrange() {
		restTemplateBuilder = new RestTemplateBuilder()
				.additionalMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper));

		testRestTemplate = new TestRestTemplate(restTemplateBuilder);

		userRepository.deleteAll();

		try {
			PostBrandDto postBrandDto = modelMapper.map(BrandBuilder.build(faker.code().isbn10(),faker.company().name()), PostBrandDto.class);
			HttpEntity<PostBrandDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postBrandDto);

			// Act
			ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(postBrandURI), port, null),
					HttpMethod.POST, httpEntity, JsonNode.class);
			
			BrandDto createdDto = TestResponseUtils.toDto(response, BrandDto.class, objectMapper);
			brand = objectMapper.convertValue(createdDto, Brand.class);
		} catch (Exception e) {
			// user already exists
		}
	}

	@AfterAll
	void cleanup() {
		// Cleanup
		// userRepository.deleteAll();
	}


	@Test
	void testPostBrandSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostBrandDto postDto = modelMapper.map(BrandBuilder.build(faker.code().isbn10(),faker.company().name()), PostBrandDto.class);
		HttpEntity<PostBrandDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(postBrandURI), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
	}
	
	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin" })
	void testPostUserSuccess(String role, String userName) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostUserDto postDto = modelMapper.map(UserBuilder.build(), PostUserDto.class);
		postDto.setRoles(new ArrayList<RoleEnum>());
		postDto.getRoles().add(role.equals("admin") ? RoleEnum.admin : RoleEnum.manager);
		HttpEntity<PostUserDto> httpEntity = HttpUtils.createHttpEntity(role, userName, postDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(userPostURI), port, null), HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
	}
	
	@Test
	void testPutUserNotAllowed() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostUserDto postDto = modelMapper.map(UserBuilder.build(), PostUserDto.class);
		postDto.setRoles(new ArrayList<RoleEnum>());
		postDto.getRoles().add(RoleEnum.manager);
		postDto.getRoles().add(RoleEnum.member);
		HttpEntity<PostUserDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postDto);

		// Act
		ResponseEntity<JsonNode> responsePost = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(userPostURI), port, null), HttpMethod.POST, httpEntity, JsonNode.class);
		Assertions.assertEquals(HttpStatus.CREATED, responsePost.getStatusCode(),
				String.format("Post error: %s", responsePost.getStatusCode()));
		
		UserDto createdDto = TestResponseUtils.toDto(responsePost, UserDto.class, objectMapper);
		
		PutUserDto putDto = modelMapper.map(createdDto, PutUserDto.class);
		putDto.getRoles().add(RoleEnum.admin);

		// Act
		HttpEntity<PutUserDto> putHttpEntity = HttpUtils.createHttpEntity(Roles.Manager, Users.Manager, putDto);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(userPutURI, createdDto.getUuid())), port, null),
				HttpMethod.PUT, putHttpEntity, JsonNode.class);
		
		Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testGetUserSuccess(String role, String userName) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostUserDto postDto = modelMapper.map(UserBuilder.build(), PostUserDto.class);
		postDto.setRoles(new ArrayList<RoleEnum>());
		postDto.getRoles().add(role.equals("admin") ? RoleEnum.admin : RoleEnum.manager);
		HttpEntity<PostUserDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postDto);

		ResponseEntity<JsonNode> responsePost = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(userPostURI), port, null), HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.CREATED, responsePost.getStatusCode(),
				String.format("Post error: %s", responsePost.getStatusCode()));

		UserDto createdDto = TestResponseUtils.toDto(responsePost, UserDto.class, objectMapper);

		// Act
		httpEntity = HttpUtils.createHttpEntity(role, userName, null);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(userGetURI, createdDto.getUuid())), port, null),
				HttpMethod.GET, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));
		
		UserDto userDto = TestResponseUtils.toDto(response, UserDto.class, objectMapper);
		Assertions.assertTrue(userDto.getFirstname().equals(createdDto.getFirstname()));
		Assertions.assertTrue(userDto.getLastname().equals(createdDto.getLastname()));
		Assertions.assertTrue(userDto.getEmail().equals(createdDto.getEmail()));
		Assertions.assertTrue(userDto.getRoles().contains(RoleEnum.valueOf(role)));
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin" })
	void testPutUserSuccess(String role, String userName) throws JsonProcessingException, MalformedURLException {
		// Arrange
		PostUserDto postDto = modelMapper.map(UserBuilder.build(), PostUserDto.class);
		postDto.setRoles(new ArrayList<RoleEnum>());
		postDto.getRoles().add(RoleEnum.admin);
		postDto.getRoles().add(RoleEnum.manager);
		HttpEntity<PostUserDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postDto);

		ResponseEntity<JsonNode> responsePost = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(userPostURI), port, null), HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.CREATED, responsePost.getStatusCode(),
				String.format("Post error: %s", responsePost.getStatusCode()));
		
		UserDto createdDto = TestResponseUtils.toDto(responsePost, UserDto.class, objectMapper);
		
		PutUserDto putDto = modelMapper.map(createdDto, PutUserDto.class);
		putDto.getRoles().add(RoleEnum.member);
		putDto.getRoles().remove(RoleEnum.manager);

		// Act
		HttpEntity<PutUserDto> putHttpEntity = HttpUtils.createHttpEntity(role, userName, putDto);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(userPutURI, createdDto.getUuid())), port, null),
				HttpMethod.PUT, putHttpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put error: %s", response.getStatusCode()));
		
		UserDto updatedDto = TestResponseUtils.toDto(response, UserDto.class, objectMapper);
		Assertions.assertTrue(updatedDto.getRoles().contains(RoleEnum.admin));
		Assertions.assertTrue(updatedDto.getRoles().contains(RoleEnum.member));
		Assertions.assertFalse(updatedDto.getRoles().contains(RoleEnum.manager));
		
	}

	@Test
	void testDeleteUserSuccess() throws JsonProcessingException, MalformedURLException {
		// Arrange
		PostUserDto postDto = modelMapper.map(UserBuilder.build(), PostUserDto.class);
		HttpEntity<PostUserDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(userPostURI), port, null), HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));

		UserDto userToDeleteDto = TestResponseUtils.toDto(response, UserDto.class, objectMapper);

		// Act
		httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, null);
		response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(userDeleteURI, userToDeleteDto.getUuid())), port, null),
				HttpMethod.DELETE, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.ACCEPTED, response.getStatusCode(),
				String.format("User delete error: %s", response.getStatusCode()));
	}

	@Test
	void testPostMemberSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostMemberDto postDto = modelMapper.map(MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular), PostMemberDto.class);
		postDto.setPassword("admin.test.1");
		HttpEntity<PostMemberDto> httpEntityMember = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(String.format(memberPostURI, brand.getUuid())), port, null),
				HttpMethod.POST, httpEntityMember, JsonNode.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
	}
	
	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPutMemberSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {

		PostMemberDto postDto = modelMapper.map(MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular), PostMemberDto.class);
		postDto.setPassword("admin.test.1");
		HttpEntity<PostMemberDto> httpEntityMember = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(String.format(memberPostURI, brand.getUuid())), port, null),
				HttpMethod.POST, httpEntityMember, JsonNode.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
		
		PutMemberDto putDto = TestResponseUtils.toDto(response, PutMemberDto.class, objectMapper);
		putDto.getPerson().setEmail(faker.internet().emailAddress());
		putDto.getPerson().setFirstname(putDto.getPerson().getFirstname() + " - updated");
		putDto.getPerson().setLastname(putDto.getPerson().getLastname() + " - updated");
		
		// Act
		HttpEntity<PutMemberDto> httpPutEntity = HttpUtils.createHttpEntity(role, user, putDto);
		response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(memberPutURI, putDto.getBrandUuid(), putDto.getUuid())), port, null),
				HttpMethod.PUT, httpPutEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put error: %s", response.getStatusCode()));
	}
}
