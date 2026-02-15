package com.iso.hypo.admin.papi;

import java.net.MalformedURLException;
import java.net.URI;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
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
import com.iso.hypo.admin.papi.dto.model.BrandDto;
import com.iso.hypo.admin.papi.dto.post.PostBrandDto;
import com.iso.hypo.domain.BrandBuilder;
import com.iso.hypo.domain.aggregate.Brand;
import com.iso.hypo.domain.security.Roles;
import com.iso.hypo.repositories.BrandRepository;
import com.iso.hypo.repositories.CoachRepository;
import com.iso.hypo.repositories.CourseRepository;
import com.iso.hypo.repositories.GymRepository;
import com.iso.hypo.repositories.MemberRepository;
import com.iso.hypo.repositories.MembershipPlanRepository;
import com.iso.hypo.repositories.UserRepository;
import com.iso.hypo.services.BrandService;
import com.iso.hypo.services.clients.AzureGraphClientService;
import com.iso.hypo.services.mappers.BrandMapper;
import com.iso.hypo.tests.data.Populator;
import com.iso.hypo.tests.http.HttpUtils;
import com.iso.hypo.tests.security.Users;
import com.iso.hypo.tests.utils.TestResponseUtils;

import net.datafaker.Faker;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "app.test.run=false")
@TestInstance(Lifecycle.PER_CLASS)
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
	UserRepository userRepository;
	@Autowired
	ObjectMapper objectMapper;
	@Autowired
	BrandService brandService;
	@Autowired
	BrandMapper brandMapper;
	@Autowired
	AzureGraphClientService azureGraphClientService;
	@Autowired
	ModelMapper modelMapper;
	
	private RestTemplateBuilder restTemplateBuilder;
	private TestRestTemplate testRestTemplate;
	private Faker faker = new Faker();

	@Test
	void populator() throws MalformedURLException, JsonProcessingException, Exception {
		restTemplateBuilder = new RestTemplateBuilder()
				.additionalMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper));
		testRestTemplate = new TestRestTemplate(restTemplateBuilder);
	
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

		for (int i = 0; i < 10; i++) {
			Brand item = BrandBuilder.build(faker.code().isbn10(), faker.company().name());
			brandRepository.save(item);
		}

		for (int i = 0; i < 5; i++) {
			Brand item = BrandBuilder.build(faker.code().isbn10(), faker.company().name());
			item.setActive(false);
			brandRepository.save(item);
		}

		Populator populator = new Populator( gymRepository, coachRepository, courseRepository,	membershipPlanRepository, memberRepository);
		
		// Arrange
		PostBrandDto postBrandDto = modelMapper.map(BrandBuilder.build("crossfitextreme", "Crossfit Extreme"), PostBrandDto.class);
		HttpEntity<PostBrandDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postBrandDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(postBrandURI), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));

		BrandDto createdBrandDto = TestResponseUtils.toDto(response, BrandDto.class, objectMapper);
		populator.populateFullBrand(createdBrandDto);
		
		postBrandDto = modelMapper.map(BrandBuilder.build("fitnessboxing", "Fitness Boxing"), PostBrandDto.class);
		httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postBrandDto);

		// Act
		response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(postBrandURI), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));

		createdBrandDto = TestResponseUtils.toDto(response, BrandDto.class, objectMapper);
		populator.populateFullBrand(createdBrandDto);
	}

}

