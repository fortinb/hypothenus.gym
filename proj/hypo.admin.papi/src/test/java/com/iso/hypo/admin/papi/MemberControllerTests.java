package com.iso.hypo.admin.papi;

import static org.awaitility.Awaitility.await;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import com.iso.hypo.admin.papi.dto.contact.ContactDto;
import com.iso.hypo.admin.papi.dto.contact.PhoneNumberDto;
import com.iso.hypo.admin.papi.dto.model.MemberDto;
import com.iso.hypo.admin.papi.dto.patch.PatchMemberDto;
import com.iso.hypo.admin.papi.dto.post.PostMemberDto;
import com.iso.hypo.admin.papi.dto.put.PutMemberDto;
import com.iso.hypo.admin.papi.dto.search.MemberSearchDto;
import com.iso.hypo.domain.BrandBuilder;
import com.iso.hypo.domain.MemberBuilder;
import com.iso.hypo.domain.aggregate.Brand;
import com.iso.hypo.domain.aggregate.Member;
import com.iso.hypo.domain.enumeration.MemberTypeEnum;
import com.iso.hypo.repositories.BrandRepository;
import com.iso.hypo.repositories.MemberRepository;
import com.iso.hypo.services.exception.MemberException;
import com.iso.hypo.tests.http.HttpUtils;
import com.iso.hypo.domain.security.Roles;
import com.iso.hypo.tests.security.Users;
import com.iso.hypo.tests.utils.StringUtils;
import com.iso.hypo.tests.utils.TestResponseUtils;

