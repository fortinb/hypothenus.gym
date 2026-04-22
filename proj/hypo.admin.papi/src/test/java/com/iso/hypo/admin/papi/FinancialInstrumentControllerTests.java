package com.iso.hypo.admin.papi;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

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
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iso.hypo.admin.papi.dto.ErrorDto;
import com.iso.hypo.admin.papi.dto.model.MemberDto;
import com.iso.hypo.admin.papi.dto.post.PostFinancialInstrumentDto;
import com.iso.hypo.admin.papi.dto.put.PutMemberDto;
import com.iso.hypo.brand.domain.model.Brand;
import com.iso.hypo.brand.domain.repository.BrandRepository;
import com.iso.hypo.common.application.security.Roles;
import com.iso.hypo.domain.BrandBuilder;
import com.iso.hypo.domain.FinancialInstrumentBuilder;
import com.iso.hypo.domain.MemberBuilder;
import com.iso.hypo.finance.application.dto.FinancialInstrumentDto;
import com.iso.hypo.finance.domain.model.FinancialInstrument;
import com.iso.hypo.finance.domain.model.enumeration.FinancialInstrumentTypeEnum;
import com.iso.hypo.finance.domain.repository.FinancialInstrumentRepository;
import com.iso.hypo.membership.domain.exception.MemberException;
import com.iso.hypo.membership.domain.model.Member;
import com.iso.hypo.membership.domain.model.enumeration.MemberTypeEnum;
import com.iso.hypo.membership.domain.repository.MemberRepository;
import com.iso.hypo.tests.http.HttpUtils;
import com.iso.hypo.tests.security.Users;
import com.iso.hypo.tests.utils.TestResponseUtils;

