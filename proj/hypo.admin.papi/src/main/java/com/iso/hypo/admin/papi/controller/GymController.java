package com.iso.hypo.admin.papi.controller;

import java.util.ArrayList;
import java.util.List;

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
import com.iso.hypo.admin.papi.dto.MessageDto;
import com.iso.hypo.admin.papi.dto.enumeration.MessageSeverityEnum;
import com.iso.hypo.admin.papi.dto.model.GymDto;
import com.iso.hypo.admin.papi.dto.patch.PatchGymDto;
import com.iso.hypo.admin.papi.dto.post.PostGymDto;
import com.iso.hypo.admin.papi.dto.put.PutGymDto;
import com.iso.hypo.admin.papi.dto.search.GymSearchDto;
import com.iso.hypo.services.exception.GymException;
import com.iso.hypo.services.GymQueryService;
import com.iso.hypo.services.GymService;

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

	@Autowired
	private Logger logger;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private com.iso.hypo.common.context.RequestContext requestContext;

	private GymService gymService;
	private GymQueryService gymQueryService;

	public GymController(GymService gymService, GymQueryService gymQueryService) {
		this.gymService = gymService;
		this.gymQueryService = gymQueryService;
	}

	@GetMapping("/brands/{brandUuid}/gyms/search")
	@Operation(summary = "Search for gyms")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = Page.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasRole('" + Roles.Admin + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> searchGym(
			@Parameter(description = "search criteria") @RequestParam String criteria,
			@Parameter(description = "page number") @RequestParam int page,
			@Parameter(description = "page size") @RequestParam int pageSize,
			@Parameter(description = "includeInactive") @RequestParam(required = false, defaultValue = "false") boolean includeInactive,
			@PathVariable String brandUuid) {

		Page<com.iso.hypo.domain.dto.GymSearchDto> entities = null;
		try {
			entities = gymQueryService.search(page, pageSize, criteria, includeInactive);
		} catch (GymException e) {
			logger.error(e.getMessage(), e);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(requestContext != null ? requestContext.getTrackingNumber() : null, e.getCode(), e.getMessage(), criteria));
		}

		return ResponseEntity.ok(entities.map(item -> modelMapper.map(item, GymSearchDto.class)));
	}

	@GetMapping("/brands/{brandUuid}/gyms")
	@PreAuthorize("hasRole('" + Roles.Admin + "')")
	@Operation(summary = "Retrieve a list of brands")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = Page.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> listGym(
			@Parameter(description = "page number") @RequestParam int page,
			@Parameter(description = "page size") @RequestParam int pageSize,
			@Parameter(description = "includeInactive") @RequestParam(required = false, defaultValue = "false") boolean includeInactive,
			@PathVariable String brandUuid) {

		Page<com.iso.hypo.domain.dto.GymDto> entities = null;
		try {
			entities = gymQueryService.list(brandUuid, page, pageSize, includeInactive);
		} catch (GymException e) {
			logger.error(e.getMessage(), e);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(requestContext != null ? requestContext.getTrackingNumber() : null, e.getCode(), e.getMessage(), null));

		}

		return ResponseEntity.ok(entities.map(item -> modelMapper.map(item, GymDto.class)));
	}

	@GetMapping("/brands/{brandUuid}/gyms/{uuid}")
	@Operation(summary = "Retrieve a specific gym")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = GymDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "','" + Roles.Member + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> getGym(
			@PathVariable String uuid,
			@PathVariable String brandUuid) {
		com.iso.hypo.domain.dto.GymDto entity = null;
		try {
			entity = gymQueryService.find(brandUuid, uuid);
		} catch (GymException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == GymException.GYM_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(requestContext != null ? requestContext.getTrackingNumber() : null, e.getCode(), e.getMessage(), uuid));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(requestContext != null ? requestContext.getTrackingNumber() : null, e.getCode(), e.getMessage(), uuid));
		}

		return ResponseEntity.ok(modelMapper.map(entity, GymDto.class));
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
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Brand UUID in path and request body do not match");
		}
		
		com.iso.hypo.domain.dto.GymDto domainDto = modelMapper.map(request, com.iso.hypo.domain.dto.GymDto.class);
		
		try {
			domainDto = gymService.create(domainDto);
		} catch (GymException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == GymException.GYM_CODE_ALREADY_EXIST) {
				GymDto errorResponse = modelMapper.map(request, GymDto.class);
				List<MessageDto> messages = new ArrayList<MessageDto>();

				MessageDto message = new MessageDto();
				message.setCode(e.getCode());
				message.setDescription(e.getMessage());
				message.setSeverity(MessageSeverityEnum.Warning);
				messages.add(message);

				errorResponse.setMessages(messages);
				return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}

		return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(domainDto.getUuid()).toUri()).body(modelMapper.map(domainDto, GymDto.class));
	}

	@PutMapping("/brands/{brandUuid}/gyms/{uuid}")
	@Operation(summary = "Update a gym")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = GymDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
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
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Brand UUID in path and request body do not match");
		}
		
		com.iso.hypo.domain.dto.GymDto domainDto = modelMapper.map(request, com.iso.hypo.domain.dto.GymDto.class);

		try {
			domainDto = gymService.update(brandUuid, domainDto);
		} catch (GymException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == GymException.GYM_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(requestContext != null ? requestContext.getTrackingNumber() : null, e.getCode(), e.getMessage(), uuid));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(requestContext != null ? requestContext.getTrackingNumber() : null, e.getCode(), e.getMessage(), uuid));
		}

		return ResponseEntity.ok(modelMapper.map(domainDto, GymDto.class));
	}

	@PatchMapping("/brands/{brandUuid}/gyms/{uuid}")
	@Operation(summary = "Patch a gym")
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = GymDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> patchGym(
			@PathVariable String uuid,
			@PathVariable String brandUuid, 
			@RequestBody PatchGymDto request) {
		
		if (!request.getBrandUuid().equals(brandUuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Brand UUID in path and request body do not match");
		}
		
		com.iso.hypo.domain.dto.GymDto domainDto = modelMapper.map(request, com.iso.hypo.domain.dto.GymDto.class);

		try {
			domainDto = gymService.patch(brandUuid, domainDto);
		} catch (GymException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == GymException.GYM_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(requestContext != null ? requestContext.getTrackingNumber() : null, e.getCode(), e.getMessage(), uuid));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(requestContext != null ? requestContext.getTrackingNumber() : null, e.getCode(), e.getMessage(), uuid));
		}

		return ResponseEntity.ok(modelMapper.map(domainDto, GymDto.class));
	}

	@PostMapping("/brands/{brandUuid}/gyms/{uuid}/activate")
	@Operation(summary = "Activate a gym")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = GymDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> activateGym(
			@PathVariable String uuid,
			@PathVariable String brandUuid) {
		com.iso.hypo.domain.dto.GymDto entity;

		try {
			entity = gymService.activate(brandUuid, uuid);
		} catch (GymException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == GymException.GYM_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(requestContext != null ? requestContext.getTrackingNumber() : null, e.getCode(), e.getMessage(), uuid));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(requestContext != null ? requestContext.getTrackingNumber() : null, e.getCode(), e.getMessage(), uuid));
		}

		return ResponseEntity.ok(modelMapper.map(entity, GymDto.class));
	}

	@PostMapping("/brands/{brandUuid}/gyms/{uuid}/deactivate")
	@Operation(summary = "Deactivate a gym")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = GymDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> deactivateGym(
			@PathVariable String uuid,
			@PathVariable String brandUuid) {

		com.iso.hypo.domain.dto.GymDto entity;

		try {
			entity = gymService.deactivate(brandUuid, uuid);
		} catch (GymException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == GymException.GYM_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(requestContext != null ? requestContext.getTrackingNumber() : null, e.getCode(), e.getMessage(), uuid));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(requestContext != null ? requestContext.getTrackingNumber() : null, e.getCode(), e.getMessage(), uuid));
		}

		return ResponseEntity.ok(modelMapper.map(entity, GymDto.class));
	}

	@DeleteMapping("/brands/{brandUuid}/gyms/{uuid}")
	@Operation(summary = "Delete a gym")
	@ApiResponses({ @ApiResponse(responseCode = "202"),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
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

			if (e.getCode() == GymException.GYM_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(requestContext != null ? requestContext.getTrackingNumber() : null, e.getCode(), e.getMessage(), uuid));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(requestContext != null ? requestContext.getTrackingNumber() : null, e.getCode(), e.getMessage(), uuid));
		}

		return ResponseEntity.ok(uuid);
	}
}
