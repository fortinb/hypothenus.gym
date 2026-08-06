package com.iso.hypo.admin.papi;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
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
import com.iso.hypo.admin.papi.dto.model.UserDto;
import com.iso.hypo.admin.papi.dto.post.PostBrandDto;
import com.iso.hypo.admin.papi.dto.post.PostUserDto;
import com.iso.hypo.brand.application.mapper.BrandDtoMapper;
import com.iso.hypo.brand.application.usecase.BrandService;
import com.iso.hypo.brand.domain.model.Brand;
import com.iso.hypo.brand.domain.repository.BrandRepository;
import com.iso.hypo.brand.domain.repository.CoachRepository;
import com.iso.hypo.brand.domain.repository.CourseRepository;
import com.iso.hypo.brand.domain.repository.GymRepository;
import com.iso.hypo.brand.domain.repository.UserRepository;
import com.iso.hypo.common.application.security.RoleEnum;
import com.iso.hypo.common.application.security.Roles;
import com.iso.hypo.common.application.usecase.AzureGraphClientService;
import com.iso.hypo.domain.BrandBuilder;
import com.iso.hypo.domain.UserBuilder;
import com.iso.hypo.finance.domain.repository.FinancialInstrumentRepository;
import com.iso.hypo.finance.domain.repository.PaymentRepository;
import com.iso.hypo.membership.domain.repository.MemberRepository;
import com.iso.hypo.membership.domain.repository.MembershipPlanRepository;
import com.iso.hypo.sale.domain.repository.OrderRepository;
import com.iso.hypo.tests.data.Populator;
import com.iso.hypo.tests.http.HttpUtils;
import com.iso.hypo.tests.security.Users;
import com.iso.hypo.tests.utils.RetryUtils;
import com.microsoft.graph.models.User;

import net.datafaker.Faker;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper.Builder;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "app.test.run=false")
@TestInstance(Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@Tag("populator")
class PopulatorTests {
	public static final String postBrandURI = "/v1/brands";
	@LocalServerPort
	private int port;

	@Autowired
	BrandRepository brandRepository;
	@Autowired
	GymRepository gymRepository;
	@Autowired
	CoachRepository coachRepository;
	@Autowired
	CourseRepository courseRepository;
	@Autowired
	MembershipPlanRepository membershipPlanRepository;
	@Autowired
	MemberRepository memberRepository;
	@Autowired
	FinancialInstrumentRepository financialInstrumentRepository;
	@Autowired
	OrderRepository orderRepository;
	@Autowired
	PaymentRepository paymentRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	Builder objectMapper;
	@Autowired
	BrandService brandService;
	@Autowired
	BrandDtoMapper brandMapper;
	@Autowired
	AzureGraphClientService azureGraphClientService;
	@Autowired
	ModelMapper modelMapper;
	
	private RestTestClient restClient;
	
	private Faker faker = new Faker();

	@Test
	void populator() throws MalformedURLException, JsonProcessingException, Exception {
    	restClient = RestTestClient.bindToServer()
		        .baseUrl("http://localhost:" + port)
		        .configureMessageConverters(converters -> 
		        	converters.addCustomConverter(
		        			new JacksonJsonHttpMessageConverter(
		        			objectMapper
		        			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
		        			.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false))))
		        .build();
	
		try {
			azureGraphClientService.deleteAllUser();
			azureGraphClientService.deleteAllGroup();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		userRepository.deleteAll();
		brandRepository.deleteAll();
		gymRepository.deleteAll();
		coachRepository.deleteAll();
		courseRepository.deleteAll();
		memberRepository.deleteAll();
		membershipPlanRepository.deleteAll();
		financialInstrumentRepository.deleteAll();
		orderRepository.deleteAll();
		paymentRepository.deleteAll();
		
		// Admin user is required
		UserDto adminUserDto = createAdminUser();

		// Brands
		for (int i = 0; i < 10; i++) {
			Brand item = BrandBuilder.build(faker.code().isbn10(), faker.company().name());
			brandRepository.save(item);
		}

		for (int i = 0; i < 5; i++) {
			Brand item = BrandBuilder.build(faker.code().isbn10(), faker.company().name());
			item.setActive(false);
			brandRepository.save(item);
		}
		
		// Full Brands with all related entities is required for testing
		Populator populator = new Populator( gymRepository, coachRepository, courseRepository,	membershipPlanRepository, memberRepository, modelMapper, restClient, port);
		
		// Create Brand #1
		PostBrandDto postBrandDto = modelMapper.map(BrandBuilder.build("crossfitextreme", "Crossfit Extreme"), PostBrandDto.class);
		
		// Act
		BrandDto createdBrandDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(postBrandURI), port, null))			
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(postBrandDto)
					.exchange() 
				    .expectStatus().isCreated() 
					.expectBody(BrandDto.class) 
					.returnResult()
				    .getResponseBody();

		populator.populateFullBrand(createdBrandDto, adminUserDto);
		
		// Create Brand #2
		postBrandDto = modelMapper.map(BrandBuilder.build("fitnessboxing", "Fitness Boxing"), PostBrandDto.class);
		
		createdBrandDto = 
				this.restClient.post()
					.uri(HttpUtils.createURL(URI.create(postBrandURI), port, null))			
					.headers(h -> h.addAll(HttpUtils.createHttpHeaders(Roles.Admin, Users.Admin)))
					.accept(org.springframework.http.MediaType.APPLICATION_JSON)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(postBrandDto)
					.exchange() 
				    .expectStatus().isCreated() 
					.expectBody(BrandDto.class) 
					.returnResult()
				    .getResponseBody();
		
		populator.populateFullBrand(createdBrandDto, adminUserDto);
	}

	private UserDto createAdminUser() throws JsonProcessingException, MalformedURLException {
		// Wait for user deletion to propagate in Azure AD before attempting to create the user again.
		try {
			RetryUtils.retryUntil(10, 1,
					() -> {
						Optional<User> idpUser;
						try {
							idpUser = azureGraphClientService.userExists("fortinb@videotron.ca");
							return idpUser.isEmpty();
						} catch (Exception e) {
							e.printStackTrace();
							return false;
						}
					},
					null);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Arrange
		final String userPostURI = "/v1/users/admin";
		
		PostUserDto postDto = modelMapper.map(UserBuilder.build(), PostUserDto.class);
		postDto.setFirstname("Bruno");
		postDto.setLastname("Fortin");
		postDto.setEmail("fortinb@videotron.ca");
		postDto.setRoles(new ArrayList<RoleEnum>());
		postDto.getRoles().add(RoleEnum.admin);
		postDto.getRoles().add(RoleEnum.manager);

		// Act
		UserDto createdUserDto = 
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
		
		return createdUserDto;
	}
	
}