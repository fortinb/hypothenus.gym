package com.iso.hypo.admin.papi;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.TestPropertySource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iso.hypo.admin.papi.dto.enumeration.RoleEnum;
import com.iso.hypo.admin.papi.dto.model.UserDto;
import com.iso.hypo.admin.papi.dto.patch.PatchMemberDto;
import com.iso.hypo.admin.papi.dto.patch.PatchUserDto;
import com.iso.hypo.admin.papi.dto.post.PostBrandDto;
import com.iso.hypo.admin.papi.dto.post.PostMemberDto;
import com.iso.hypo.admin.papi.dto.post.PostUserDto;
import com.iso.hypo.admin.papi.dto.put.PutMemberDto;
import com.iso.hypo.admin.papi.dto.put.PutUserDto;
import com.iso.hypo.domain.BrandBuilder;
import com.iso.hypo.domain.MemberBuilder;
import com.iso.hypo.domain.UserBuilder;
import com.iso.hypo.domain.aggregate.Brand;
import com.iso.hypo.domain.aggregate.Member;
import com.iso.hypo.domain.aggregate.User;
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
@TestPropertySource(properties = "app.test.run=true")
@TestInstance(Lifecycle.PER_CLASS)
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

		brand = BrandBuilder.build(codeBrand_1, faker.company().name());
		brandRepository.save(brand);
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
	
	@Test
	void testPostUserSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostUserDto postDto = modelMapper.map(UserBuilder.build(), PostUserDto.class);
		postDto.setRoles(new ArrayList<RoleEnum>());
		postDto.getRoles().add(RoleEnum.member);
		HttpEntity<PostUserDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(userPostURI), port, null), HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testGetUserSuccess(String role, String userName) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostUserDto postDto = modelMapper.map(UserBuilder.build(), PostUserDto.class);
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
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPutUserSuccess(String role, String userName) throws JsonProcessingException, MalformedURLException {
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
				HttpUtils.createURL(URI.create(String.format(userPutURI, updatedUser.getUuid())), port, null),
				HttpMethod.PUT, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPatchUserSuccess(String role, String userName) throws JsonProcessingException, MalformedURLException {
		// Arrange
		User userToPatch = UserBuilder.build();
		userToPatch.setActive(true);
		userToPatch = userRepository.save(userToPatch);

		PatchUserDto patchDto = modelMapper.map(userToPatch, PatchUserDto.class);
		patchDto.setFirstname(null);

		// Act
		HttpEntity<PatchUserDto> httpEntity = HttpUtils.createHttpEntity(role, userName, patchDto);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(userPatchURI, userToPatch.getUuid())), port, null),
				HttpMethod.PATCH, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));
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

		httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, null);
		response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(userGetURI, userToDeleteDto.getUuid())), port, null),
				HttpMethod.GET, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));
	}

	@Test
	void testPostMemberSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostMemberDto postDto = modelMapper.map(MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular), PostMemberDto.class);
		HttpEntity<PostMemberDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(String.format(memberPostURI, brand.getUuid())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
	}
	
	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPutMemberSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Member updatedMember = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
		updatedMember.setActive(true);
		updatedMember = memberRepository.save(updatedMember);

		PutMemberDto putDto = modelMapper.map(updatedMember, PutMemberDto.class);
		// mutate mutable fields
		putDto.getPerson().setEmail(faker.internet().emailAddress());
		putDto.getPerson().setFirstname(putDto.getPerson().getFirstname() + " - updated");
		putDto.getPerson().setLastname(putDto.getPerson().getLastname() + " - updated");
		if (putDto.getPerson().getAddress() != null) {
			putDto.getPerson().getAddress().setStreetName(faker.address().streetName());
		}
		if (putDto.getPerson().getPhoneNumbers() != null && putDto.getPerson().getPhoneNumbers().size() > 0) {
			putDto.getPerson().getPhoneNumbers().remove(0);
		}
		if (putDto.getPerson().getContacts() != null && putDto.getPerson().getContacts().size() > 1) {
			putDto.getPerson().getContacts().remove(1);
			putDto.getPerson().getContacts().get(0).setLastname("Updated" + faker.name().lastName());
		} else if (putDto.getPerson().getContacts() != null && putDto.getPerson().getContacts().size() == 1) {
			putDto.getPerson().getContacts().get(0).setLastname("Updated" + faker.name().lastName());
		}

		// Act
		HttpEntity<PutMemberDto> httpEntity = HttpUtils.createHttpEntity(role, user, putDto);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(memberPutURI, updatedMember.getBrandUuid(), updatedMember.getUuid())), port, null),
				HttpMethod.PUT, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put error: %s", response.getStatusCode()));
	}
	
	@ParameterizedTest
    @CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
    void testPatchMemberSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
        // Arrange
        Member memberToPatch = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
        memberToPatch.setActive(true);
        memberToPatch = memberRepository.save(memberToPatch);

        PatchMemberDto patchDto = modelMapper.map(memberToPatch, PatchMemberDto.class);
        patchDto.getPerson().setDateOfBirth(null);
        patchDto.getPerson().getAddress().setStreetName(null);
        patchDto.getPerson().setLastname(faker.name().lastName());
        patchDto.setPreferredGymUuid(null);
        
        memberToPatch.getPerson().setLastname(patchDto.getPerson().getLastname());

        // Act
        HttpEntity<PatchMemberDto> httpEntity = HttpUtils.createHttpEntity(role, user, patchDto);
        ResponseEntity<JsonNode> response = testRestTemplate.exchange(
                HttpUtils.createURL(URI.create(String.format(memberPatchURI, brand.getUuid(), patchDto.getUuid())), port, null),
                HttpMethod.PATCH, httpEntity, JsonNode.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(), String.format("Get error: %s", response.getStatusCode()));
    }
	
	@Test
    void testDeleteMemberSuccess() throws JsonProcessingException, MalformedURLException {
        // Arrange
        Member memberToDelete = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
        memberToDelete = memberRepository.save(memberToDelete);

        // Act
        HttpEntity<PutMemberDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, null);
        ResponseEntity<JsonNode> response = testRestTemplate.exchange(
                HttpUtils.createURL(URI.create(String.format(memberDeleteURI, brand.getUuid(), memberToDelete.getUuid())), port, null),
                HttpMethod.DELETE, httpEntity, JsonNode.class);

        Assertions.assertEquals(HttpStatus.ACCEPTED, response.getStatusCode(), String.format("Member delete error: %s", response.getStatusCode()));

        httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, null);
        response = testRestTemplate.exchange(
                HttpUtils.createURL(URI.create(String.format(memberGetURI, brand.getUuid(), memberToDelete.getUuid())), port, null),
                HttpMethod.GET, httpEntity, JsonNode.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), String.format("Get error: %s", response.getStatusCode()));
    }
}