import net.datafaker.Faker;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "app.test.run=true")
@TestInstance(Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class MemberControllerTests {

    public static final String searchURI = "/v1/brands/%s/members/search";
    public static final String listURI = "/v1/brands/%s/members";
    public static final String postURI = "/v1/brands/%s/members/register";
    public static final String getURI = "/v1/brands/%s/members/%s";
    public static final String putURI = "/v1/brands/%s/members/%s";
    public static final String patchURI = "/v1/brands/%s/members/%s";
    public static final String deleteURI = "/v1/brands/%s/members/%s";
    public static final String postActivateURI = "/v1/brands/%s/members/%s/activate";
    public static final String postDeactivateURI = "/v1/brands/%s/members/%s/deactivate";
    public static final String searchCriteria = "criteria";
    public static final String pageNumber = "page";
    public static final String pageSize = "pageSize";

    public static final String brandCode = "MemberBrand1";

    @LocalServerPort
    private int port;

    @Autowired
    BrandRepository brandRepository;
    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ModelMapper modelMapper;

    private Faker faker = new Faker();

    private TestRestTemplate testRestTemplate = new TestRestTemplate();
    private Member member;
    private Member memberIsDeleted;
    private Brand brand;
    private List<Member> members = new ArrayList<Member>();

    @BeforeAll
    void arrange() {
        testRestTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        memberRepository.deleteAll();

        brand = BrandBuilder.build(brandCode, faker.company().name());
        brandRepository.save(brand);

        // create members to assign preferredMemberUuid
        Member member1 = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
        memberRepository.save(member1);
        Member member2 = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
        memberRepository.save(member2);

        member = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
        member.setPreferredGymUuid(null);
        member.setActive(true);
        memberRepository.save(member);

        memberIsDeleted = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
        memberIsDeleted.setDeleted(true);
        memberIsDeleted = memberRepository.save(memberIsDeleted);

        for (int i = 0; i < 10; i++) {
            Member item = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
            item.setPreferredGymUuid(null);
            item.setActive(true);
            memberRepository.save(item);
            members.add(item);
        }

        for (int i = 0; i < 4; i++) {
            Member item = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
            item.setPreferredGymUuid(null);
            item.setActive(true);
            memberRepository.save(item);
            members.add(item);
        }

        Member item = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
        item.setPreferredGymUuid(null);
        item.setActive(false);
        memberRepository.save(item);
    }

    @AfterAll
    void cleanup() {
        // Cleanup if needed
    }
    
    @Test
	void testSearchAutocompleteIsDeletedSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(memberIsDeleted.getPerson().getFirstname(), 10);
		assertSearch(criteria,0,0);
	}
	
	@Test
	void testSearchAutocompleteFirstnameSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(member.getPerson().getFirstname(), 3);
		assertSearch(criteria,1,1000);
	}
	
	@Test
	void testSearchAutocompleteLastnameSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(member.getPerson().getLastname(), 3);
		assertSearch(criteria,1,1000);
	}


	@Test
	void testSearchAutocompletePhoneSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(member.getPerson().getPhoneNumbers().getFirst().getNumber(), 7);
		assertSearch(criteria,1,1000);
	}

	@Test
	void testSearchAutocompleteZipCodeSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(member.getPerson().getAddress().getZipCode(), 3);
		assertSearch(criteria,1,1000);
	}

	@Test
	void testSearchAutocompleteEmailSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Act
		String criteria = StringUtils.extractRandomWordPartial(member.getPerson().getEmail(), 3);
		assertSearch(criteria,1,1000);
	}

	@Test
	void testListFirstPageSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		HttpEntity<String> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, null);

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "0");
		params.add(pageSize, "4");

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(String.format(listURI, brand.getUuid())), port, params),
				HttpMethod.GET, httpEntity, JsonNode.class);

		// Assert
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		// Assert
		Page<MemberDto> page = TestResponseUtils.toPage(response, new TypeReference<Page<MemberDto>>() {}, objectMapper);
		Assertions.assertEquals(0, page.getPageable().getPageNumber(),
				String.format("Member list first page number invalid: %d", page.getPageable().getPageNumber()));
		Assertions.assertEquals(4, page.getNumberOfElements(),
				String.format("Member list first page number of elements invalid: %d", page.getNumberOfElements()));
	}

	@Test
	void testListSecondPageSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		HttpEntity<String> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, "");

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(pageNumber, "1");
		params.add(pageSize, "4");

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(String.format(listURI, brand.getUuid())), port, params),
				HttpMethod.GET, httpEntity, JsonNode.class);


		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		Page<MemberDto> page = TestResponseUtils.toPage(response, new TypeReference<Page<MemberDto>>() {}, objectMapper);

		// Assert
		Assertions.assertEquals(1, page.getPageable().getPageNumber(),
				String.format("Member list second page number invalid: %d", page.getPageable().getPageNumber()));
		Assertions.assertEquals(4, page.getNumberOfElements(),
				String.format("Member list second page number of elements invalid: %d", page.getNumberOfElements()));
	}

	@Test
	void testPostSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostMemberDto postDto = modelMapper.map(MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular), PostMemberDto.class);
		HttpEntity<PostMemberDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(String.format(postURI, brand.getUuid())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));

		MemberDto createdDto = TestResponseUtils.toDto(response, MemberDto.class, objectMapper);
		assertMember(modelMapper.map(postDto, MemberDto.class), createdDto);
	}
	
	@Test
	void testPostDuplicateFailure() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostMemberDto postDto = modelMapper.map(MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular), PostMemberDto.class);
		HttpEntity<PostMemberDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(String.format(postURI, brand.getUuid())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));

		// Act
		response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(String.format(postURI, brand.getUuid())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);
		
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
		
		MemberDto dupDto = TestResponseUtils.toDto(response, MemberDto.class, objectMapper);

		Assertions.assertEquals(1, dupDto.getMessages().size(),
				String.format("Duplicate error ,missing message: %s", dupDto.getMessages().size()));
		
		Assertions.assertEquals(MemberException.MEMBER_ALREADY_EXIST, dupDto.getMessages().getFirst().getCode(),
				String.format("Duplicate error, missing message: %s", dupDto.getMessages().getFirst().getCode()));
	}
	
	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPostFailureForbiddenBrandMismatch(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PostMemberDto postDto = modelMapper.map(MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular), PostMemberDto.class);
		HttpEntity<PostMemberDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(String.format(postURI, faker.code().isbn10())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
	}

    @ParameterizedTest
    @CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis", "member, Guillaume Fortin" })
    void testGetSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
        // Arrange
        PostMemberDto postDto = modelMapper.map(MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular), PostMemberDto.class);
        HttpEntity<PostMemberDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postDto);

        ResponseEntity<JsonNode> responsePost = testRestTemplate.exchange(
                HttpUtils.createURL(URI.create(String.format(postURI, brand.getUuid())), port, null), HttpMethod.POST,
                httpEntity, JsonNode.class);

        Assertions.assertEquals(HttpStatus.CREATED, responsePost.getStatusCode(),
                String.format("Post error: %s", responsePost.getStatusCode()));

        // Act
        httpEntity = HttpUtils.createHttpEntity(role, user, null);
        MemberDto createdDto = TestResponseUtils.toDto(responsePost, MemberDto.class, objectMapper);
        ResponseEntity<JsonNode> response = testRestTemplate.exchange(
                HttpUtils.createURL(URI.create(String.format(getURI, createdDto.getBrandUuid(), createdDto.getUuid())), port,
                        null), HttpMethod.GET, httpEntity, JsonNode.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(), 
        		String.format("Get error: %s", response.getStatusCode()));

        MemberDto fetchedDto = TestResponseUtils.toDto(response, MemberDto.class, objectMapper);
        assertMember(modelMapper.map(postDto, MemberDto.class), fetchedDto);
    }

    @Test
    void testGetFailureNotFound() throws MalformedURLException, JsonProcessingException, Exception {
        // Arrange
        HttpEntity<Object> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, null);
        ResponseEntity<JsonNode> response = testRestTemplate.exchange(
                HttpUtils.createURL(URI.create(String.format(getURI, brand.getUuid(), faker.code().isbn10())), port, null),
                HttpMethod.GET, httpEntity, JsonNode.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), String.format("Get error: %s", response.getStatusCode()));
    }


    @ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPutSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
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
				HttpUtils.createURL(URI.create(String.format(putURI, updatedMember.getBrandUuid(), updatedMember.getUuid())), port, null),
				HttpMethod.PUT, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put error: %s", response.getStatusCode()));

		MemberDto updatedDto = TestResponseUtils.toDto(response, MemberDto.class, objectMapper);
		assertMember(modelMapper.map(putDto, MemberDto.class), updatedDto);
	}
    
    @ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPutNullSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange
		Member updatedMember = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
		updatedMember.setActive(true);
		updatedMember = memberRepository.save(updatedMember);
		
		PutMemberDto putDto = modelMapper.map(updatedMember, PutMemberDto.class);
		
		putDto.getPerson().setEmail(null);
		putDto.getPerson().setAddress(null);
		putDto.getPerson().setPhoneNumbers(null);
		putDto.getPerson().setContacts(null);
		
		// Act
		HttpEntity<PutMemberDto> httpEntity = HttpUtils.createHttpEntity(role, user, putDto);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, updatedMember.getBrandUuid(), updatedMember.getUuid())), port, null),
				HttpMethod.PUT, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Put null error: %s", response.getStatusCode()));

	 	MemberDto updated = TestResponseUtils.toDto(response, MemberDto.class, objectMapper);
	 	assertMember(modelMapper.map(putDto, MemberDto.class), updated);
	}
    
	@Test
	void testPutFailureNotFound() throws MalformedURLException, JsonProcessingException, Exception {
		PutMemberDto putDto = modelMapper.map(MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular), PutMemberDto.class);
		HttpEntity<PutMemberDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, putDto);

		// Arrange
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, brand.getUuid(), putDto.getUuid())), port, null),
				HttpMethod.PUT, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));
	}
	
	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPutFailureForbiddenMemberMismatch(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PutMemberDto putDto = modelMapper.map(MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular), PutMemberDto.class);
		HttpEntity<PutMemberDto> httpEntity = HttpUtils.createHttpEntity(role, user, putDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, brand.getUuid(), faker.code().isbn10())), port, null),
				HttpMethod.PUT, httpEntity, JsonNode.class);
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
	}
	
	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPutFailureForbiddenBrandMismatch(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PutMemberDto putDto = modelMapper.map(MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular), PutMemberDto.class);
		HttpEntity<PutMemberDto> httpEntity = HttpUtils.createHttpEntity(role, user, putDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, faker.code().isbn10(), putDto.getUuid())), port, null),
				HttpMethod.PUT, httpEntity, JsonNode.class);
		
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
	}
    
    @ParameterizedTest
    @CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
    void testActivateSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
        // Arrange
        Member memberToActivate = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
        memberToActivate.setActive(false);
        memberToActivate.setActivatedOn(null);
        memberToActivate.setDeactivatedOn(null);
        memberToActivate = memberRepository.save(memberToActivate);
        memberToActivate.setActive(true);
        memberToActivate.setActivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
        memberToActivate.setDeactivatedOn(null);

        // Act
        HttpEntity<PutMemberDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
        ResponseEntity<JsonNode> response = testRestTemplate.exchange(
                HttpUtils.createURL(URI.create(String.format(postActivateURI, brand.getUuid(), memberToActivate.getUuid())), port, null),
                HttpMethod.POST, httpEntity, JsonNode.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(), String.format("Member activation error: %s", response.getStatusCode()));

        MemberDto activated = TestResponseUtils.toDto(response, MemberDto.class, objectMapper);
        assertMember(modelMapper.map(memberToActivate, MemberDto.class), activated);
    }
    
	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testActivateFailureNotFound(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange

		// Act
		HttpEntity<PutMemberDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(postActivateURI, brand.getUuid(), faker.code().isbn10())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);
											
		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Member activation error: %s", response.getStatusCode()));

		if (response.getBody() != null && response.getBody().size() > 0) {
			ErrorDto err = TestResponseUtils.toError(response, objectMapper);
			Assertions.assertEquals(MemberException.MEMBER_NOT_FOUND, err.getCode());
		}
	}

    @ParameterizedTest
    @CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
    void testDeactivateSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
        // Arrange
        Member memberToDeactivate = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
        memberToDeactivate.setActive(true);
        memberToDeactivate.setActivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));
        memberToDeactivate = memberRepository.save(memberToDeactivate);
        memberToDeactivate.setActive(false);
        memberToDeactivate.setDeactivatedOn(Instant.now().truncatedTo(ChronoUnit.DAYS));

        // Act
        HttpEntity<PutMemberDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
        ResponseEntity<JsonNode> response = testRestTemplate.exchange(
                HttpUtils.createURL(URI.create(String.format(postDeactivateURI, brand.getUuid(), memberToDeactivate.getUuid())), port, null),
                HttpMethod.POST, httpEntity, JsonNode.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(), String.format("Member deactivation error: %s", response.getStatusCode()));

        MemberDto deactivated = TestResponseUtils.toDto(response, MemberDto.class, objectMapper);
        assertMember(modelMapper.map(memberToDeactivate, MemberDto.class), deactivated);
    }
    
    @ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testDeactivateFailure(String role, String user) throws JsonProcessingException, MalformedURLException {
		// Arrange

		// Act
		HttpEntity<PutMemberDto> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(postDeactivateURI, brand.getUuid(), faker.code().ean13())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Member activation error: %s", response.getStatusCode()));

		if (response.getBody() != null && response.getBody().size() > 0) {
			ErrorDto err = TestResponseUtils.toError(response, objectMapper);
			Assertions.assertEquals(MemberException.MEMBER_NOT_FOUND, err.getCode());
		}
	}

    @ParameterizedTest
    @CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
    void testPatchSuccess(String role, String user) throws JsonProcessingException, MalformedURLException {
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
                HttpUtils.createURL(URI.create(String.format(patchURI, brand.getUuid(), patchDto.getUuid())), port, null),
                HttpMethod.PATCH, httpEntity, JsonNode.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(), String.format("Get error: %s", response.getStatusCode()));

        MemberDto patchedDto = TestResponseUtils.toDto(response, MemberDto.class, objectMapper);
        assertMember(modelMapper.map(memberToPatch, MemberDto.class), patchedDto);
    }
    
    @ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPatchFailureNotFound(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		Member patchTarget = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
		PatchMemberDto patchDto = modelMapper.map(patchTarget, PatchMemberDto.class);
		
		HttpEntity<PatchMemberDto> httpEntity = HttpUtils.createHttpEntity(role, user, patchDto);
		
		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(patchURI, patchTarget.getBrandUuid(), patchTarget.getUuid())), port, null),
				HttpMethod.PATCH, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Patch error: %s", response.getStatusCode()));
	}
	
	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPatchFailureForbiddenBrandMismatch(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PutMemberDto patchDto = modelMapper.map(MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular), PutMemberDto.class);
		HttpEntity<PutMemberDto> httpEntity = HttpUtils.createHttpEntity(role, user, patchDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, faker.code().isbn10(), patchDto.getUuid())), port, null),
				HttpMethod.PATCH, httpEntity, JsonNode.class);
		
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
	}
	
	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis" })
	void testPatchFailureForbiddenMemberMismatch(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		PutMemberDto putDto = modelMapper.map(MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular), PutMemberDto.class);
		HttpEntity<PutMemberDto> httpEntity = HttpUtils.createHttpEntity(role, user, putDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, brand.getUuid(), faker.code().isbn10())), port, null),
				HttpMethod.PATCH, httpEntity, JsonNode.class);
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
	}

    @Test
    void testDeleteSuccess() throws JsonProcessingException, MalformedURLException {
        // Arrange
        Member memberToDelete = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
        memberToDelete = memberRepository.save(memberToDelete);

        // Act
        HttpEntity<PutMemberDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, null);
        ResponseEntity<JsonNode> response = testRestTemplate.exchange(
                HttpUtils.createURL(URI.create(String.format(deleteURI, brand.getUuid(), memberToDelete.getUuid())), port, null),
                HttpMethod.DELETE, httpEntity, JsonNode.class);

        Assertions.assertEquals(HttpStatus.ACCEPTED, response.getStatusCode(), String.format("Member delete error: %s", response.getStatusCode()));

        httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, null);
        response = testRestTemplate.exchange(
                HttpUtils.createURL(URI.create(String.format(getURI, brand.getUuid(), memberToDelete.getUuid())), port, null),
                HttpMethod.GET, httpEntity, JsonNode.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), String.format("Get error: %s", response.getStatusCode()));
    }
    
    private void assertSearch(String criteria, int minimumNumberOfElements, int maximumNumberOfElements) 
			throws JsonProcessingException, MalformedURLException {
		// Arrange
		HttpEntity<String> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, "");

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(searchCriteria, criteria);
		params.add(pageNumber, "0");
		params.add(pageSize, "4");
		
		// Act
		await()
        .atMost(5, TimeUnit.SECONDS)
        .pollInterval(200, TimeUnit.MILLISECONDS)
        .untilAsserted(() -> {
    		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
    				HttpUtils.createURL(URI.create(String.format(searchURI, brand.getUuid()) ), port, params), HttpMethod.GET, httpEntity,
    				JsonNode.class);

    		Assertions.assertEquals(response.getStatusCode(), HttpStatus.OK,
    				String.format("Search error: %s", response.getStatusCode()));
    		
    		Page<MemberSearchDto> page = TestResponseUtils.toPage(response, new TypeReference<Page<MemberSearchDto>>() {}, objectMapper);
				Assertions.assertTrue(page.getNumberOfElements() >= minimumNumberOfElements &&
						page.getNumberOfElements() <= maximumNumberOfElements,
						String.format("Member search return invalid number of results [%s]: %d",
							criteria, page.getNumberOfElements()));
			});
	}

	public static final void assertMember(MemberDto expected, MemberDto result) {
		if (expected.getUuid() != null) {
			Assertions.assertEquals(expected.getUuid(), result.getUuid());
		}

		Assertions.assertEquals(expected.getBrandUuid(), result.getBrandUuid());
		// preferredMemberUuid may be null in some scenarios
		if (expected.getPreferredGymUuid() != null) {
			Assertions.assertEquals(expected.getPreferredGymUuid(), result.getPreferredGymUuid());
		}

		Assertions.assertEquals(expected.getMemberType(), result.getMemberType());
		Assertions.assertEquals(expected.getPerson().getFirstname(), result.getPerson().getFirstname());
		Assertions.assertEquals(expected.getPerson().getLastname(), result.getPerson().getLastname());
		Assertions.assertEquals(expected.getPerson().getEmail(), result.getPerson().getEmail());
		Assertions.assertEquals(expected.getPerson().getPhotoUri(), result.getPerson().getPhotoUri());
		Assertions.assertEquals(expected.getPerson().getCommunicationLanguage(),
				result.getPerson().getCommunicationLanguage());
		Assertions.assertEquals(expected.getPerson().getNote(), result.getPerson().getNote());

		if (expected.getPerson().getDateOfBirth() != null) {
			java.time.LocalDate expecteDob = expected.getPerson().getDateOfBirth().toInstant()
					.atZone(ZoneId.systemDefault()).toLocalDate();

			java.time.LocalDate resultDob = result.getPerson().getDateOfBirth().toInstant()
					.atZone(ZoneId.systemDefault()).toLocalDate();
			Assertions.assertEquals(expecteDob, resultDob);
		}

		Assertions.assertEquals(expected.isActive(), result.isActive());

		if (expected.getActivatedOn() != null) {
			Assertions.assertNotNull(result.getActivatedOn());
			Assertions.assertTrue(expected.getActivatedOn().truncatedTo(ChronoUnit.DAYS)
					.equals(result.getActivatedOn().truncatedTo(ChronoUnit.DAYS)));
		}

		if (expected.getDeactivatedOn() != null) {
			Assertions.assertNotNull(result.getDeactivatedOn());
			Assertions.assertTrue(expected.getDeactivatedOn().truncatedTo(ChronoUnit.DAYS)
					.equals(result.getDeactivatedOn().truncatedTo(ChronoUnit.DAYS)));
		}

		if (expected.getPerson().getAddress() != null) {
			Assertions.assertEquals(expected.getPerson().getAddress().getCivicNumber(),
					result.getPerson().getAddress().getCivicNumber());
			Assertions.assertEquals(expected.getPerson().getAddress().getStreetName(),
					result.getPerson().getAddress().getStreetName());
			Assertions.assertEquals(expected.getPerson().getAddress().getAppartment(),
					result.getPerson().getAddress().getAppartment());
			Assertions.assertEquals(expected.getPerson().getAddress().getCity(),
					result.getPerson().getAddress().getCity());
			Assertions.assertEquals(expected.getPerson().getAddress().getState(),
					result.getPerson().getAddress().getState());
			Assertions.assertEquals(expected.getPerson().getAddress().getZipCode(),
					result.getPerson().getAddress().getZipCode());
		}

		if (expected.getPerson().getAddress() == null) {
			Assertions.assertNull(result.getPerson().getAddress());
		}

		if (expected.getPerson().getPhoneNumbers() != null) {
			Assertions.assertNotNull(result.getPerson().getPhoneNumbers());

			Assertions.assertEquals(expected.getPerson().getPhoneNumbers().size(),
					result.getPerson().getPhoneNumbers().size());
			expected.getPerson().getPhoneNumbers().forEach(phone -> {
				Optional<PhoneNumberDto> previous = result.getPerson().getPhoneNumbers().stream()
						.filter(item -> item.getType().equals(phone.getType())).findFirst();
				Assertions.assertTrue(previous.isPresent());
			});
		}

		if (expected.getPerson().getPhoneNumbers() == null) {
			Assertions.assertNull(result.getPerson().getPhoneNumbers());

			if (expected.getPerson().getContacts() != null) {
				Assertions.assertNotNull(result.getPerson().getContacts());

				Assertions.assertEquals(expected.getPerson().getContacts().size(),
						result.getPerson().getContacts().size());
				expected.getPerson().getContacts().forEach(contact -> {
					Optional<ContactDto> previous = result.getPerson().getContacts().stream()
							.filter(item -> item.getFirstname().equals(contact.getFirstname())).findFirst();
					Assertions.assertTrue(previous.isPresent());
					Assertions.assertEquals(previous.get().getLastname(), contact.getLastname());
					Assertions.assertEquals(previous.get().getDescription(), contact.getDescription());
					Assertions.assertEquals(previous.get().getEmail(), contact.getEmail());

					if (previous.get().getPhoneNumbers() != null) {
						Assertions.assertNotNull(contact.getPhoneNumbers());

						Assertions.assertEquals(contact.getPhoneNumbers().size(), contact.getPhoneNumbers().size());

						contact.getPhoneNumbers().forEach(phone -> {
							Optional<PhoneNumberDto> previousPhone = contact.getPhoneNumbers().stream()
									.filter(item -> item.getType().equals(phone.getType())).findFirst();
							Assertions.assertTrue(previousPhone.isPresent());
							Assertions.assertEquals(previousPhone.get().getNumber(), phone.getNumber());
						});
					}

					if (previous.get().getPhoneNumbers() == null) {
						Assertions.assertNull(contact.getPhoneNumbers());
					}
				});
			}

			if (expected.getPerson().getContacts() == null) {
				Assertions.assertNull(result.getPerson().getContacts());
			}
		}
    }
}
