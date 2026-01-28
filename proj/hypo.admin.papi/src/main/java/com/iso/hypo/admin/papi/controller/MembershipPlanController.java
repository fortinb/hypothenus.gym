package com.iso.hypo.admin.papi.controller;

import java.util.Objects;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.iso.hypo.common.context.RequestContext;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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

import com.iso.hypo.admin.papi.config.security.Roles;
import com.iso.hypo.admin.papi.dto.ErrorDto;
import com.iso.hypo.admin.papi.dto.model.MembershipPlanDto;
import com.iso.hypo.admin.papi.dto.patch.PatchMembershipPlanDto;
import com.iso.hypo.admin.papi.dto.post.PostMembershipPlanDto;
import com.iso.hypo.admin.papi.dto.put.PutMembershipPlanDto;
import com.iso.hypo.services.exception.MembershipPlanException;
import com.iso.hypo.services.MembershipPlanQueryService;
import com.iso.hypo.services.MembershipPlanService;
import com.iso.hypo.admin.papi.controller.util.ControllerErrorHandler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.*;

@RestController
@RequestMapping("/v1")
@Validated
public class MembershipPlanController {

	private static final Logger logger = LoggerFactory.getLogger(MembershipPlanController.class);

	private final ModelMapper modelMapper;
	private final RequestContext requestContext;

	private final MembershipPlanService membershipPlanService;
	private final MembershipPlanQueryService membershipPlanQueryService;

	public MembershipPlanController(ModelMapper modelMapper, MembershipPlanService membershipPlanService, MembershipPlanQueryService membershipPlanQueryService, RequestContext requestContext) {
		this.modelMapper = modelMapper;
		this.membershipPlanService = membershipPlanService;
		this.membershipPlanQueryService = membershipPlanQueryService;
		this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
	}

	@GetMapping("/brands/{brandUuid}/membership/plans")
	@Operation(summary = "Retrieve a list of membership plans")
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
	public ResponseEntity<Object> listMembershipPlans(
			@PathVariable String brandUuid,
			@Parameter(description = "page number") @RequestParam int page,
			@Parameter(description = "page size") @RequestParam int pageSize,
			@Parameter(description = "includeInactive") @RequestParam(required = false, defaultValue = "false") boolean includeInactive) {

		Page<com.iso.hypo.domain.dto.MembershipPlanDto> domainDtos = null;
		try {
			domainDtos = membershipPlanQueryService.list(brandUuid, page, pageSize, includeInactive);
		} catch (MembershipPlanException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, null);
		}

