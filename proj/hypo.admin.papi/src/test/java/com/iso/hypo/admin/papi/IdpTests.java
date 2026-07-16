package com.iso.hypo.admin.papi;

import static org.awaitility.Awaitility.await;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

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
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.iso.hypo.admin.papi.dto.model.BrandDto;
import com.iso.hypo.admin.papi.dto.model.MemberDto;
import com.iso.hypo.admin.papi.dto.model.UserDto;
import com.iso.hypo.admin.papi.dto.post.PostBrandDto;
import com.iso.hypo.admin.papi.dto.post.PostMemberDto;
import com.iso.hypo.admin.papi.dto.post.PostUserDto;
import com.iso.hypo.admin.papi.dto.put.PutMemberDto;
import com.iso.hypo.admin.papi.dto.put.PutUserDto;
import com.iso.hypo.brand.application.exception.UserException;
import com.iso.hypo.brand.application.mapper.UserDtoMapper;
import com.iso.hypo.brand.application.usecase.UserService;
import com.iso.hypo.brand.domain.model.Brand;
import com.iso.hypo.brand.domain.repository.BrandRepository;
import com.iso.hypo.brand.domain.repository.UserRepository;
import com.iso.hypo.common.application.security.RoleEnum;
import com.iso.hypo.common.application.security.Roles;
import com.iso.hypo.domain.BrandBuilder;
import com.iso.hypo.domain.MemberBuilder;
import com.iso.hypo.domain.UserBuilder;
import com.iso.hypo.membership.domain.model.enumeration.MemberTypeEnum;
import com.iso.hypo.membership.domain.repository.MemberRepository;
import com.iso.hypo.tests.http.HttpUtils;
import com.iso.hypo.tests.security.Users;

