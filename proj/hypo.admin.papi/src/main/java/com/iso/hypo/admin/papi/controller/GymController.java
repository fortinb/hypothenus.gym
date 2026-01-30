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
import com.iso.hypo.admin.papi.controller.util.ControllerErrorHandler;
import com.iso.hypo.admin.papi.dto.ErrorDto;
import com.iso.hypo.admin.papi.dto.model.GymDto;
import com.iso.hypo.admin.papi.dto.patch.PatchGymDto;
import com.iso.hypo.admin.papi.dto.post.PostGymDto;
import com.iso.hypo.admin.papi.dto.put.PutGymDto;
import com.iso.hypo.admin.papi.dto.search.GymSearchDto;
import com.iso.hypo.common.context.RequestContext;
import com.iso.hypo.services.GymQueryService;
import com.iso.hypo.services.GymService;
import com.iso.hypo.services.exception.GymException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/v1")
@Validated
public class GymController {

	private static final Logger logger = LoggerFactory.getLogger(GymController.class);

	private final ModelMapper modelMapper;
	private final RequestContext requestContext;

	private final GymService gymService;
	private final GymQueryService gymQueryService;

	public GymController(ModelMapper modelMapper, GymService gymService, GymQueryService gymQueryService, RequestContext requestContext) {
		this.modelMapper = modelMapper;
		this.gymService = gymService;
		this.gymQueryService = gymQueryService;
		this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
	}

	@GetMapping("/brands/{brandUuid}/gyms/search")
	@Operation(summary = "Search for gyms")
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
	@PreAuthorize("hasRole('" + Roles.Admin + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> searchGym(
			@PathVariable String brandUuid,
			@Parameter(description = "search criteria") @RequestParam String criteria,
			@Parameter(description = "page number") @RequestParam int page,
			@Parameter(description = "page size") @RequestParam int pageSize,
			@Parameter(description = "includeInactive") @RequestParam(required = false, defaultValue = "false") boolean includeInactive) {

		Page<com.iso.hypo.domain.dto.search.GymSearchDto> domainDtos = null;
		try {
			domainDtos = gymQueryService.search(page, pageSize, criteria, includeInactive);
		} catch (GymException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, criteria);
		}

		return ResponseEntity.ok(domainDtos.map(item -> modelMapper.map(item, GymSearchDto.class)));
	}

	@GetMapping("/brands/{brandUuid}/gyms")
	@PreAuthorize("hasRole('" + Roles.Admin + "')")
	@Operation(summary = "Retrieve a list of brands")
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
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> listGym(
			@PathVariable String brandUuid,
			@Parameter(description = "page number") @RequestParam int page,
			@Parameter(description = "page size") @RequestParam int pageSize,
			@Parameter(description = "includeInactive") @RequestParam(required = false, defaultValue = "false") boolean includeInactive) {

		Page<com.iso.hypo.domain.dto.GymDto> domainDtos = null;
		try {
			domainDtos = gymQueryService.list(brandUuid, page, pageSize, includeInactive);
		} catch (GymException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, null);

		}

