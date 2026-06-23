package com.iso.hypo.admin.papi.controller;

import java.util.Objects;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.iso.hypo.admin.papi.controller.util.ControllerErrorHandler;
import com.iso.hypo.admin.papi.dto.ErrorDto;
import com.iso.hypo.admin.papi.dto.model.OrderDto;
import com.iso.hypo.admin.papi.dto.patch.PatchOrderDto;
import com.iso.hypo.admin.papi.dto.post.PostOrderDto;
import com.iso.hypo.admin.papi.dto.put.PutOrderDto;
import com.iso.hypo.common.application.context.RequestContext;
import com.iso.hypo.common.application.dto.PageResultDto;
import com.iso.hypo.common.application.security.Roles;
import com.iso.hypo.sale.application.exception.OrderException;
import com.iso.hypo.sale.application.usecase.OrderQueryService;
import com.iso.hypo.sale.application.usecase.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/v1")
@Validated
public class SaleController {

	private static final Logger logger = LoggerFactory.getLogger(SaleController.class);

	private final ModelMapper modelMapper;
	private final RequestContext requestContext;
	private final OrderService orderService;
	private final OrderQueryService orderQueryService;

	public SaleController(ModelMapper modelMapper,
			OrderService orderService,
			OrderQueryService orderQueryService,
			RequestContext requestContext) {
		this.modelMapper = modelMapper;
		this.orderService = orderService;
		this.orderQueryService = orderQueryService;
		this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
	}

	@GetMapping("/brands/{brandUuid}/members/{memberUuid}/orders")
	@Operation(summary = "Retrieve a list of orders for a member")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = Page.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "400", description = "Bad request.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "403", description = "Forbidden.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected server error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "','" + Roles.Member + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> listOrders(
			@PathVariable String brandUuid,
			@PathVariable String memberUuid,
			@Parameter(description = "page number") @RequestParam int page,
			@Parameter(description = "page size") @RequestParam int pageSize,
			@Parameter(description = "includeInactive") @RequestParam(required = false, defaultValue = "false") boolean includeInactive) {

		PageResultDto<com.iso.hypo.sale.application.dto.OrderDto> domainDtos = null;
		try {
			domainDtos = orderQueryService.list(brandUuid, memberUuid, page, pageSize, includeInactive);
		} catch (OrderException e) {
			logger.error(e.getMessage(), e);
			return ControllerErrorHandler.buildErrorResponse(e, requestContext, null);
		}

		return ResponseEntity.ok(domainDtos.map(item -> modelMapper.map(item, OrderDto.class)));
	}

	@GetMapping("/brands/{brandUuid}/members/{memberUuid}/orders/{uuid}")
	@Operation(summary = "Retrieve an order by uuid")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = OrderDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected server error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "','" + Roles.Member + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> getOrder(
			@PathVariable String brandUuid,
			@PathVariable String memberUuid,
			@PathVariable String uuid) {

		com.iso.hypo.sale.application.dto.OrderDto domainDto;
		try {
			domainDto = orderQueryService.findByUuid(brandUuid, memberUuid, uuid);
		} catch (OrderException e) {
			logger.error(e.getMessage(), e);
			return ControllerErrorHandler.buildErrorResponse(e, requestContext, uuid);
		}

		return ResponseEntity.ok(modelMapper.map(domainDto, OrderDto.class));
	}

