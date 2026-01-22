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
import com.iso.hypo.admin.papi.dto.model.CoachDto;
import com.iso.hypo.admin.papi.dto.patch.PatchCoachDto;
import com.iso.hypo.admin.papi.dto.post.PostCoachDto;
import com.iso.hypo.admin.papi.dto.put.PutCoachDto;
import com.iso.hypo.services.exception.CoachException;
import com.iso.hypo.services.CoachService;

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

	@GetMapping("/brands/{brandUuid}/gyms/{gymUuid}/coachs")
	@Operation(summary = "Retrieve a list of coachs")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = Page.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> listCoach(
			@PathVariable String brandUuid,
			@PathVariable String gymUuid,
			@Parameter(description = "page number") @RequestParam int page,
			@Parameter(description = "page size") @RequestParam int pageSize,
			@Parameter(description = "includeInactive") @RequestParam(required = false, defaultValue="false") boolean includeInactive) {

		Page<com.iso.hypo.domain.dto.CoachDto> entities = null;
		try {
			entities = coachService.list(brandUuid, gymUuid, page, pageSize, includeInactive);
		} catch (CoachException e) {
			logger.error(e.getMessage(), e);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), null));

		}

		return ResponseEntity.ok(entities.map(item -> modelMapper.map(item, CoachDto.class)));
	}

	@GetMapping("/brands/{brandUuid}/gyms/{gymUuid}/coachs/{uuid}")
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
			@PathVariable String brandUuid,
			@PathVariable String gymUuid,
			@PathVariable String uuid) {
		com.iso.hypo.domain.dto.CoachDto entity = null;
		try {
			entity = coachService.findByCoachUuid(brandUuid, gymUuid, uuid);
		} catch (CoachException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == CoachException.COACH_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), uuid));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), uuid));
		}

		return ResponseEntity.ok(modelMapper.map(entity, CoachDto.class));
	}

	@PostMapping("/brands/{brandUuid}/gyms/{gymUuid}/coachs")
	@Operation(summary = "Create a new coach")
	@ApiResponses({
			@ApiResponse(responseCode = "201", content = {
					@Content(schema = @Schema(implementation = CoachDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.CREATED)
	public ResponseEntity<Object> createCoach(
			@PathVariable String brandUuid,
			@PathVariable String gymUuid,
			@RequestBody PostCoachDto request) {
		
		if (!request.getBrandUuid().equals(brandUuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Brand UUID in path and request body do not match");
		}
		
		if (!request.getGymUuid().equals(gymUuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Gym UUID in path and request body do not match");
		}
		
		com.iso.hypo.domain.dto.CoachDto domainDto = modelMapper.map(request, com.iso.hypo.domain.dto.CoachDto.class);

		try {
			domainDto = coachService.create(brandUuid, gymUuid, domainDto);
		} catch (CoachException e) {
			logger.error(e.getMessage(), e);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}

		return ResponseEntity.created(
				ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(domainDto.getUuid()).toUri())
				.body(modelMapper.map(domainDto, CoachDto.class));
	}

	@PutMapping("/brands/{brandUuid}/gyms/{gymUuid}/coachs/{uuid}")
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
			@PathVariable String brandUuid,
			@PathVariable String gymUuid,
			@PathVariable String uuid,
			@Parameter(description = "activate or deactivate coach") 
			@RequestParam(required = false, defaultValue = "true") boolean isActive,
			@RequestBody PutCoachDto request) {
		
		if (!request.getBrandUuid().equals(brandUuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Brand UUID in path and request body do not match");
		}
		
		if (!request.getGymUuid().equals(gymUuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Gym UUID in path and request body do not match");
		}
		
		com.iso.hypo.domain.dto.CoachDto domainDto = modelMapper.map(request, com.iso.hypo.domain.dto.CoachDto.class);
		
		try {
			domainDto = coachService.update(brandUuid, gymUuid, domainDto);
		} catch (CoachException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == CoachException.COACH_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), uuid));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), uuid));
		}

		return ResponseEntity.ok(modelMapper.map(domainDto, CoachDto.class));
	}

	@PostMapping("/brands/{brandUuid}/gyms/{gymUuid}/coachs/{uuid}/activate")
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
			@PathVariable String brandUuid,
			@PathVariable String gymUuid,
			@PathVariable String uuid) {
		com.iso.hypo.domain.dto.CoachDto entity;
		
		try {
			entity = coachService.activate(brandUuid, gymUuid, uuid);
		} catch (CoachException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == CoachException.COACH_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), uuid));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), uuid));
		}

		return ResponseEntity.ok(modelMapper.map(entity, CoachDto.class));
	}
	
	@PostMapping("/brands/{brandUuid}/gyms/{gymUuid}/coachs/{uuid}/deactivate")
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
			@PathVariable String brandUuid,
			@PathVariable String gymUuid,
			@PathVariable String uuid) {
		com.iso.hypo.domain.dto.CoachDto entity;
		
		try {
			entity = coachService.deactivate(brandUuid, gymUuid, uuid);
		} catch (CoachException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == CoachException.COACH_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), uuid));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), uuid));
		}

		return ResponseEntity.ok(modelMapper.map(entity, CoachDto.class));
	}
	
	@PatchMapping("/brands/{brandUuid}/gyms/{gymUuid}/coachs/{uuid}")
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
			@PathVariable String brandUuid,
			@PathVariable String gymUuid,
			@PathVariable String uuid,
			@RequestBody PatchCoachDto request) {
		
		if (!request.getBrandUuid().equals(brandUuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Brand UUID in path and request body do not match");
		}
		
		if (!request.getGymUuid().equals(gymUuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Gym UUID in path and request body do not match");
		}
		
		com.iso.hypo.domain.dto.CoachDto domainDto = modelMapper.map(request, com.iso.hypo.domain.dto.CoachDto.class);
		
		try {
			domainDto = coachService.patch(brandUuid, gymUuid, domainDto);
		} catch (CoachException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == CoachException.COACH_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), uuid));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), uuid));
		}

		return ResponseEntity.ok(modelMapper.map(domainDto, CoachDto.class));
	}

	@DeleteMapping("/brands/{brandUuid}/gyms/{gymUuid}/coachs/{uuid}")
	@Operation(summary = "Delete a coach")
	@ApiResponses({ @ApiResponse(responseCode = "202"),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.ACCEPTED)
	public ResponseEntity<Object> deleteCoach(
			@PathVariable String brandUuid,
			@PathVariable String gymUuid,
			@PathVariable String uuid) {
		try {
			coachService.delete(brandUuid, gymUuid, uuid);
		} catch (CoachException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == CoachException.COACH_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), uuid));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), uuid));
		}

		return ResponseEntity.ok(uuid);
	}
}