		return ResponseEntity.ok(domainDtos.map(item -> modelMapper.map(item, GymDto.class)));
	}

	@GetMapping("/brands/{brandUuid}/gyms/{uuid}")
	@Operation(summary = "Retrieve a specific gym")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = GymDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "403", description = "Forbidden. The client does not have permission to access this resource.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found. The requested resource does not exist.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected server error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "','" + Roles.Member + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> getGym(
			@PathVariable String brandUuid,
			@PathVariable String uuid) {
		com.iso.hypo.domain.dto.GymDto domainDto = null;
		try {
			domainDto = gymQueryService.find(brandUuid, uuid);
		} catch (GymException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, uuid);
		}

		return ResponseEntity.ok(modelMapper.map(domainDto, GymDto.class));
	}

	@PostMapping("/brands/{brandUuid}/gyms")
	@Operation(summary = "Create a new gym")
	@ApiResponses({
			@ApiResponse(responseCode = "201", content = {
					@Content(schema = @Schema(implementation = GymDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasRole('" + Roles.Admin + "')")
	@ResponseStatus(value = HttpStatus.CREATED)
	public ResponseEntity<Object> createGym(
			@PathVariable String brandUuid,
			@RequestBody PostGymDto request) {
		
		if (!request.getBrandUuid().equals(brandUuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		
		com.iso.hypo.domain.dto.GymDto domainDto = modelMapper.map(request, com.iso.hypo.domain.dto.GymDto.class);
		
		try {
			domainDto = gymService.create(domainDto);
		} catch (GymException e) {
			logger.error(e.getMessage(), e);
			
			if (e.getCode() == GymException.GYM_CODE_ALREADY_EXIST) {
				return ResponseEntity.status(HttpStatus.OK).body(modelMapper.map(e.getGymDto(), GymDto.class));
			}

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, null);
		}

		return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(domainDto.getUuid()).toUri()).body(modelMapper.map(domainDto, GymDto.class));
	}

	@PutMapping("/brands/{brandUuid}/gyms/{uuid}")
	@Operation(summary = "Update a gym")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = GymDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "403", description = "Forbidden. The client does not have permission to access this resource.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found. The requested resource does not exist.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> updateGym(
			@PathVariable String brandUuid,
			@PathVariable String uuid, 
			@RequestBody PutGymDto request) {
		
		if (!request.getBrandUuid().equals(brandUuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		
		if (!request.getUuid().equals(uuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		
		com.iso.hypo.domain.dto.GymDto domainDto = modelMapper.map(request, com.iso.hypo.domain.dto.GymDto.class);

		try {
			domainDto = gymService.update(domainDto);
		} catch (GymException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, uuid);
		}

		return ResponseEntity.ok(modelMapper.map(domainDto, GymDto.class));
	}

	@PatchMapping("/brands/{brandUuid}/gyms/{uuid}")
	@Operation(summary = "Patch a gym")
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = GymDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "403", description = "Forbidden. The client does not have permission to access this resource.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found. The requested resource does not exist.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> patchGym(
			@PathVariable String brandUuid,
			@PathVariable String uuid,
			@RequestBody PatchGymDto request) {
		
		if (!request.getBrandUuid().equals(brandUuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		
		if (!request.getUuid().equals(uuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		
		com.iso.hypo.domain.dto.GymDto domainDto = modelMapper.map(request, com.iso.hypo.domain.dto.GymDto.class);

		try {
			domainDto = gymService.patch(domainDto);
		} catch (GymException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, uuid);
		}

		return ResponseEntity.ok(modelMapper.map(domainDto, GymDto.class));
	}

	@PostMapping("/brands/{brandUuid}/gyms/{uuid}/activate")
	@Operation(summary = "Activate a gym")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = GymDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "403", description = "Forbidden. The client does not have permission to access this resource.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found. The requested resource does not exist.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> activateGym(
			@PathVariable String brandUuid,
			@PathVariable String uuid) {
		com.iso.hypo.domain.dto.GymDto domainDto;

		try {
			domainDto = gymService.activate(brandUuid, uuid);
		} catch (GymException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, uuid);
		}

		return ResponseEntity.ok(modelMapper.map(domainDto, GymDto.class));
	}

	@PostMapping("/brands/{brandUuid}/gyms/{uuid}/deactivate")
	@Operation(summary = "Deactivate a gym")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = GymDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "403", description = "Forbidden. The client does not have permission to access this resource.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found. The requested resource does not exist.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> deactivateGym(
			@PathVariable String brandUuid,
			@PathVariable String uuid) {

		com.iso.hypo.domain.dto.GymDto domainDto;

		try {
			domainDto = gymService.deactivate(brandUuid, uuid);
		} catch (GymException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, uuid);
		}

		return ResponseEntity.ok(modelMapper.map(domainDto, GymDto.class));
	}

	@DeleteMapping("/brands/{brandUuid}/gyms/{uuid}")
	@Operation(summary = "Delete a gym")
	@ApiResponses({ @ApiResponse(responseCode = "202"),
			@ApiResponse(responseCode = "403", description = "Forbidden. The client does not have permission to access this resource.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found. The requested resource does not exist.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasRole('" + Roles.Admin + "')")
	@ResponseStatus(value = HttpStatus.ACCEPTED)
	public ResponseEntity<Object> deleteGym(
			@PathVariable String brandUuid,
			@PathVariable String uuid) {
		try {
			gymService.delete(brandUuid, uuid);
		} catch (GymException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, uuid);
		}

		return ResponseEntity.accepted().build();
	}
}