import net.datafaker.Faker;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper.Builder;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "app.test.run=false")
@TestInstance(Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class IdpTests {

	public static final String postBrandURI = "/v1/brands";
	
	public static final String userPostURI = "/v1/users/admin";
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
	UserDtoMapper userMapper;
	@Autowired
	Builder objectMapper;
	@Autowired
	ModelMapper modelMapper;

	private Faker faker = new Faker();

	private RestTestClient restClient;
	
	private Brand brand;

	public static final String codeBrand_1 = "Brand1";
	
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

		try {
			PostBrandDto postDto = modelMapper.map(BrandBuilder.build(faker.code().isbn10(),faker.company().name()), PostBrandDto.class);
			
			// Act
			BrandDto createdDto = 
					this.restClient.post()
						.uri(HttpUtils.createURL(URI.create(postBrandURI), port, null))				
						.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
						.accept(org.springframework.http.MediaType.APPLICATION_JSON)
						.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
						.body(postDto)
						.exchange() 
					    .expectStatus().isCreated() 
						.expectBody(BrandDto.class) 
						.returnResult()
					    .getResponseBody();
			
			brand = modelMapper.map(createdDto, Brand.class);
		} catch (Exception e) {
			 Assertions.fail(String.format("Error during creation of brand: %s", e.getMessage()));
			 return;
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
		
		// Act
		BrandDto _ = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(postBrandURI), port, null))				
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(postDto)
					.exchange() 
				    .expectStatus().isCreated() 
					.expectBody(BrandDto.class) 
					.returnResult()
				    .getResponseBody();
	}
	
	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin" })
	void testPostUserSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostUserDto postDto = modelMapper.map(UserBuilder.build(), PostUserDto.class);
		postDto.setRoles(new ArrayList<RoleEnum>());
		postDto.getRoles().add(role.equals("admin") ? RoleEnum.admin : RoleEnum.manager);

		// Act
		UserDto _ = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(userPostURI), port, null))			
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(postDto)
					.exchange() 
				    .expectStatus().isCreated() 
					.expectBody(UserDto.class) 
					.returnResult()
				    .getResponseBody();
	}
	
	@Test
	void testPutUserRoleAssignmenNotAllowed() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostUserDto postDto = modelMapper.map(UserBuilder.build(), PostUserDto.class);
		postDto.setRoles(new ArrayList<RoleEnum>());
		postDto.getRoles().add(RoleEnum.manager);
		postDto.getRoles().add(RoleEnum.member);

		UserDto createdDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(userPostURI), port, null))			
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(postDto)
					.exchange() 
				    .expectStatus().isCreated() 
					.expectBody(UserDto.class) 
					.returnResult()
				    .getResponseBody();

		PutUserDto putDto = modelMapper.map(createdDto, PutUserDto.class);
		putDto.getRoles().add(RoleEnum.admin);

		// Act
		UserDto updatedDto = 
				this.restClient.put()
					.uri(HttpUtils.createURL(URI.create(String.format(userPutURI, createdDto.getUuid())), port, null))		
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Manager, Users.Manager)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(putDto)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(UserDto.class) 
					.returnResult()
				    .getResponseBody();
		
		// Assert
		Assertions.assertTrue(updatedDto.getMessages().getFirst().getCode().equals(UserException.ROLE_ASSIGNMENT_NOT_ALLOWED));
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testGetUserSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostUserDto postDto = modelMapper.map(UserBuilder.build(), PostUserDto.class);
		postDto.setRoles(new ArrayList<RoleEnum>());
		postDto.getRoles().add(role.equals("admin") ? RoleEnum.admin : RoleEnum.manager);
		
		UserDto createdDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(userPostURI), port, null))			
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
					.uri(HttpUtils.createURL(URI.create(String.format(userGetURI, createdDto.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(UserDto.class) 
					.returnResult()
				    .getResponseBody();
		
		// Assert
		Assertions.assertTrue(fetchedDto.getFirstname().equals(createdDto.getFirstname()));
		Assertions.assertTrue(fetchedDto.getLastname().equals(createdDto.getLastname()));
		Assertions.assertTrue(fetchedDto.getEmail().equals(createdDto.getEmail()));
		Assertions.assertTrue(fetchedDto.getRoles().contains(RoleEnum.valueOf(role)));
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin" })
	void testPutUserSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		PostUserDto postDto = modelMapper.map(UserBuilder.build(), PostUserDto.class);
		postDto.setRoles(new ArrayList<RoleEnum>());
		postDto.getRoles().add(RoleEnum.admin);
		postDto.getRoles().add(RoleEnum.manager);
		
		UserDto createdDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(userPostURI), port, null))			
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(postDto)
					.exchange() 
				    .expectStatus().isCreated() 
					.expectBody(UserDto.class) 
					.returnResult()
				    .getResponseBody();

		PutUserDto putDto = modelMapper.map(createdDto, PutUserDto.class);
		putDto.getRoles().add(RoleEnum.member);
		putDto.getRoles().remove(RoleEnum.manager);

		// Act
		UserDto updatedDto = 
				this.restClient.put()
					.uri(HttpUtils.createURL(URI.create(String.format(userPutURI, createdDto.getUuid())), port, null))
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
		Assertions.assertTrue(updatedDto.getRoles().contains(RoleEnum.admin));
		Assertions.assertTrue(updatedDto.getRoles().contains(RoleEnum.member));
		Assertions.assertFalse(updatedDto.getRoles().contains(RoleEnum.manager));
		
	}

	@Test
	void testDeleteUserSuccess() throws JsonProcessingException, MalformedURLException {
		// Arrange
		PostUserDto postDto = modelMapper.map(UserBuilder.build(), PostUserDto.class);
		
		UserDto createdDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(userPostURI), port, null))			
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
		await()
        .atMost(20, TimeUnit.SECONDS)
        .pollInterval(500, TimeUnit.MILLISECONDS)
        .untilAsserted(() -> {
        	
    		// Act
    		UserDto _ = 
    				this.restClient.delete()
    					.uri(HttpUtils.createURL(URI.create(String.format(userDeleteURI, createdDto.getUuid())), port, null))
    					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
    					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
    					.exchange() 
    				    .expectStatus().isAccepted() 
    					.expectBody(UserDto.class) 
    					.returnResult()
    				    .getResponseBody(); 
			});
	}

	@Test
	void testPostMemberSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostMemberDto postDto = modelMapper.map(MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular), PostMemberDto.class);
		postDto.setPassword("admin.test.1");
		
		// Act
		MemberDto _ = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(memberPostURI, brand.getUuid())), port, null))	
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(postDto)
					.exchange() 
				    .expectStatus().isCreated() 
					.expectBody(MemberDto.class) 
					.returnResult()
				    .getResponseBody();
	}
	
	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPutMemberSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {

		PostMemberDto postDto = modelMapper.map(MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular), PostMemberDto.class);
		postDto.setPassword("admin.test.1");
		
		MemberDto createdDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(String.format(memberPostURI, brand.getUuid())), port, null))		
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(postDto)
					.exchange() 
				    .expectStatus().isCreated() 
					.expectBody(MemberDto.class) 
					.returnResult()
				    .getResponseBody();
		
		PutMemberDto putDto = modelMapper.map(createdDto, PutMemberDto.class);
		putDto.getPerson().setEmail(faker.internet().emailAddress());
		putDto.getPerson().setFirstname(putDto.getPerson().getFirstname() + " - updated");
		putDto.getPerson().setLastname(putDto.getPerson().getLastname() + " - updated");
		
		// Act
		MemberDto _ = 
				this.restClient.put()
					.uri(HttpUtils.createURL(URI.create(String.format(memberPutURI, putDto.getBrandUuid(), putDto.getUuid())), port, null))
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(role, user)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(putDto)
					.exchange() 
				    .expectStatus().isOk() 
					.expectBody(MemberDto.class) 
					.returnResult()
				    .getResponseBody();
	}
}
