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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.iso.hypo.admin.papi.controller.util.ControllerErrorHandler;
import com.iso.hypo.admin.papi.dto.ErrorDto;
import com.iso.hypo.admin.papi.dto.model.MembershipDto;
import com.iso.hypo.common.application.context.RequestContext;
import com.iso.hypo.common.application.security.Roles;
import com.iso.hypo.membership.application.usecase.MembershipQueryService;
import com.iso.hypo.membership.domain.exception.MembershipException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/v1")
@Validated
public class MembershipController {

	private static final Logger logger = LoggerFactory.getLogger(MembershipController.class);

	private final ModelMapper modelMapper;
	private final RequestContext requestContext;

	private final MembershipQueryService membershipQueryService;

	public MembershipController(ModelMapper modelMapper, MembershipQueryService membershipQueryService, RequestContext requestContext) {
		this.modelMapper = modelMapper;
		this.membershipQueryService = membershipQueryService;
		this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
	}

	@GetMapping("/brands/{brandUuid}/memberships")
	@Operation(summary = "Retrieve a list of memberships")
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
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> listMemberships(
			@PathVariable String brandUuid,
			@Parameter(description = "page number") @RequestParam int page,
			@Parameter(description = "page size") @RequestParam int pageSize,
			@Parameter(description = "includeInactive") @RequestParam(required = false, defaultValue = "false") boolean includeInactive) {

		Page<com.iso.hypo.membership.application.dto.MembershipDto> domainDtos = null;
		try {
			domainDtos = membershipQueryService.list(brandUuid, page, pageSize, includeInactive);
		} catch (MembershipException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, null);
		}

		return ResponseEntity.ok(domainDtos.map(item -> modelMapper.map(item, MembershipDto.class)));
	}

	@GetMapping("/brands/{brandUuid}/memberships/{uuid}")
	@Operation(summary = "Retrieve a specific membership")
	@ApiResponses({ @ApiResponse(responseCode = "200", content = {
			@Content(schema = @Schema(implementation = MembershipDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> getMembership(
			@PathVariable String brandUuid,
			@PathVariable String uuid) {
		com.iso.hypo.membership.application.dto.MembershipDto domainDto = null;
		try {
			domainDto = membershipQueryService.find(brandUuid, uuid);
		} catch (MembershipException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, uuid);
		}

		return ResponseEntity.ok(modelMapper.map(domainDto, MembershipDto.class));
	}
}