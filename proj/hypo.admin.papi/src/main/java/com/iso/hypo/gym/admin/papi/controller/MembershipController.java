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
import com.iso.hypo.gym.admin.papi.dto.model.MembershipDto;
import com.iso.hypo.gym.admin.papi.dto.patch.PatchMembershipDto;
import com.iso.hypo.gym.admin.papi.dto.post.PostMembershipDto;
import com.iso.hypo.gym.admin.papi.dto.put.PutMembershipDto;
import com.iso.hypo.member.exception.MemberException;
import com.iso.hypo.member.services.MembershipService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.*;

@RestController
@RequestMapping("/v1")
@Validated
public class MembershipController {

	@Autowired
	private Logger logger;

	@Autowired
	private ModelMapper modelMapper;

	private MembershipService membershipService;

	public MembershipController(MembershipService membershipService) {
		this.membershipService = membershipService;
	}

	@GetMapping("/brands/{brandId}/memberships")
	@Operation(summary = "Retrieve a list of memberships")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = Page.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> listMemberships(@PathVariable("brandId") String brandId,
			@Parameter(description = "page number") @RequestParam(name = "page", required = true) int page,
			@Parameter(description = "page size") @RequestParam(name = "pageSize", required = true) int pageSize,
			@Parameter(description = "includeInactive") @RequestParam(name = "includeInactive", required = false, defaultValue = "false") boolean includeInactive) {

		Page<com.iso.hypo.member.dto.MembershipDto> entities = null;
		try {
			entities = membershipService.list(brandId, page, pageSize, includeInactive);
		} catch (MemberException e) {
			logger.error(e.getMessage(), e);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), null));
		}

		return ResponseEntity.ok(entities.map(item -> modelMapper.map(item, MembershipDto.class)));
	}

	@GetMapping("/brands/{brandId}/memberships/{membershipId}")
	@Operation(summary = "Retrieve a specific membership")
	@ApiResponses({ @ApiResponse(responseCode = "200", content = {
			@Content(schema = @Schema(implementation = MembershipDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> getMembership(@PathVariable("brandId") String brandId,
			@PathVariable("membershipId") String membershipId) {
		com.iso.hypo.member.dto.MembershipDto entity = null;
		try {
			entity = membershipService.findByMembershipUuid(brandId, membershipId);
		} catch (MemberException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == MemberException.MEMBERSHIP_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), membershipId));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), membershipId));
		}

		return ResponseEntity.ok(modelMapper.map(entity, MembershipDto.class));
	}

	@PostMapping("/brands/{brandId}/memberships")
	@Operation(summary = "Create a new membership")
	@ApiResponses({ @ApiResponse(responseCode = "201", content = {
			@Content(schema = @Schema(implementation = MembershipDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.CREATED)
	public ResponseEntity<Object> createMembership(@PathVariable("brandId") String brandId,
			@RequestBody PostMembershipDto request) {
		com.iso.hypo.member.dto.MembershipDto domainDto = modelMapper.map(request, com.iso.hypo.member.dto.MembershipDto.class);

		try {
			domainDto = membershipService.create(brandId, domainDto);
		} catch (MemberException e) {
			logger.error(e.getMessage(), e);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}

		return ResponseEntity.created(
				ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(domainDto.getUuid()).toUri())
				.body(modelMapper.map(domainDto, MembershipDto.class));
	}

	@PutMapping("/brands/{brandId}/memberships/{membershipId}")
	@Operation(summary = "Update a membership")
	@ApiResponses({ @ApiResponse(responseCode = "200", content = {
			@Content(schema = @Schema(implementation = MembershipDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> updateMembership(@PathVariable("brandId") String brandId,
			@PathVariable("membershipId") String membershipId,
			@Parameter(description = "activate or deactivate Membership") @RequestParam(name = "isActive", required = false, defaultValue = "true") boolean isActive,
			@RequestBody PutMembershipDto request) {
		com.iso.hypo.member.dto.MembershipDto domainDto = modelMapper.map(request, com.iso.hypo.member.dto.MembershipDto.class);

		try {
			domainDto = membershipService.update(brandId, domainDto);
		} catch (MemberException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == MemberException.MEMBERSHIP_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), membershipId));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), membershipId));
		}

		return ResponseEntity.ok(modelMapper.map(domainDto, MembershipDto.class));
	}

	@PostMapping("/brands/{brandId}/memberships/{membershipId}/activate")
	@Operation(summary = "Activate a membership")
	@ApiResponses({ @ApiResponse(responseCode = "200", content = {
			@Content(schema = @Schema(implementation = MembershipDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> activateMembership(@PathVariable("brandId") String brandId,
			@PathVariable("membershipId") String membershipId) {
		com.iso.hypo.member.dto.MembershipDto entity;

		try {
			entity = membershipService.activate(brandId, membershipId);
		} catch (MemberException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == MemberException.MEMBERSHIP_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), membershipId));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), membershipId));
		}

		return ResponseEntity.ok(modelMapper.map(entity, MembershipDto.class));
	}

	@PostMapping("/brands/{brandId}/memberships/{membershipId}/deactivate")
	@Operation(summary = "Deactivate a membership")
	@ApiResponses({ @ApiResponse(responseCode = "200", content = {
			@Content(schema = @Schema(implementation = MembershipDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> deactivateMembership(@PathVariable("brandId") String brandId,
			@PathVariable("membershipId") String membershipId) {
		com.iso.hypo.member.dto.MembershipDto entity;

		try {
			entity = membershipService.deactivate(brandId, membershipId);
		} catch (MemberException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == MemberException.MEMBERSHIP_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), membershipId));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), membershipId));
		}

		return ResponseEntity.ok(modelMapper.map(entity, MembershipDto.class));
	}

	@PatchMapping("/brands/{brandId}/memberships/{membershipId}")
	@Operation(summary = "Patch a membership")
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ApiResponses({ @ApiResponse(responseCode = "200", content = {
			@Content(schema = @Schema(implementation = MembershipDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> patchMembership(@PathVariable("brandId") String brandId,
			@PathVariable("membershipId") String membershipId, @RequestBody PatchMembershipDto request) {
		com.iso.hypo.member.dto.MembershipDto domainDto = modelMapper.map(request, com.iso.hypo.member.dto.MembershipDto.class);

		try {
			domainDto = membershipService.patch(brandId, domainDto);
		} catch (MemberException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == MemberException.MEMBERSHIP_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), membershipId));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), membershipId));
		}

		return ResponseEntity.ok(modelMapper.map(domainDto, MembershipDto.class));
	}

	@DeleteMapping("/brands/{brandId}/memberships/{membershipId}")
	@Operation(summary = "Delete a membership")
	@ApiResponses({ @ApiResponse(responseCode = "202"),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.ACCEPTED)
	public ResponseEntity<Object> deleteMembership(@PathVariable("brandId") String brandId,
			@PathVariable("membershipId") String membershipId) {
		try {
			membershipService.delete(brandId, membershipId);
		} catch (MemberException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == MemberException.MEMBERSHIP_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), membershipId));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), membershipId));
		}

		return ResponseEntity.ok(membershipId);
	}
}