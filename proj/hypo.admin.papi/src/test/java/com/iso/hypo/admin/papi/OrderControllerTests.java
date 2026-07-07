package com.iso.hypo.admin.papi;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

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
//import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
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
import com.iso.hypo.admin.papi.dto.model.OrderDto;
import com.iso.hypo.admin.papi.dto.order.BillingDetailDto;
import com.iso.hypo.admin.papi.dto.order.DiscountDetailDto;
import com.iso.hypo.admin.papi.dto.order.OrderItemDto;
import com.iso.hypo.admin.papi.dto.order.PaymentDetailDto;
import com.iso.hypo.admin.papi.dto.order.ShippingDetailDto;
import com.iso.hypo.admin.papi.dto.post.PostFinancialInstrumentDto;
import com.iso.hypo.admin.papi.dto.post.PostOrderDto;
import com.iso.hypo.admin.papi.dto.put.PutOrderDto;
import com.iso.hypo.brand.domain.model.Brand;
import com.iso.hypo.brand.domain.repository.BrandRepository;
import com.iso.hypo.common.application.dto.PageResultDto;
import com.iso.hypo.common.application.dto.enumeration.OrderStatusEnumDto;
import com.iso.hypo.common.application.security.Roles;
import com.iso.hypo.domain.BrandBuilder;
import com.iso.hypo.domain.FinancialInstrumentBuilder;
import com.iso.hypo.domain.MemberBuilder;
import com.iso.hypo.domain.MembershipBuilder;
import com.iso.hypo.domain.MembershipPlanBuilder;
import com.iso.hypo.domain.OrderBuilder;
import com.iso.hypo.finance.application.dto.FinancialInstrumentDto;
import com.iso.hypo.finance.domain.repository.FinancialInstrumentRepository;
import com.iso.hypo.membership.domain.model.Member;
import com.iso.hypo.membership.domain.model.Membership;
import com.iso.hypo.membership.domain.model.MembershipPlan;
import com.iso.hypo.membership.domain.model.enumeration.MemberTypeEnum;
import com.iso.hypo.membership.domain.model.enumeration.MembershipPlanPeriodEnum;
import com.iso.hypo.membership.domain.repository.MemberRepository;
import com.iso.hypo.membership.domain.repository.MembershipPlanRepository;
import com.iso.hypo.membership.domain.repository.MembershipRepository;
import com.iso.hypo.sale.application.exception.OrderException;
import com.iso.hypo.sale.domain.model.Order;
import com.iso.hypo.sale.domain.model.enumeration.OrderStatusEnum;
import com.iso.hypo.sale.domain.repository.OrderRepository;
import com.iso.hypo.tests.http.HttpUtils;
import com.iso.hypo.tests.security.Users;
import com.iso.hypo.tests.utils.TestResponseUtils;

