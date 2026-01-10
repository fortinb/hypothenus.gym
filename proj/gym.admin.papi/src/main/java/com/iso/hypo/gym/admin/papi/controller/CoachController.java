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
import com.iso.hypo.gym.admin.papi.dto.model.CoachDto;
import com.iso.hypo.gym.admin.papi.dto.patch.PatchCoachDto;
import com.iso.hypo.gym.admin.papi.dto.post.PostCoachDto;
import com.iso.hypo.gym.admin.papi.dto.put.PutCoachDto;
import com.iso.hypo.common.exception.DomainException;
import com.iso.hypo.coach.domain.aggregate.Coach;
import com.iso.hypo.coach.services.CoachService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/v1")
@Validated
public class CoachController {

	@Autowired
	private Logger logger;

	@Autowired
	private ModelMapper modelMapper;

	private CoachService coachService;

	public CoachController(CoachService coachService) {
		this.coachService = coachService;
	}

	@GetMapping("/brands/{brandId}/gyms/{gymId}/coachs")
	@Operation(summary = "Retrieve a list of coachs")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = Page.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> listCoach(
			@PathVariable("brandId") String brandId,
			@PathVariable("gymId") String gymId,
			@Parameter(description = "page number") @RequestParam(name = "page", required = true) int page,
			@Parameter(description = "page size") @RequestParam(name = "pageSize", required = true) int pageSize,
			@Parameter(description = "includeInactive") @RequestParam(name = "includeInactive", required = false, defaultValue="false") boolean includeInactive) {

		Page<Coach> entities = null;
		try {
			entities = coachService.list(brandId, gymId, page, pageSize, includeInactive);
		} catch (DomainException e) {
			logger.error(e.getMessage(), e);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), null));

		}

		return ResponseEntity.ok(entities.map(item -> modelMapper.map(item, CoachDto.class)));
	}

	@GetMapping("/brands/{brandId}/gyms/{gymId}/coachs/{coachId}")
	@Operation(summary = "Retrieve a specific coach")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = CoachDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> getCoach(
			@PathVariable("brandId") String brandId,
			@PathVariable("gymId") String gymId,
			@PathVariable("coachId") String coachId) {
		Coach entity = null;
		try {
			entity = coachService.findByCoachId(brandId, gymId, coachId);
		} catch (DomainException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == DomainException.COACH_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), coachId));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), coachId));
		}

		return ResponseEntity.ok(modelMapper.map(entity, CoachDto.class));
	}

	@PostMapping("/brands/{brandId}/gyms/{gymId}/coachs")
	@Operation(summary = "Create a new coach")
	@ApiResponses({
			@ApiResponse(responseCode = "201", content = {
					@Content(schema = @Schema(implementation = CoachDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.CREATED)
	public ResponseEntity<Object> createCoach(
			@PathVariable("brandId") String brandId,
			@PathVariable("gymId") String gymId,
			@RequestBody PostCoachDto request) {
		Coach entity = modelMapper.map(request, Coach.class);

		try {
			coachService.create(brandId, gymId, entity);
		} catch (DomainException e) {
			logger.error(e.getMessage(), e);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}

		return ResponseEntity.created(
				ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(entity.getId()).toUri())
				.body(modelMapper.map(entity, CoachDto.class));
	}

	@PutMapping("/brands/{brandId}/gyms/{gymId}/coachs/{coachId}")
	@Operation(summary = "Update a coach")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = CoachDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> updateCoach(
			@PathVariable("brandId") String brandId,
			@PathVariable("gymId") String gymId,
			@PathVariable("coachId") String coachId,
			@Parameter(description = "activate or deactivate coach") @RequestParam(name = "isActive", required = false, defaultValue = "true") boolean isActive,
			@RequestBody PutCoachDto request) {
		Coach entity = modelMapper.map(request, Coach.class);
		
		try {
			entity = coachService.update(brandId, gymId, entity);
		} catch (DomainException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == DomainException.COACH_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), coachId));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), coachId));
		}

		return ResponseEntity.ok(modelMapper.map(entity, CoachDto.class));
	}

	@PostMapping("/brands/{brandId}/gyms/{gymId}/coachs/{coachId}/activate")
	@Operation(summary = "Activate a coach")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = CoachDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> activateCoach(
			@PathVariable("brandId") String brandId,
			@PathVariable("gymId") String gymId,
			@PathVariable("coachId") String coachId) {
		Coach entity;
		
		try {
			entity = coachService.activate(brandId, gymId, coachId);
		} catch (DomainException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == DomainException.COACH_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), coachId));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), coachId));
		}

		return ResponseEntity.ok(modelMapper.map(entity, CoachDto.class));
	}
	
	@PostMapping("/brands/{brandId}/gyms/{gymId}/coachs/{coachId}/deactivate")
	@Operation(summary = "Deactivate a coach")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = CoachDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> deactivateCoach(
			@PathVariable("brandId") String brandId,
			@PathVariable("gymId") String gymId,
			@PathVariable("coachId") String coachId) {
		Coach entity;
		
		try {
			entity = coachService.deactivate(brandId, gymId, coachId);
		} catch (DomainException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == DomainException.COACH_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), coachId));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), coachId));
		}

		return ResponseEntity.ok(modelMapper.map(entity, CoachDto.class));
	}
	
	@PatchMapping("/brands/{brandId}/gyms/{gymId}/coachs/{coachId}")
	@Operation(summary = "Patch a coach")
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = CoachDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> patchCoach(
			@PathVariable("brandId") String brandId,
			@PathVariable("gymId") String gymId,
			@PathVariable("coachId") String coachId,
			@RequestBody PatchCoachDto request) {
		Coach entity = modelMapper.map(request, Coach.class);
		
		try {
			entity = coachService.patch(brandId, gymId, entity);
		} catch (DomainException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == DomainException.COACH_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), coachId));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), coachId));
		}

		return ResponseEntity.ok(modelMapper.map(entity, CoachDto.class));
	}

	@DeleteMapping("/brands/{brandId}/gyms/{gymId}/coachs/{coachId}")
	@Operation(summary = "Delete a coach")
	@ApiResponses({ @ApiResponse(responseCode = "202"),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.ACCEPTED)
	public ResponseEntity<Object> deleteCoach(
			@PathVariable("brandId") String brandId,
			@PathVariable("gymId") String gymId,
			@PathVariable("coachId") String coachId) {
		try {
			coachService.delete(brandId, gymId, coachId);
		} catch (DomainException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == DomainException.COACH_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), coachId));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), coachId));
		}

		return ResponseEntity.ok(coachId);
	}
}
