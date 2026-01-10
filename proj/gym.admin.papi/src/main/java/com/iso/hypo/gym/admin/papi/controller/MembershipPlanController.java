package com.iso.hypo.gym.admin.papi.controller;

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

import com.iso.hypo.gym.admin.papi.config.security.Roles;
import com.iso.hypo.gym.admin.papi.dto.ErrorDto;
import com.iso.hypo.gym.admin.papi.dto.model.MembershipPlanDto;
import com.iso.hypo.gym.admin.papi.dto.patch.PatchMembershipPlanDto;
import com.iso.hypo.gym.admin.papi.dto.post.PostMembershipPlanDto;
import com.iso.hypo.gym.admin.papi.dto.put.PutMembershipPlanDto;
import com.iso.hypo.common.exception.DomainException;
import com.iso.hypo.membershipPlan.domain.aggregate.MembershipPlan;
import com.iso.hypo.membershipPlan.services.MembershipPlanService;

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

	@GetMapping("/brands/{brandId}/membership/plans")
	@Operation(summary = "Retrieve a list of membership plans")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = Page.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> listMembershipPlans(@PathVariable("brandId") String brandId,
			@Parameter(description = "page number") @RequestParam(name = "page", required = true) int page,
			@Parameter(description = "page size") @RequestParam(name = "pageSize", required = true) int pageSize,
			@Parameter(description = "includeInactive") @RequestParam(name = "includeInactive", required = false, defaultValue = "false") boolean includeInactive) {

		Page<MembershipPlan> entities = null;
		try {
			entities = membershipPlanService.list(brandId, page, pageSize, includeInactive);
		} catch (DomainException e) {
			logger.error(e.getMessage(), e);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), null));
		}

		return ResponseEntity.ok(entities.map(item -> modelMapper.map(item, MembershipPlanDto.class)));
	}

	@GetMapping("/brands/{brandId}/membership/plans/{membershipPlansId}")
	@Operation(summary = "Retrieve a specific membership plan")
	@ApiResponses({ @ApiResponse(responseCode = "200", content = {
			@Content(schema = @Schema(implementation = MembershipPlanDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> getMembershipPlan(@PathVariable("brandId") String brandId,
			@PathVariable("membershipPlansId") String membershipPlansId) {
		MembershipPlan entity = null;
		try {
			entity = membershipPlanService.findByMembershipPlanId(brandId, membershipPlansId);
		} catch (DomainException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == DomainException.MEMBERSHIPPLAN_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), membershipPlansId));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), membershipPlansId));
		}

		return ResponseEntity.ok(modelMapper.map(entity, MembershipPlanDto.class));
	}

	@PostMapping("/brands/{brandId}/membership/plans")
	@Operation(summary = "Create a new membership plan")
	@ApiResponses({ @ApiResponse(responseCode = "201", content = {
			@Content(schema = @Schema(implementation = MembershipPlanDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.CREATED)
	public ResponseEntity<Object> createMembershipPlan(@PathVariable("brandId") String brandId,
			@RequestBody PostMembershipPlanDto request) {
		MembershipPlan entity = modelMapper.map(request, MembershipPlan.class);

		try {
			membershipPlanService.create(brandId, entity);
		} catch (DomainException e) {
			logger.error(e.getMessage(), e);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}

		return ResponseEntity.created(
				ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(entity.getId()).toUri())
				.body(modelMapper.map(entity, MembershipPlanDto.class));
	}

	@PutMapping("/brands/{brandId}/membership/plans/{membershipPlansId}")
	@Operation(summary = "Update a membership plan")
	@ApiResponses({ @ApiResponse(responseCode = "200", content = {
			@Content(schema = @Schema(implementation = MembershipPlanDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> updateMembershipPlan(@PathVariable("brandId") String brandId,
			@PathVariable("membershipPlansId") String membershipPlansId,
			@Parameter(description = "activate or deactivate MembershipPlan") @RequestParam(name = "isActive", required = false, defaultValue = "true") boolean isActive,
			@RequestBody PutMembershipPlanDto request) {
		MembershipPlan entity = modelMapper.map(request, MembershipPlan.class);

		try {
			entity = membershipPlanService.update(brandId, entity);
		} catch (DomainException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == DomainException.MEMBERSHIPPLAN_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), membershipPlansId));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), membershipPlansId));
		}

		return ResponseEntity.ok(modelMapper.map(entity, MembershipPlanDto.class));
	}

	@PostMapping("/brands/{brandId}/membership/plans/{membershipPlansId}/activate")
	@Operation(summary = "Activate a membership plan")
	@ApiResponses({ @ApiResponse(responseCode = "200", content = {
			@Content(schema = @Schema(implementation = MembershipPlanDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> activateMembershipPlan(@PathVariable("brandId") String brandId,
			@PathVariable("membershipPlansId") String membershipPlansId) {
		MembershipPlan entity;

		try {
			entity = membershipPlanService.activate(brandId, membershipPlansId);
		} catch (DomainException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == DomainException.MEMBERSHIPPLAN_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), membershipPlansId));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), membershipPlansId));
		}

		return ResponseEntity.ok(modelMapper.map(entity, MembershipPlanDto.class));
	}

	@PostMapping("/brands/{brandId}/membership/plans/{membershipPlansId}/deactivate")
	@Operation(summary = "Deactivate a membership plan")
	@ApiResponses({ @ApiResponse(responseCode = "200", content = {
			@Content(schema = @Schema(implementation = MembershipPlanDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> deactivateMembershipPlan(@PathVariable("brandId") String brandId,
			@PathVariable("membershipPlansId") String membershipPlansId) {
		MembershipPlan entity;

		try {
			entity = membershipPlanService.deactivate(brandId, membershipPlansId);
		} catch (DomainException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == DomainException.MEMBERSHIPPLAN_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), membershipPlansId));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), membershipPlansId));
		}

		return ResponseEntity.ok(modelMapper.map(entity, MembershipPlanDto.class));
	}

	@PatchMapping("/brands/{brandId}/membership/plans/{membershipPlansId}")
	@Operation(summary = "Patch a membership plan")
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ApiResponses({ @ApiResponse(responseCode = "200", content = {
			@Content(schema = @Schema(implementation = MembershipPlanDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> patchMembershipPlan(@PathVariable("brandId") String brandId,
			@PathVariable("membershipPlansId") String membershipPlansId, @RequestBody PatchMembershipPlanDto request) {
		MembershipPlan entity = modelMapper.map(request, MembershipPlan.class);

		try {
			entity = membershipPlanService.patch(brandId, entity);
		} catch (DomainException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == DomainException.MEMBERSHIPPLAN_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), membershipPlansId));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), membershipPlansId));
		}

		return ResponseEntity.ok(modelMapper.map(entity, MembershipPlanDto.class));
	}

	@DeleteMapping("/brands/{brandId}/membership/plans/{membershipPlansId}")
	@Operation(summary = "Delete a membership plan")
	@ApiResponses({ @ApiResponse(responseCode = "202"),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.ACCEPTED)
	public ResponseEntity<Object> deleteMembershipPlan(@PathVariable("brandId") String brandId,
			@PathVariable("membershipPlansId") String membershipPlansId) {
		try {
			membershipPlanService.delete(brandId, membershipPlansId);
		} catch (DomainException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == DomainException.MEMBERSHIPPLAN_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), membershipPlansId));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), membershipPlansId));
		}

		return ResponseEntity.ok(membershipPlansId);
	}
}