import net.datafaker.Faker;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "app.test.run=true")
@TestInstance(Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class OrderControllerTests {

/*	public static final String listURI    = "/v1/brands/%s/members/%s/orders";
	public static final String getURI     = "/v1/brands/%s/members/%s/orders/%s";
	public static final String postURI    = "/v1/brands/%s/members/%s/orders";
	public static final String putURI     = "/v1/brands/%s/members/%s/orders/%s";
	public static final String patchURI   = "/v1/brands/%s/members/%s/orders/%s";
	public static final String deleteURI  = "/v1/brands/%s/members/%s/orders/%s";
	public static final String submitURI  = "/v1/brands/%s/members/%s/orders/%s/submit";
	public static final String cancelURI  = "/v1/brands/%s/members/%s/orders/%s/cancel";
	
	public static final String financialInstrumentPostURI  =  "/v1/brands/%s/members/%s/financial/instruments";

	public static final String pageNumber = "page";
	public static final String pageSize   = "pageSize";

	public static final String brandCode  = "OrderBrand1";

	@LocalServerPort
	private int port;

	@Autowired
	BrandRepository brandRepository;
	@Autowired
	MemberRepository memberRepository;
	@Autowired
	MembershipPlanRepository membershipPlanRepository;
	@Autowired
	MembershipRepository membershipRepository;
	@Autowired
	FinancialInstrumentRepository financialInstrumentRepository;
	@Autowired
	OrderRepository orderRepository;
	@Autowired
	ObjectMapper objectMapper;
	@Autowired
	ModelMapper modelMapper;


	private Faker faker = new Faker();

	private TestRestTemplate testRestTemplate = new TestRestTemplate();
	private Member member;
	private Brand brand;
	private MembershipPlan membershipPlan1;
	private MembershipPlan membershipPlan2;
	//private List<String> membershipPlans;
	private List<com.iso.hypo.sale.domain.model.MembershipPlan> membershipPlans;

	@BeforeAll
	void arrange() {
		testRestTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
		orderRepository.deleteAll();
		memberRepository.deleteAll();
		membershipPlanRepository.deleteAll();

		brand = BrandBuilder.build(brandCode, faker.company().name());
		brandRepository.save(brand);

		membershipPlan1 = MembershipPlanBuilder.build(brand.getUuid(), null, null);
		membershipPlanRepository.save(membershipPlan1);

		membershipPlan2 = MembershipPlanBuilder.build(brand.getUuid(), null, null);
		membershipPlanRepository.save(membershipPlan2);
		membershipPlans = List.of(
				modelMapper.map(membershipPlan1, com.iso.hypo.sale.domain.model.MembershipPlan.class),
				modelMapper.map( membershipPlan2, com.iso.hypo.sale.domain.model.MembershipPlan.class));
		
		member = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
		memberRepository.save(member);
		
		for (int i = 0; i < 2; i++) {
			Order order = OrderBuilder.build(brand.getUuid(), member.getUuid(), membershipPlans);
			order.setActive(true);
			orderRepository.save(order);
		}
	}

	@AfterAll
	void cleanup() {
		// Cleanup if needed
	}

	@Test
	void testListFirstPageSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		HttpEntity<String> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, null);

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(pageNumber, "0");
		params.add(pageSize, "2");

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(listURI, brand.getUuid(), member.getUuid())), port, params),
				HttpMethod.GET, httpEntity, JsonNode.class);

		// Assert
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("List error: %s", response.getStatusCode()));

		PageResultDto<OrderDto> page = TestResponseUtils.toPage(response,
				new TypeReference<PageResultDto<OrderDto>>() {}, objectMapper);
		Assertions.assertEquals(0, page.getPageNumber(),
				String.format("Order list first page number invalid: %d", page.getPageNumber()));
		Assertions.assertEquals(2, page.getContent().size(),
				String.format("Order list first page number of elements invalid: %d", page.getTotalElements()));
	}

	@Test
	void testGetSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		Order order = OrderBuilder.build(brand.getUuid(), member.getUuid(), membershipPlans);
		order.setActive(true);
		orderRepository.save(order);

		HttpEntity<String> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, null);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(getURI, brand.getUuid(), member.getUuid(), order.getUuid())), port, null),
				HttpMethod.GET, httpEntity, JsonNode.class);

		// Assert
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));

		OrderDto result =
				TestResponseUtils.toDto(response, OrderDto.class, objectMapper);
		
		assertOrder(modelMapper.map(order, OrderDto.class), result);
	}

	@Test
	void testGetFailureNotFound() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		HttpEntity<String> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, null);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(getURI, brand.getUuid(), member.getUuid(), faker.code().isbn10())), port, null),
				HttpMethod.GET, httpEntity, JsonNode.class);

		// Assert
		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Get error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis", "member, Guillaume Fortin" })
	void testPostSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		Member postMember = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
		memberRepository.save(postMember);

		Order postOrder = OrderBuilder.build(brand.getUuid(), postMember.getUuid(), membershipPlans);
		PostOrderDto postOrderDto = modelMapper.map(postOrder, PostOrderDto.class);
		HttpEntity<PostOrderDto> httpEntity = HttpUtils.createHttpEntity(role, user, postOrderDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postURI, brand.getUuid(), postMember.getUuid())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		// Assert
		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));

		OrderDto createdDto =
				TestResponseUtils.toDto(response, OrderDto.class, objectMapper);
		
		assertOrder(modelMapper.map(postOrderDto, OrderDto.class), createdDto);
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis", "member, Guillaume Fortin" })
	void testPostFailureForbiddenBrandMismatch(String role, String user)
			throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		Member postMember = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
		memberRepository.save(postMember);

		PostOrderDto postDto = modelMapper.map(
				OrderBuilder.build(brand.getUuid(), postMember.getUuid(), membershipPlans),
				PostOrderDto.class);
		HttpEntity<PostOrderDto> httpEntity = HttpUtils.createHttpEntity(role, user, postDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postURI, faker.code().isbn10(), postMember.getUuid())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		// Assert
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis", "member, Guillaume Fortin" })
	void testPostFailureForbiddenMemberMismatch(String role, String user)
			throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		Member postMember = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
		memberRepository.save(postMember);

		PostOrderDto postDto = modelMapper.map(
				OrderBuilder.build(brand.getUuid(), postMember.getUuid(), membershipPlans),
				PostOrderDto.class);
		HttpEntity<PostOrderDto> httpEntity = HttpUtils.createHttpEntity(role, user, postDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postURI, brand.getUuid(), faker.code().isbn10())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		// Assert
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
	}
	
	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis", "member, Guillaume Fortin" })
	void testUpdateSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		Member postMember = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
		memberRepository.save(postMember);

		Order orderToUpdate = OrderBuilder.build(brand.getUuid(), postMember.getUuid(), membershipPlans);
		orderToUpdate.setActive(true);
		orderRepository.save(orderToUpdate);
		
		OrderDto orderToUpdateDto = modelMapper.map(orderToUpdate, OrderDto.class);
		
		MembershipPlan membershipPlan = MembershipPlanBuilder.build(brand.getUuid(), null, null);
		membershipPlanRepository.save(membershipPlan);
		
		orderToUpdate.setBillingDetail(OrderBuilder.buildBillingDetail());
		orderToUpdate.setShippingDetail(OrderBuilder.buildShippingDetail());
		orderToUpdate.setDiscountDetail(OrderBuilder.buildDiscountDetail());
		orderToUpdate.setPaymentDetail(OrderBuilder.buildPaymentDetail());
		orderToUpdate.setItems(OrderBuilder.buildItems(List.of(modelMapper.map(membershipPlan, com.iso.hypo.sale.domain.model.MembershipPlan.class))));
		PutOrderDto putOrderDto = modelMapper.map(orderToUpdate, PutOrderDto.class);
		
		List<OrderItemDto> items = orderToUpdate.getItems().stream()
				.map(item -> modelMapper.map(item, OrderItemDto.class))
				.toList();
		orderToUpdateDto.setItems(items);
		orderToUpdateDto.setBillingDetail(modelMapper.map(orderToUpdate.getBillingDetail(), BillingDetailDto.class));
		orderToUpdateDto.setShippingDetail(modelMapper.map(orderToUpdate.getShippingDetail(), ShippingDetailDto.class));
		orderToUpdateDto.setDiscountDetail(modelMapper.map(orderToUpdate.getDiscountDetail(), DiscountDetailDto.class));
		orderToUpdateDto.setPaymentDetail(modelMapper.map(orderToUpdate.getPaymentDetail(), PaymentDetailDto.class));
		
		HttpEntity<PutOrderDto> httpEntity = HttpUtils.createHttpEntity(role, user, putOrderDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(putURI, brand.getUuid(), postMember.getUuid(), orderToUpdate.getUuid())), port, null),
				HttpMethod.PUT, httpEntity, JsonNode.class);

		// Assert
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Order submit error: %s", response.getStatusCode()));
		
		OrderDto updatedDto =
				TestResponseUtils.toDto(response, OrderDto.class, objectMapper);
		
		assertOrder(modelMapper.map(orderToUpdateDto, OrderDto.class), updatedDto);
	}
	
	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis", "member, Guillaume Fortin" })
	void testPatchSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		Member postMember = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
		memberRepository.save(postMember);

		Order orderToUpdate = OrderBuilder.build(brand.getUuid(), postMember.getUuid(), membershipPlans);
		orderToUpdate.setActive(true);
		orderRepository.save(orderToUpdate);
		
		OrderDto orderToUpdateDto = modelMapper.map(orderToUpdate, OrderDto.class);
		
		MembershipPlan membershipPlan = MembershipPlanBuilder.build(brand.getUuid(), null, null);
		membershipPlanRepository.save(membershipPlan);

		orderToUpdate.setItems(OrderBuilder.buildItems(List.of(modelMapper.map(membershipPlan, com.iso.hypo.sale.domain.model.MembershipPlan.class))));
		orderToUpdate.setBillingDetail(null);
		orderToUpdate.setShippingDetail(null);
		orderToUpdate.setDiscountDetail(null);
		orderToUpdate.setPaymentDetail(null);
		orderToUpdate.setSubTotal(null);
		orderToUpdate.setShippingTotal(null);
		orderToUpdate.setDiscountTotal(null);
		orderToUpdate.setTotal(null);
		orderToUpdate.setDeposit(null);
		orderToUpdate.setTaxes(null);
		orderToUpdate.setOrderNumber(null);		
		
		List<OrderItemDto> items = orderToUpdate.getItems().stream()
				.map(item -> modelMapper.map(item, OrderItemDto.class))
				.toList();
		orderToUpdateDto.setItems(items);
		
		PutOrderDto putOrderDto = modelMapper.map(orderToUpdate, PutOrderDto.class);
		
		HttpEntity<PutOrderDto> httpEntity = HttpUtils.createHttpEntity(role, user, putOrderDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(patchURI, brand.getUuid(), postMember.getUuid(), orderToUpdate.getUuid())), port, null),
				HttpMethod.PATCH, httpEntity, JsonNode.class);

		// Assert
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Order submit error: %s", response.getStatusCode()));
		
		OrderDto updatedDto =
				TestResponseUtils.toDto(response, OrderDto.class, objectMapper);
		
		assertOrder(modelMapper.map(orderToUpdateDto, OrderDto.class), updatedDto);
	}
	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis", "member, Guillaume Fortin" })
	void testSubmitSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		Member member = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
		memberRepository.save(member);
		
		PostFinancialInstrumentDto postFinancialInstrumentDto = modelMapper
				.map(FinancialInstrumentBuilder.build(brand.getUuid(), member), PostFinancialInstrumentDto.class);
		HttpEntity<PostFinancialInstrumentDto> httpEntity = HttpUtils.createHttpEntity(role, user, postFinancialInstrumentDto);
		postFinancialInstrumentDto.getCreditCard().setCvd("123");
		postFinancialInstrumentDto.getCreditCard().setZipCode("H3Z2Y7");
		postFinancialInstrumentDto.getCreditCard().setCardHolderName("John Doe");

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils
				.createURL(URI.create(String.format(financialInstrumentPostURI, brand.getUuid(), member.getUuid())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));

		FinancialInstrumentDto createdFinancialInstrumentDto = TestResponseUtils.toDto(response, FinancialInstrumentDto.class,	objectMapper);
		
		Order postOrder = OrderBuilder.build(brand.getUuid(), member.getUuid(), membershipPlans);
		postOrder.getPaymentDetail().setFinancialInstrumentUuid(createdFinancialInstrumentDto.getUuid());
		
		PostOrderDto postOrderDto = modelMapper.map(postOrder, PostOrderDto.class);
		HttpEntity<PostOrderDto> httpEntityOrder = HttpUtils.createHttpEntity(role, user, postOrderDto);

		// Act
		response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postURI, brand.getUuid(), member.getUuid())), port, null),
				HttpMethod.POST, httpEntityOrder, JsonNode.class);

		// Assert
		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));

		OrderDto createdOrderDto =
				TestResponseUtils.toDto(response, OrderDto.class, objectMapper);
		
		PutOrderDto putOrderDto = modelMapper.map(createdOrderDto, PutOrderDto.class);
		HttpEntity<PutOrderDto> httpEntitySubmit = HttpUtils.createHttpEntity(role, user, putOrderDto);

		// Act
		response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(submitURI, brand.getUuid(), member.getUuid(), putOrderDto.getUuid())), port, null),
				HttpMethod.POST, httpEntitySubmit, JsonNode.class);

		// Assert
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Order submit error: %s", response.getStatusCode()));

		OrderDto submittedDto =
				TestResponseUtils.toDto(response,OrderDto.class, objectMapper);
		Assertions.assertEquals(OrderStatusEnumDto.completed, submittedDto.getStatus(),
				"Order status should be completed");
		Assertions.assertNotNull(submittedDto.getSubmittedOn(), "SubmittedOn should not be null");
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis", "member, Guillaume Fortin" })
	void testSubmitFailureNotFound(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		Order orderToSubmit = OrderBuilder.build(brand.getUuid(), member.getUuid(), membershipPlans);
		orderToSubmit.setActive(true);
		
		PutOrderDto putOrderDto = modelMapper.map(orderToSubmit, PutOrderDto.class);
		HttpEntity<PutOrderDto> httpEntity = HttpUtils.createHttpEntity(role, user, putOrderDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(submitURI, brand.getUuid(), member.getUuid(), orderToSubmit.getUuid())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		// Assert
		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Order submit error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis", "member, Guillaume Fortin" })
	void testSubmitFailureForbiddenBrandMismatch(String role, String user)
			throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		Member postMember = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
		memberRepository.save(postMember);
		
		Order orderToSubmit = OrderBuilder.build(brand.getUuid(), postMember.getUuid(), membershipPlans);
		orderToSubmit.setActive(true);
		orderRepository.save(orderToSubmit);
		
		PutOrderDto putOrderDto = modelMapper.map(orderToSubmit, PutOrderDto.class);
		HttpEntity<PutOrderDto> httpEntity = HttpUtils.createHttpEntity(role, user, putOrderDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(submitURI,  faker.code().isbn10(), postMember.getUuid(), orderToSubmit.getUuid())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		// Assert
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis", "member, Guillaume Fortin" })
	void testSubmitFailureForbiddenMemberMismatch(String role, String user)
			throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		Member postMember = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
		memberRepository.save(postMember);
		
		Order orderToSubmit = OrderBuilder.build(brand.getUuid(), postMember.getUuid(), membershipPlans);
		orderToSubmit.setActive(true);
		orderRepository.save(orderToSubmit);
		
		PutOrderDto putOrderDto = modelMapper.map(orderToSubmit, PutOrderDto.class);
		HttpEntity<PutOrderDto> httpEntity = HttpUtils.createHttpEntity(role, user, putOrderDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(submitURI, brand.getUuid(), faker.code().isbn10(), orderToSubmit.getUuid())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		// Assert
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
	}
	
	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis", "member, Guillaume Fortin" })
	void testSubmitFailureForbiddenOrderMismatch(String role, String user)
			throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		Member postMember = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
		memberRepository.save(postMember);
		
		Order orderToSubmit = OrderBuilder.build(brand.getUuid(), postMember.getUuid(), membershipPlans);
		orderToSubmit.setActive(true);
		orderRepository.save(orderToSubmit);
		
		PutOrderDto putOrderDto = modelMapper.map(orderToSubmit, PutOrderDto.class);
		HttpEntity<PutOrderDto> httpEntity = HttpUtils.createHttpEntity(role, user, putOrderDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(submitURI, brand.getUuid(), member.getUuid(), faker.code().isbn10())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		// Assert
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
	}
	
	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis", "member, Guillaume Fortin" })
	void testCancelSuccess(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		Member postMember = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
		memberRepository.save(postMember);

		Order orderToCancel = OrderBuilder.build(brand.getUuid(), postMember.getUuid(), membershipPlans);
		orderToCancel.setActive(true);
		orderToCancel.setStatus(OrderStatusEnum.submitted);
		orderRepository.save(orderToCancel);

		// Act
		HttpEntity<Void> httpEntity = HttpUtils.createHttpEntity(role, user, null);
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(cancelURI, brand.getUuid(), postMember.getUuid(), orderToCancel.getUuid())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		// Assert
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Order cancel error: %s", response.getStatusCode()));

		OrderDto cancelledDto =
				TestResponseUtils.toDto(response, OrderDto.class, objectMapper);
		Assertions.assertEquals(OrderStatusEnumDto.cancelled, cancelledDto.getStatus(),
				"Order status should be cancelled");
	}

	@ParameterizedTest
	@CsvSource({ "admin, Bruno Fortin", "manager, Liliane Denis", "member, Guillaume Fortin" })
	void testCancelFailureNotFound(String role, String user) throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		HttpEntity<Void> httpEntity = HttpUtils.createHttpEntity(role, user, null);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(cancelURI, brand.getUuid(), member.getUuid(), faker.code().isbn10())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		// Assert
		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
				String.format("Order cancel error: %s", response.getStatusCode()));
	}

	@Test
	void testTrialNewMemberSameMembershipPlanFailure() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		Member postMember = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
		memberRepository.save(postMember);
		
		MembershipPlan membershipPlan = MembershipPlanBuilder.build(brand.getUuid(), null, null);
		membershipPlan.setPeriod(MembershipPlanPeriodEnum.trial);
		membershipPlan = membershipPlanRepository.save(membershipPlan);

		Membership membership = MembershipBuilder.build(brand.getUuid(), postMember.getUuid(), membershipPlan);
		membership = membershipRepository.save(membership);
		
		//membershipRepository
		Order postOrder = OrderBuilder.build(brand.getUuid(), postMember.getUuid(), List.of(modelMapper.map(membershipPlan, com.iso.hypo.sale.domain.model.MembershipPlan.class)));
		PostOrderDto postOrderDto = modelMapper.map(postOrder, PostOrderDto.class);
		HttpEntity<PostOrderDto> httpEntity = HttpUtils.createHttpEntity(Roles.Member, Users.Member, postOrderDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postURI, brand.getUuid(), postMember.getUuid())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		// Assert
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));

		OrderDto orderDto =
				TestResponseUtils.toDto(response, OrderDto.class, objectMapper);
		
		Assertions.assertEquals(1, orderDto.getMessages().size(),
				String.format("Trial error , missing message: %s", orderDto.getMessages().size()));
		
		Assertions.assertEquals(OrderException.TRIAL_MEMBERSHIP_PLAN_ONLY_FOR_NEW_MEMBER, orderDto.getMessages().getFirst().getCode(),
				String.format("Trial error, missing message: %s", orderDto.getMessages().getFirst().getCode()));
	}
	
	@Test
	void testTrialNewMemberFailure() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		Member postMember = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
		postMember = memberRepository.save(postMember);
		
		MembershipPlan membershipPlan1 = MembershipPlanBuilder.build(brand.getUuid(), null, null);
		membershipPlan1.setPeriod(MembershipPlanPeriodEnum.classes);
		membershipPlan1 = membershipPlanRepository.save(membershipPlan1);
		
		Membership membership = MembershipBuilder.build(brand.getUuid(), postMember.getUuid(), membershipPlan1);
		membership = membershipRepository.save(membership);
		
		MembershipPlan trialMembershipPlan = MembershipPlanBuilder.build(brand.getUuid(), null, null);
		trialMembershipPlan.setPeriod(MembershipPlanPeriodEnum.trial);
		trialMembershipPlan = membershipPlanRepository.save(trialMembershipPlan);
		
		//membershipRepository
		Order postOrder = OrderBuilder.build(brand.getUuid(), postMember.getUuid(), List.of(modelMapper.map(trialMembershipPlan, com.iso.hypo.sale.domain.model.MembershipPlan.class)));
		PostOrderDto postOrderDto = modelMapper.map(postOrder, PostOrderDto.class);
		HttpEntity<PostOrderDto> httpEntity = HttpUtils.createHttpEntity(Roles.Member, Users.Member, postOrderDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postURI, brand.getUuid(), postMember.getUuid())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		// Assert
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));

		OrderDto orderDto =
				TestResponseUtils.toDto(response, OrderDto.class, objectMapper);
		
		Assertions.assertEquals(1, orderDto.getMessages().size(),
				String.format("Trial error , missing message: %s", orderDto.getMessages().size()));
		
		Assertions.assertEquals(OrderException.TRIAL_MEMBERSHIP_PLAN_ONLY_FOR_NEW_MEMBER, orderDto.getMessages().getFirst().getCode(),
				String.format("Trial error, missing message: %s", orderDto.getMessages().getFirst().getCode()));
	}

	@Test
	void testTrialOnlyOneSuccess() throws MalformedURLException, JsonProcessingException, Exception {
		// Arrange
		Member postMember = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
		memberRepository.save(postMember);
		
		MembershipPlan membershipPlan = MembershipPlanBuilder.build(brand.getUuid(), null, null);
		membershipPlan.setPeriod(MembershipPlanPeriodEnum.trial);
		membershipPlan = membershipPlanRepository.save(membershipPlan);
		
		// membershipRepository
		Order postOrder = OrderBuilder.build(brand.getUuid(), postMember.getUuid(), List.of(modelMapper.map(membershipPlan, com.iso.hypo.sale.domain.model.MembershipPlan.class)));
		postOrder.getItems().stream().findFirst().get().setQuantity(2);
		
		PostOrderDto postOrderDto = modelMapper.map(postOrder, PostOrderDto.class);
		HttpEntity<PostOrderDto> httpEntity = HttpUtils.createHttpEntity(Roles.Member, Users.Member, postOrderDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(
				HttpUtils.createURL(URI.create(String.format(postURI, brand.getUuid(), postMember.getUuid())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		// Assert
		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));

		OrderDto createdDto =
				TestResponseUtils.toDto(response, OrderDto.class, objectMapper);
		
		Assertions.assertEquals(1, createdDto.getItems().stream().findFirst().get().getQuantity(),
				String.format("Only one trial error , invalid quantity : %s", createdDto.getItems().stream().findFirst().get().getQuantity()));
	}
	
	public static void assertOrder(OrderDto expected, OrderDto result) {
		if (expected.getUuid() != null) {
			Assertions.assertEquals(expected.getUuid(), result.getUuid());
		}

		Assertions.assertEquals(expected.getBrandUuid(), result.getBrandUuid());
		Assertions.assertEquals(expected.getMemberUuid(), result.getMemberUuid());
		Assertions.assertNotNull(result.getOrderNumber());
		Assertions.assertNotNull(result.getStatus(), "Order status should not be null");

		if (expected.getCreatedOn() != null) {
			Assertions.assertNotNull(result.getCreatedOn());
			Assertions.assertTrue(expected.getCreatedOn().truncatedTo(ChronoUnit.DAYS).equals(result.getCreatedOn().truncatedTo(ChronoUnit.DAYS)));
		}

		if (expected.getSubmittedOn() != null) {
			Assertions.assertNotNull(result.getSubmittedOn());
			Assertions.assertTrue(expected.getSubmittedOn().truncatedTo(ChronoUnit.DAYS).equals(result.getSubmittedOn().truncatedTo(ChronoUnit.DAYS)));
		}

		// Items
		Assertions.assertNotNull(result.getItems(), "Order items should not be null");
		Assertions.assertFalse(result.getItems().isEmpty(), "Order items should not be empty");
		Assertions.assertEquals(expected.getItems().size(), result.getItems().size());
		expected.getItems().forEach(expectedItem -> {
			Optional<OrderItemDto> resultItem = result.getItems().stream()
					.filter(item -> item.getMembershipPlan().getUuid().equals(expectedItem.getMembershipPlan().getUuid()))
					.findFirst();
			Assertions.assertTrue(resultItem.isPresent());
			Assertions.assertEquals(expectedItem.getQuantity(), resultItem.get().getQuantity());

			Assertions.assertNotNull(resultItem.get().getMembershipPlan());
			Assertions.assertEquals(expectedItem.getMembershipPlan().getUuid(), resultItem.get().getMembershipPlan().getUuid());
			
			if (expectedItem.getUnitPrice() != null) {
				Assertions.assertNotNull(resultItem.get().getUnitPrice());
				Assertions.assertEquals(expectedItem.getUnitPrice().getAmount(), resultItem.get().getUnitPrice().getAmount());
			}

			if (expectedItem.getItemTotal() != null) {
				Assertions.assertNotNull(resultItem.get().getItemTotal());
				Assertions.assertEquals(expectedItem.getItemTotal().getAmount(), resultItem.get().getItemTotal().getAmount());
			}
		});

		// Costs
		if (expected.getSubTotal() != null) {
			Assertions.assertNotNull(result.getSubTotal());
			Assertions.assertEquals(expected.getSubTotal().getAmount(), result.getSubTotal().getAmount());
		}

		if (expected.getShippingTotal() != null) {
			Assertions.assertNotNull(result.getShippingTotal());
			Assertions.assertEquals(expected.getShippingTotal().getAmount(), result.getShippingTotal().getAmount());
		}

		if (expected.getDiscountTotal() != null) {
			Assertions.assertNotNull(result.getDiscountTotal());
			Assertions.assertEquals(expected.getDiscountTotal().getAmount(), result.getDiscountTotal().getAmount());
		}

		if (expected.getTotal() != null) {
			Assertions.assertNotNull(result.getTotal());
			Assertions.assertEquals(expected.getTotal().getAmount(), result.getTotal().getAmount());
		}

		if (expected.getDeposit() != null) {
			Assertions.assertNotNull(result.getDeposit());
			Assertions.assertEquals(expected.getDeposit().getAmount(), result.getDeposit().getAmount());
		}

		if (expected.getCurrency() != null) {
			Assertions.assertEquals(expected.getCurrency().getCode(), result.getCurrency().getCode());
			Assertions.assertEquals(expected.getCurrency().getName(), result.getCurrency().getName());
			Assertions.assertEquals(expected.getCurrency().getSymbol(), result.getCurrency().getSymbol());
		}		
		
		// Billing detail
		if (expected.getBillingDetail() != null) {
			Assertions.assertNotNull(result.getBillingDetail(), "Billing detail should not be null");
			Assertions.assertEquals(expected.getBillingDetail().getEmail(), result.getBillingDetail().getEmail());
			Assertions.assertEquals(expected.getBillingDetail().getName(), result.getBillingDetail().getName());

			if (expected.getBillingDetail().getAddress() != null) {
				Assertions.assertEquals(expected.getBillingDetail().getAddress().getCivicNumber(), result.getBillingDetail().getAddress().getCivicNumber());
				Assertions.assertEquals(expected.getBillingDetail().getAddress().getStreetName(), result.getBillingDetail().getAddress().getStreetName());
				Assertions.assertEquals(expected.getBillingDetail().getAddress().getAppartment(), result.getBillingDetail().getAddress().getAppartment());
				Assertions.assertEquals(expected.getBillingDetail().getAddress().getCity(), result.getBillingDetail().getAddress().getCity());
				Assertions.assertEquals(expected.getBillingDetail().getAddress().getState(), result.getBillingDetail().getAddress().getState());
				Assertions.assertEquals(expected.getBillingDetail().getAddress().getZipCode(), result.getBillingDetail().getAddress().getZipCode());
			}

			if (expected.getBillingDetail().getAddress() == null) {
				Assertions.assertNull(result.getBillingDetail().getAddress());
			}
		}

		if (expected.getBillingDetail() == null) {
			Assertions.assertNull(result.getBillingDetail());
		}

		// Shipping detail
		if (expected.getShippingDetail() != null) {
			Assertions.assertNotNull(result.getShippingDetail(), "Shipping detail should not be null");
			Assertions.assertEquals(expected.getShippingDetail().getShippingMethod(), result.getShippingDetail().getShippingMethod());
			Assertions.assertEquals(expected.getShippingDetail().getCarrier(), result.getShippingDetail().getCarrier());
			Assertions.assertEquals(expected.getShippingDetail().getTrackingNumber(), result.getShippingDetail().getTrackingNumber());

			if (expected.getShippingDetail().getAddress() != null) {
				Assertions.assertEquals(expected.getShippingDetail().getAddress().getCivicNumber(), result.getShippingDetail().getAddress().getCivicNumber());
				Assertions.assertEquals(expected.getShippingDetail().getAddress().getStreetName(), result.getShippingDetail().getAddress().getStreetName());
				Assertions.assertEquals(expected.getShippingDetail().getAddress().getAppartment(), result.getShippingDetail().getAddress().getAppartment());
				Assertions.assertEquals(expected.getShippingDetail().getAddress().getCity(), result.getShippingDetail().getAddress().getCity());
				Assertions.assertEquals(expected.getShippingDetail().getAddress().getState(), result.getShippingDetail().getAddress().getState());
				Assertions.assertEquals(expected.getShippingDetail().getAddress().getZipCode(), result.getShippingDetail().getAddress().getZipCode());
			}

			if (expected.getShippingDetail().getAddress() == null) {
				Assertions.assertNull(result.getShippingDetail().getAddress());
			}
		}

		if (expected.getShippingDetail() == null) {
			Assertions.assertNull(result.getShippingDetail());
		}

		// Discount detail
		if (expected.getDiscountDetail() != null) {
			Assertions.assertNotNull(result.getDiscountDetail(), "Discount detail should not be null");
			Assertions.assertEquals(expected.getDiscountDetail().getCouponCode(), result.getDiscountDetail().getCouponCode());
			Assertions.assertEquals(expected.getDiscountDetail().getRate(), result.getDiscountDetail().getRate());
		}

		if (expected.getDiscountDetail() == null) {
			Assertions.assertNull(result.getDiscountDetail());
		}

		// Payment detail
		if (expected.getPaymentDetail() != null) {
			Assertions.assertNotNull(result.getPaymentDetail(), "Payment detail should not be null");
			Assertions.assertEquals(expected.getPaymentDetail().getPaymentMethod(), result.getPaymentDetail().getPaymentMethod());
			Assertions.assertEquals(expected.getPaymentDetail().getFinancialInstrumentUuid(), result.getPaymentDetail().getFinancialInstrumentUuid());
		}

		if (expected.getPaymentDetail() == null) {
			Assertions.assertNull(result.getPaymentDetail());
		}
	}
	*/
}
