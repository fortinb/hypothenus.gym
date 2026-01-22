package com.iso.hypo.admin.papi.controller;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.iso.hypo.services.MembershipPlanService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.*;

@RestController
@RequestMapping("/v1")
@Validated
public class MembershipPlanController {

	@Autowired
	private Logger logger;

	@Autowired
	private ModelMapper modelMapper;

	private MembershipPlanService membershipPlanService;

	public MembershipPlanController(MembershipPlanService membershipPlanService) {
		this.membershipPlanService = membershipPlanService;
	}

	@GetMapping("/brands/{brandUuid}/membership/plans")
	@Operation(summary = "Retrieve a list of membership plans")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = Page.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> listMembershipPlans(@PathVariable String brandUuid,
			@Parameter(description = "page number") @RequestParam int page,
			@Parameter(description = "page size") @RequestParam int pageSize,
			@Parameter(description = "includeInactive") @RequestParam(required = false, defaultValue = "false") boolean includeInactive) {

		Page<com.iso.hypo.domain.dto.MembershipPlanDto> entities = null;
		try {
			entities = membershipPlanService.list(brandUuid, page, pageSize, includeInactive);
		} catch (MembershipPlanException e) {
			logger.error(e.getMessage(), e);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), null));
		}

		return ResponseEntity.ok(entities.map(item -> modelMapper.map(item, MembershipPlanDto.class)));
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
		com.iso.hypo.domain.dto.MembershipPlanDto entity = null;
		try {
			entity = membershipPlanService.findByMembershipPlanUuid(brandUuid, uuid);
		} catch (MembershipPlanException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == MembershipPlanException.MEMBERSHIPPLAN_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), uuid));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), uuid));
		}

		return ResponseEntity.ok(modelMapper.map(entity, MembershipPlanDto.class));
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
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Brand UUID in path and request body do not match");
		}
		
		com.iso.hypo.domain.dto.MembershipPlanDto domainDto = modelMapper.map(request, com.iso.hypo.domain.dto.MembershipPlanDto.class);

		try {
			domainDto = membershipPlanService.create(brandUuid, domainDto);
		} catch (MembershipPlanException e) {
			logger.error(e.getMessage(), e);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
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
	public ResponseEntity<Object> updateMembershipPlan(@PathVariable String brandUuid,
			@PathVariable String uuid,
			@Parameter(description = "activate or deactivate MembershipPlan") @RequestParam(required = false, defaultValue = "true") boolean isActive,
			@RequestBody PutMembershipPlanDto request) {
		
		if (!request.getBrandUuid().equals(brandUuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Brand UUID in path and request body do not match");
		}
		
		com.iso.hypo.domain.dto.MembershipPlanDto domainDto = modelMapper.map(request, com.iso.hypo.domain.dto.MembershipPlanDto.class);

		try {
			domainDto = membershipPlanService.update(brandUuid, domainDto);
		} catch (MembershipPlanException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == MembershipPlanException.MEMBERSHIPPLAN_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), uuid));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), uuid));
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
		com.iso.hypo.domain.dto.MembershipPlanDto entity;

		try {
			entity = membershipPlanService.activate(brandUuid, uuid);
		} catch (MembershipPlanException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == MembershipPlanException.MEMBERSHIPPLAN_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), uuid));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), uuid));
		}

		return ResponseEntity.ok(modelMapper.map(entity, MembershipPlanDto.class));
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
		com.iso.hypo.domain.dto.MembershipPlanDto entity;

		try {
			entity = membershipPlanService.deactivate(brandUuid, uuid);
		} catch (MembershipPlanException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == MembershipPlanException.MEMBERSHIPPLAN_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), uuid));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), uuid));
		}

		return ResponseEntity.ok(modelMapper.map(entity, MembershipPlanDto.class));
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
	public ResponseEntity<Object> patchMembershipPlan(@PathVariable String brandUuid,
			@PathVariable String uuid, @RequestBody PatchMembershipPlanDto request) {
		
		if (!request.getBrandUuid().equals(brandUuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Brand UUID in path and request body do not match");
		}
		
		com.iso.hypo.domain.dto.MembershipPlanDto domainDto = modelMapper.map(request, com.iso.hypo.domain.dto.MembershipPlanDto.class);

		try {
			domainDto = membershipPlanService.patch(brandUuid, domainDto);
		} catch (MembershipPlanException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == MembershipPlanException.MEMBERSHIPPLAN_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), uuid));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), uuid));
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

			if (e.getCode() == MembershipPlanException.MEMBERSHIPPLAN_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), uuid));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), uuid));
		}

		return ResponseEntity.ok(uuid);
	}
}