		return ResponseEntity.ok(domainDtos.map(item -> modelMapper.map(item, MembershipPlanDto.class)));
	}

	@GetMapping("/brands/{brandUuid}/membership/plans/{uuid}")
	@Operation(summary = "Retrieve a specific membership plan")
	@ApiResponses({ @ApiResponse(responseCode = "200", content = {
			@Content(schema = @Schema(implementation = MembershipPlanDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> getMembershipPlan(
			@PathVariable String brandUuid,
			@PathVariable String uuid) {
		com.iso.hypo.domain.dto.MembershipPlanDto domainDto = null;
		try {
			domainDto = membershipPlanQueryService.find(brandUuid, uuid);
		} catch (MembershipPlanException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, uuid);
		}

		return ResponseEntity.ok(modelMapper.map(domainDto, MembershipPlanDto.class));
	}

	@PostMapping("/brands/{brandUuid}/membership/plans")
	@Operation(summary = "Create a new membership plan")
	@ApiResponses({ @ApiResponse(responseCode = "201", content = {
			@Content(schema = @Schema(implementation = MembershipPlanDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.CREATED)
	public ResponseEntity<Object> createMembershipPlan(
			@PathVariable String brandUuid,
			@RequestBody PostMembershipPlanDto request) {
		
		if (!request.getBrandUuid().equals(brandUuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		
		com.iso.hypo.domain.dto.MembershipPlanDto domainDto = modelMapper.map(request, com.iso.hypo.domain.dto.MembershipPlanDto.class);

		try {
			domainDto = membershipPlanService.create(domainDto);
		} catch (MembershipPlanException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, null);
		}

		return ResponseEntity.created(
				ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(domainDto.getUuid()).toUri())
				.body(modelMapper.map(domainDto, MembershipPlanDto.class));
	}

	@PutMapping("/brands/{brandUuid}/membership/plans/{uuid}")
	@Operation(summary = "Update a membership plan")
	@ApiResponses({ @ApiResponse(responseCode = "200", content = {
			@Content(schema = @Schema(implementation = MembershipPlanDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> updateMembershipPlan(
			@PathVariable String brandUuid,
			@PathVariable String uuid,
			@RequestParam(required = false, defaultValue = "true") boolean isActive,
			@RequestBody PutMembershipPlanDto request) {
		
		if (!request.getBrandUuid().equals(brandUuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		
		if (!request.getUuid().equals(uuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		
		com.iso.hypo.domain.dto.MembershipPlanDto domainDto = modelMapper.map(request, com.iso.hypo.domain.dto.MembershipPlanDto.class);

		try {
			domainDto = membershipPlanService.update(domainDto);
		} catch (MembershipPlanException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, uuid);
		}

		return ResponseEntity.ok(modelMapper.map(domainDto, MembershipPlanDto.class));
	}

	@PostMapping("/brands/{brandUuid}/membership/plans/{uuid}/activate")
	@Operation(summary = "Activate a membership plan")
	@ApiResponses({ @ApiResponse(responseCode = "200", content = {
			@Content(schema = @Schema(implementation = MembershipPlanDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> activateMembershipPlan(
			@PathVariable String brandUuid,
			@PathVariable String uuid) {
		com.iso.hypo.domain.dto.MembershipPlanDto domainDto;

		try {
			domainDto = membershipPlanService.activate(brandUuid, uuid);
		} catch (MembershipPlanException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, uuid);
		}

		return ResponseEntity.ok(modelMapper.map(domainDto, MembershipPlanDto.class));
	}

	@PostMapping("/brands/{brandUuid}/membership/plans/{uuid}/deactivate")
	@Operation(summary = "Deactivate a membership plan")
	@ApiResponses({ @ApiResponse(responseCode = "200", content = {
			@Content(schema = @Schema(implementation = MembershipPlanDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> deactivateMembershipPlan(
			@PathVariable String brandUuid,
			@PathVariable String uuid) {
		com.iso.hypo.domain.dto.MembershipPlanDto domainDto;

		try {
			domainDto = membershipPlanService.deactivate(brandUuid, uuid);
		} catch (MembershipPlanException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, uuid);
		}

		return ResponseEntity.ok(modelMapper.map(domainDto, MembershipPlanDto.class));
	}

	@PatchMapping("/brands/{brandUuid}/membership/plans/{uuid}")
	@Operation(summary = "Patch a membership plan")
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ApiResponses({ @ApiResponse(responseCode = "200", content = {
			@Content(schema = @Schema(implementation = MembershipPlanDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> patchMembershipPlan(
			@PathVariable String brandUuid,
			@PathVariable String uuid, 
			@RequestBody PatchMembershipPlanDto request) {
		
		if (!request.getBrandUuid().equals(brandUuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		
		if (!request.getUuid().equals(uuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		
		com.iso.hypo.domain.dto.MembershipPlanDto domainDto = modelMapper.map(request, com.iso.hypo.domain.dto.MembershipPlanDto.class);

		try {
			domainDto = membershipPlanService.patch(domainDto);
		} catch (MembershipPlanException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, uuid);
		}

		return ResponseEntity.ok(modelMapper.map(domainDto, MembershipPlanDto.class));
	}

	@DeleteMapping("/brands/{brandUuid}/membership/plans/{uuid}")
	@Operation(summary = "Delete a membership plan")
	@ApiResponses({ @ApiResponse(responseCode = "202"),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.ACCEPTED)
	public ResponseEntity<Object> deleteMembershipPlan(
			@PathVariable String brandUuid,
			@PathVariable String uuid) {
		try {
			membershipPlanService.delete(brandUuid, uuid);
		} catch (MembershipPlanException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, uuid);
		}

		return ResponseEntity.ok().build();
	}
}