	@PostMapping("/brands/{brandUuid}/members/{memberUuid}/orders")
	@Operation(summary = "Create a new order for a member")
	@ApiResponses({
			@ApiResponse(responseCode = "201", content = {
					@Content(schema = @Schema(implementation = OrderDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "400", description = "Bad request.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "','" + Roles.Member + "')")
	@ResponseStatus(value = HttpStatus.CREATED)
	public ResponseEntity<Object> createOrder(
			@PathVariable String brandUuid,
			@PathVariable String memberUuid,
			@RequestBody PostOrderDto request) {

		if (!request.getBrandUuid().equals(brandUuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}

		if (!request.getMemberUuid().equals(memberUuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}

		com.iso.hypo.sale.application.dto.OrderDto domainDto =
				modelMapper.map(request, com.iso.hypo.sale.application.dto.OrderDto.class);

		try {
			domainDto = orderService.create(domainDto);
		} catch (OrderException e) {
			
			if (e.getCode() == OrderException.TRIAL_MEMBERSHIP_PLAN_ONLY_FOR_NEW_MEMBER) {
				return ResponseEntity.status(HttpStatus.OK).body(modelMapper.map(e.getOrderDto(), OrderDto.class));
			}
			
			logger.error(e.getMessage(), e);
			return ControllerErrorHandler.buildErrorResponse(e, requestContext, null);
		}

		return ResponseEntity
				.created(ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
						.buildAndExpand(domainDto.getUuid()).toUri())
				.body(modelMapper.map(domainDto, OrderDto.class));
	}
	
	@PutMapping("/brands/{brandUuid}/members/{memberUuid}/orders/{uuid}")
	@Operation(summary = "Update an order for a member")
	@ApiResponses({
			@ApiResponse(responseCode = "201", content = {
					@Content(schema = @Schema(implementation = OrderDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "400", description = "Bad request.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "','" + Roles.Member + "')")
	@ResponseStatus(value = HttpStatus.CREATED)
	public ResponseEntity<Object> createOrder(
			@PathVariable String brandUuid,
			@PathVariable String memberUuid,
			@PathVariable String uuid,
			@RequestBody PutOrderDto request) {

		if (!request.getBrandUuid().equals(brandUuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}

		if (!request.getMemberUuid().equals(memberUuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		
		if (!request.getUuid().equals(uuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}

		com.iso.hypo.sale.application.dto.OrderDto domainDto =
				modelMapper.map(request, com.iso.hypo.sale.application.dto.OrderDto.class);

		try {
			domainDto = orderService.update(domainDto);
		} catch (OrderException e) {
			
			if (e.getCode() == OrderException.TRIAL_MEMBERSHIP_PLAN_ONLY_FOR_NEW_MEMBER) {
				return ResponseEntity.status(HttpStatus.OK).body(modelMapper.map(e.getOrderDto(), OrderDto.class));
			}
			
			logger.error(e.getMessage(), e);
			return ControllerErrorHandler.buildErrorResponse(e, requestContext, null);
		}
		
		return ResponseEntity.ok(modelMapper.map(domainDto, OrderDto.class));
	}
	
	@PatchMapping("/brands/{brandUuid}/members/{memberUuid}/orders/{uuid}")
	@Operation(summary = "Update an order for a member")
	@ApiResponses({
			@ApiResponse(responseCode = "201", content = {
					@Content(schema = @Schema(implementation = OrderDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "400", description = "Bad request.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "','" + Roles.Member + "')")
	@ResponseStatus(value = HttpStatus.CREATED)
	public ResponseEntity<Object> patchOrder(
			@PathVariable String brandUuid,
			@PathVariable String memberUuid,
			@PathVariable String uuid,
			@RequestBody PatchOrderDto request) {

		if (!request.getBrandUuid().equals(brandUuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}

		if (!request.getMemberUuid().equals(memberUuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		
		if (!request.getUuid().equals(uuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}

		com.iso.hypo.sale.application.dto.OrderDto domainDto =
				modelMapper.map(request, com.iso.hypo.sale.application.dto.OrderDto.class);

		try {
			domainDto = orderService.patch(domainDto);
		} catch (OrderException e) {
			
			if (e.getCode() == OrderException.TRIAL_MEMBERSHIP_PLAN_ONLY_FOR_NEW_MEMBER) {
				return ResponseEntity.status(HttpStatus.OK).body(modelMapper.map(e.getOrderDto(), OrderDto.class));
			}
			
			logger.error(e.getMessage(), e);
			return ControllerErrorHandler.buildErrorResponse(e, requestContext, null);
		}
		
		return ResponseEntity.ok(modelMapper.map(domainDto, OrderDto.class));
	}
	
	@PostMapping("/brands/{brandUuid}/members/{memberUuid}/orders/{uuid}/submit")
	@Operation(summary = "Submit an order")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = OrderDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "','" + Roles.Member + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> submitOrder(
			@PathVariable String brandUuid,
			@PathVariable String memberUuid,
			@PathVariable String uuid,
			@RequestBody PutOrderDto request) {

		if (!request.getBrandUuid().equals(brandUuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		
		if (!request.getMemberUuid().equals(memberUuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}

		if (!request.getUuid().equals(uuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}

		com.iso.hypo.sale.application.dto.OrderDto domainDto =
				modelMapper.map(request, com.iso.hypo.sale.application.dto.OrderDto.class);
		
		try {
			domainDto = orderService.submit(brandUuid, memberUuid, uuid, domainDto);
		} catch (OrderException e) {
			
			if (e.getCode() == OrderException.TRIAL_MEMBERSHIP_PLAN_ONLY_FOR_NEW_MEMBER) {
				return ResponseEntity.status(HttpStatus.OK).body(modelMapper.map(e.getOrderDto(), OrderDto.class));
			}
			
			logger.error(e.getMessage(), e);
			return ControllerErrorHandler.buildErrorResponse(e, requestContext, uuid);
		}

		return ResponseEntity.ok(modelMapper.map(domainDto, OrderDto.class));
	}

	@PostMapping("/brands/{brandUuid}/members/{memberUuid}/orders/{uuid}/cancel")
	@Operation(summary = "Cancel an order")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = OrderDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "','" + Roles.Member + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> cancelOrder(
			@PathVariable String brandUuid,
			@PathVariable String memberUuid,
			@PathVariable String uuid) {

		com.iso.hypo.sale.application.dto.OrderDto domainDto;
		try {
			domainDto = orderService.cancel(brandUuid, memberUuid, uuid);
		} catch (OrderException e) {
			logger.error(e.getMessage(), e);
			return ControllerErrorHandler.buildErrorResponse(e, requestContext, uuid);
		}

		return ResponseEntity.ok(modelMapper.map(domainDto, OrderDto.class));
	}
}