import net.datafaker.Faker;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "app.test.run=true")
@TestInstance(Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class FinancialInstrumentControllerTests {

	public static final String listURI = "/v1/brands/%s/members/%s/financial/instruments";
	public static final String postURI = "/v1/brands/%s/members/%s/financial/instruments";
	public static final String deleteURI = "/v1/brands/%s/members/%s/financial/instruments/%s";
	public static final String postActivateURI = "/v1/brands/%s/members/%s/financial/instruments/%s/activate";
	public static final String postDeactivateURI = "/v1/brands/%s/members/%s/financial/instruments/%s/deactivate";

	public static final String pageNumber = "page";
	public static final String pageSize = "pageSize";

	public static final String brandCode = "FIBrand1";

	@LocalServerPort
	private int port;

	@Autowired
	BrandRepository brandRepository;
	@Autowired
	MemberRepository memberRepository;
	@Autowired
	FinancialInstrumentRepository financialInstrumentRepository;
	@Autowired
	ObjectMapper objectMapper;
	@Autowired
	ModelMapper modelMapper;

	private Faker faker = new Faker();

	private TestRestTemplate testRestTemplate = new TestRestTemplate();
	private FinancialInstrument financialInstrumentDeleted;
	private Member member;
	private Brand brand;

	@BeforeAll
	void arrange() {
		testRestTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
		memberRepository.deleteAll();

		brand = BrandBuilder.build(brandCode, faker.company().name());
		brandRepository.save(brand);

		// create members to assign preferredMemberUuid
		member = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
		memberRepository.save(member);
		for (int i = 0; i < 2; i++) {
			FinancialInstrument item = FinancialInstrumentBuilder.build(brand.getUuid(), member);
			item.setActive(true);
			financialInstrumentRepository.save(item);
		}

		financialInstrumentDeleted = FinancialInstrumentBuilder.build(brand.getUuid(), member);
		financialInstrumentDeleted.setActive(true);
		financialInstrumentDeleted.setDeleted(true);
		financialInstrumentRepository.save(financialInstrumentDeleted);
	}

	@AfterAll
	void cleanup() {
		// Cleanup if needed
	}

	@Test
	void testListFirstPageSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		HttpEntity<String> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, null);

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "0");
		params.add(pageSize, "1000");

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(listURI, brand.getUuid(), member.getUuid())), port, params),
				HttpMethod.GET, httpEntity, JsonNode.class);

		// Assert
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		// Assert
		Page<MemberDto> page = TestResponseUtils.toPage(response, new TypeReference<Page<MemberDto>>() {
		}, objectMapper);
		Assertions.assertEquals(0, page.getPageable().getPageNumber(), String
				.format("Financial Instrument list first page number invalid: %d", page.getPageable().getPageNumber()));
		Assertions.assertEquals(2, page.getNumberOfElements(), String.format(
				"Financial Instrument list first page number of elements invalid: %d", page.getNumberOfElements()));
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis", "member, Guillaume Fortin" })
	void testPostSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		Member postMember = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
		memberRepository.save(postMember);

		PostFinancialInstrumentDto postDto = modelMapper
				.map(FinancialInstrumentBuilder.build(brand.getUuid(), postMember), PostFinancialInstrumentDto.class);
		HttpEntity<PostFinancialInstrumentDto> httpEntity = HttpUtils.createHttpEntity(role, user, postDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(postURI, brand.getUuid(), postMember.getUuid())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));

		FinancialInstrumentDto createdDto = TestResponseUtils.toDto(response, FinancialInstrumentDto.class,
				objectMapper);
		assertFinancialInstrument(modelMapper.map(postDto, FinancialInstrumentDto.class), createdDto);
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis", "member, Guillaume Fortin" })
	void testPostFailureForbiddenBrandMismatch(String role, String user)
			throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		Member member = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
		memberRepository.save(member);

		PostFinancialInstrumentDto postDto = modelMapper.map(FinancialInstrumentBuilder.build(brand.getUuid(), member),
				PostFinancialInstrumentDto.class);
		HttpEntity<PostFinancialInstrumentDto> httpEntity = HttpUtils.createHttpEntity(role, user, postDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(postURI, faker.code().isbn10(), member.getUuid())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis", "member, Guillaume Fortin" })
	void testPostFailureForbiddenMemberMismatch(String role, String user)
			throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		Member member = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
		memberRepository.save(member);

		PostFinancialInstrumentDto postDto = modelMapper.map(FinancialInstrumentBuilder.build(brand.getUuid(), member),
				PostFinancialInstrumentDto.class);
		HttpEntity<PostFinancialInstrumentDto> httpEntity = HttpUtils.createHttpEntity(role, user, postDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(postURI, brand.getUuid(), faker.code().isbn10())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testActivateSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Member member = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
		member.setActive(false);
		member.setActivatedOn(null);
		member = memberRepository.save(member);

		FinancialInstrument financialInstrumentToActivate = FinancialInstrumentBuilder.build(brand.getUuid(), member);
		financialInstrumentToActivate.setActive(false);
		financialInstrumentToActivate.setActivatedOn(null);
		financialInstrumentToActivate.setDeactivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		financialInstrumentRepository.save(financialInstrumentToActivate);

		// Act
		HttpEntity<PutMemberDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postActivateURI, brand.getUuid(), member.getUuid(),
						financialInstrumentToActivate.getUuid())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Financial Instrument activation error: %s", response.getStatusCode()));

		FinancialInstrumentDto activated = TestResponseUtils.toDto(response, FinancialInstrumentDto.class,
				objectMapper);
		Assertions.assertEquals(true, activated.isActive(), "Financial Instrument should be active");
		Assertions.assertNotNull(activated.getActivatedOn(), "ActivationOn is null");
		assertFinancialInstrument(modelMapper.map(financialInstrumentToActivate, FinancialInstrumentDto.class),
				activated);
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testActivateFailureNotFound(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange

		// Act
		HttpEntity<PutMemberDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(
				URI.create(String.format(postActivateURI, brand.getUuid(), member.getUuid(), faker.code().isbn10())),
				port, null), HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Financial Instrument activation error: %s", response.getStatusCode()));

		if (response.getBody() != null && response.getBody().size() > 0) {
			ErrorDto err = TestResponseUtils.toError(response, objectMapper);
			Assertions.assertEquals(MemberException.FINANCIAL_INSTRUMENT_NOT_FOUND, err.getCode());
		}
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testDeactivateSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Member member = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
		member.setActive(true);
		member = memberRepository.save(member);

		FinancialInstrument financialInstrumentToDeactivate = FinancialInstrumentBuilder.build(brand.getUuid(), member);
		financialInstrumentToDeactivate.setActive(false);
		financialInstrumentToDeactivate.setActivatedOn(null);
		financialInstrumentToDeactivate.setDeactivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		financialInstrumentRepository.save(financialInstrumentToDeactivate);

		// Act
		HttpEntity<PutMemberDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postDeactivateURI, brand.getUuid(), member.getUuid(),
						financialInstrumentToDeactivate.getUuid())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Financial Instrument activation error: %s", response.getStatusCode()));

		FinancialInstrumentDto activated = TestResponseUtils.toDto(response, FinancialInstrumentDto.class,
				objectMapper);
		Assertions.assertEquals(false, activated.isActive(), "Financial Instrument should be inactive");
		Assertions.assertNotNull(activated.getDeactivatedOn(), "DeactivationOn is null");
		assertFinancialInstrument(modelMapper.map(financialInstrumentToDeactivate, FinancialInstrumentDto.class),
				activated);
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testDeactivateFailure(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange

		// Act
		HttpEntity<PutMemberDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(
				URI.create(String.format(postDeactivateURI, brand.getUuid(), member.getUuid(), faker.code().isbn10())),
				port, null), HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Financial Instrument activation error: %s", response.getStatusCode()));

		if (response.getBody() != null && response.getBody().size() > 0) {
			ErrorDto err = TestResponseUtils.toError(response, objectMapper);
			Assertions.assertEquals(MemberException.FINANCIAL_INSTRUMENT_NOT_FOUND, err.getCode());
		}
	}

	@Test
	void testDeleteSuccess() throws JsonProcessingException, MalformedURLException {
		// Arrange
		Member member = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
		member.setActive(false);
		member.setActivatedOn(null);
		member = memberRepository.save(member);

		FinancialInstrument financialInstrumentToDelete = FinancialInstrumentBuilder.build(brand.getUuid(), member);
		financialInstrumentToDelete.setActive(true);
		financialInstrumentToDelete.setActivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
		financialInstrumentRepository.save(financialInstrumentToDelete);

		// Act
		HttpEntity<PutMemberDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, null);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(deleteURI, brand.getUuid(), member.getUuid(),
						financialInstrumentToDelete.getUuid())), port, null),
				HttpMethod.DELETE, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.ACCEPTED, response.getStatusCode(),
				String.format("Financial Instrument delete error: %s", response.getStatusCode()));
	}

	public static final void assertFinancialInstrument(FinancialInstrumentDto expected, FinancialInstrumentDto result) {
		if (expected.getUuid() != null) {
			Assertions.assertEquals(expected.getUuid(), result.getUuid());
		}

		Assertions.assertEquals(expected.getBrandUuid(), result.getBrandUuid());
		Assertions.assertEquals(expected.getMemberUuid(), result.getMemberUuid());
		Assertions.assertEquals(expected.getType(), result.getType());

		// preferredMemberUuid may be null in some scenarios
		if (expected.getType() == FinancialInstrumentTypeEnum.creditCard) {
			Assertions.assertNotNull(result.getCreditCard());
			Assertions.assertEquals(expected.getCreditCard().getCardHolderName(),
					result.getCreditCard().getCardHolderName());
			// Assertions.assertEquals(expected.getCreditCard().getCardNumber(),
			// result.getCreditCard().getCardNumber());
			Assertions.assertEquals(expected.getCreditCard().getCvv(), result.getCreditCard().getCvv());
			Assertions.assertEquals(expected.getCreditCard().getExpirationDate(),
					result.getCreditCard().getExpirationDate());
		}
	}
}
