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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.iso.hypo.admin.papi.controller.util.ControllerErrorHandler;
import com.iso.hypo.admin.papi.dto.ErrorDto;
import com.iso.hypo.admin.papi.dto.model.FinancialInstrumentDto;
import com.iso.hypo.admin.papi.dto.post.PostFinancialInstrumentDto;
import com.iso.hypo.common.context.RequestContext;
import com.iso.hypo.domain.security.Roles;
import com.iso.hypo.services.FinancialInstrumentQueryService;
import com.iso.hypo.services.FinancialInstrumentService;
import com.iso.hypo.services.exception.FinancialInstrumentException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/v1")
@Validated
public class FinancialController {

	private static final Logger logger = LoggerFactory.getLogger(FinancialController.class);

	private final ModelMapper modelMapper;
	private final RequestContext requestContext;

	private final FinancialInstrumentService financialInstrumentService;
	private final FinancialInstrumentQueryService financialInstrumentQueryService;

	public FinancialController(ModelMapper modelMapper, 
			FinancialInstrumentService financialInstrumentService,
			FinancialInstrumentQueryService financialInstrumentQueryService, 
			RequestContext requestContext) {
		this.modelMapper = modelMapper;
		this.financialInstrumentService = financialInstrumentService;
		this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
		this.financialInstrumentQueryService = financialInstrumentQueryService;
	}

	@GetMapping("/brands/{brandUuid}/members/{memberUuid}/financial/instruments")
	@Operation(summary = "Retrieve a list of financial instruments for a member")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = Page.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "400", description = "Bad request. The request is invalid or missing required data.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "403", description = "Forbidden. The client does not have permission to access this resource.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found. The requested resource does not exist.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected server error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "','" + Roles.Member + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> listMembers(
			@PathVariable String brandUuid,
			@PathVariable String memberUuid,
			@Parameter(description = "page number") @RequestParam int page,
			@Parameter(description = "page size") @RequestParam int pageSize,
			@Parameter(description = "includeInactive") @RequestParam(required = false, defaultValue = "false") boolean includeInactive) {

		Page<com.iso.hypo.domain.dto.FinancialInstrumentDto> domainDtos = null;
		try {
			domainDtos = financialInstrumentQueryService.list(brandUuid, memberUuid, page, pageSize, includeInactive);
		} catch (FinancialInstrumentException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, null);
		}

		return ResponseEntity.ok(domainDtos.map(item -> modelMapper.map(item, FinancialInstrumentDto.class)));
	}
	
	@PostMapping("/brands/{brandUuid}/members/{memberUuid}/financial/instruments")
	@Operation(summary = "Create a new financial instrument for a financialInstrument")
	@ApiResponses({
			@ApiResponse(responseCode = "201", content = {
					@Content(schema = @Schema(implementation = FinancialInstrumentDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "','" + Roles.Member + "')")
	@ResponseStatus(value = HttpStatus.CREATED)
	public ResponseEntity<Object> createFinancialInstrument(
			@PathVariable String brandUuid,
			@PathVariable String memberUuid, 
			@RequestBody PostFinancialInstrumentDto request) {

		if (!request.getBrandUuid().equals(brandUuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		
		if (!request.getMemberUuid().equals(memberUuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		
		com.iso.hypo.domain.dto.FinancialInstrumentDto domainDto = modelMapper.map(request,
				com.iso.hypo.domain.dto.FinancialInstrumentDto.class);

		try {
			domainDto = financialInstrumentService.create(domainDto);
		} catch (FinancialInstrumentException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, null);
		}

		return ResponseEntity
				.created(ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
						.buildAndExpand(domainDto.getUuid()).toUri())
				.body(modelMapper.map(domainDto, FinancialInstrumentDto.class));
	}

	@PostMapping("/brands/{brandUuid}/members/{memberUuid}/financial/instruments/{uuid}/activate")
	@Operation(summary = "Activate a financial instrument")
	@ApiResponses({ @ApiResponse(responseCode = "200", content = {
			@Content(schema = @Schema(implementation = FinancialInstrumentDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "','" + Roles.Member + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> activateMember(
			@PathVariable String brandUuid,
			@PathVariable String memberUuid, 
			@PathVariable String uuid) {
		
		com.iso.hypo.domain.dto.FinancialInstrumentDto domainDto;

		try {
			domainDto = financialInstrumentService.activate(brandUuid, memberUuid, uuid);
		} catch (FinancialInstrumentException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, uuid);
		}

		return ResponseEntity.ok(modelMapper.map(domainDto, FinancialInstrumentDto.class));
	}

	@PostMapping("/brands/{brandUuid}/members/{memberUuid}/financial/instruments/{uuid}/deactivate")
	@Operation(summary = "Deactivate a financial instrument")
	@ApiResponses({ @ApiResponse(responseCode = "200", content = {
			@Content(schema = @Schema(implementation = FinancialInstrumentDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "','" + Roles.Member + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> deactivateMember(
			@PathVariable String brandUuid,
			@PathVariable String memberUuid, 
			@PathVariable String uuid) {
		
		com.iso.hypo.domain.dto.FinancialInstrumentDto domainDto;

		try {
			domainDto = financialInstrumentService.deactivate(brandUuid, memberUuid, uuid);
		} catch (FinancialInstrumentException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, uuid);
		}

		return ResponseEntity.ok(modelMapper.map(domainDto, FinancialInstrumentDto.class));
	}
	
	@DeleteMapping("/brands/{brandUuid}/members/{memberUuid}/financial/instruments/{uuid}")
	@Operation(summary = "Delete a financial instrument for a financialInstrument")
	@ApiResponses({ @ApiResponse(responseCode = "202"),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "','" + Roles.Member + "')")
	@ResponseStatus(value = HttpStatus.ACCEPTED)
	public ResponseEntity<Object> deleteFinancialInstrument(
			@PathVariable String brandUuid, 
			@PathVariable String memberUuid, 
			@PathVariable String uuid) {

		try {
			financialInstrumentService.delete(brandUuid, memberUuid, uuid);
		} catch (FinancialInstrumentException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, uuid);
		}

		return ResponseEntity.accepted().build();
	}
}
