package com.isoceles.hypothenus.gym.admin.papi.controller;

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

import com.isoceles.hypothenus.gym.admin.papi.config.security.Roles;
import com.isoceles.hypothenus.gym.admin.papi.dto.ErrorDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.GymDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.GymSearchDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.MessageDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.MessageSeverityEnum;
import com.isoceles.hypothenus.gym.admin.papi.dto.patch.PatchGymDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.post.PostGymDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.put.PutGymDto;
import com.isoceles.hypothenus.gym.domain.exception.DomainException;
import com.isoceles.hypothenus.gym.domain.model.GymSearchResult;
import com.isoceles.hypothenus.gym.domain.model.aggregate.Gym;
import com.isoceles.hypothenus.gym.domain.services.GymService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/v1/admin")
@Validated
public class GymController {

	@Autowired
	private Logger logger;

	@Autowired
	private ModelMapper modelMapper;

	private GymService gymService;

	public GymController(GymService gymService) {
		this.gymService = gymService;
	}

	@GetMapping("/brands/{brandId}/gyms/search")
	@Operation(summary = "Search for gyms")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = Page.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasRole('" + Roles.Admin + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> searchGym(
			@Parameter(description = "search criteria") @RequestParam(name = "criteria", required = true) String criteria,
			@Parameter(description = "page number") @RequestParam(name = "page", required = true) int page,
			@Parameter(description = "page size") @RequestParam(name = "pageSize", required = true) int pageSize,
			@Parameter(description = "includeInactive") @RequestParam(name = "includeInactive", required = false, defaultValue="false") boolean includeInactive,
			@PathVariable("brandId") String brandId) {

		Page<GymSearchResult> entities = null;
		try {
			entities = gymService.search(page, pageSize, criteria, includeInactive);
		} catch (DomainException e) {
			logger.error(e.getMessage(), e);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), criteria));
		}
		
		return ResponseEntity.ok(entities.map(item -> modelMapper.map(item, GymSearchDto.class)));
	}

	@GetMapping("/brands/{brandId}/gyms")
	@PreAuthorize("hasRole('" + Roles.Admin + "')")
	@Operation(summary = "Retrieve a list of gyms")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = Page.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> listGym(
			@Parameter(description = "page number") @RequestParam(name = "page", required = true) int page,
			@Parameter(description = "page size") @RequestParam(name = "pageSize", required = true) int pageSize,
			@Parameter(description = "includeInactive") @RequestParam(name = "includeInactive", required = false, defaultValue="false") boolean includeInactive,
			@PathVariable("brandId") String brandId) {

		Page<Gym> entities = null;
		try {
			entities = gymService.list(brandId, page, pageSize, includeInactive);
		} catch (DomainException e) {
			logger.error(e.getMessage(), e);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), null));

		}

		return ResponseEntity.ok(entities.map(item -> modelMapper.map(item, GymDto.class)));
	}

	@GetMapping("/brands/{brandId}/gyms/{gymId}")
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
	public ResponseEntity<Object> getGym(@PathVariable("gymId") String gymId,
										 @PathVariable("brandId") String brandId) {
		Gym entity = null;
		try {
			entity = gymService.findByGymId(brandId, gymId);
		} catch (DomainException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == DomainException.GYM_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), gymId));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), gymId));
		}

		return ResponseEntity.ok(modelMapper.map(entity, GymDto.class));
	}

	@PostMapping("/brands/{brandId}/gyms")
	@Operation(summary = "Create a new gym")
	@ApiResponses({
			@ApiResponse(responseCode = "201", content = {
					@Content(schema = @Schema(implementation = GymDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasRole('" + Roles.Admin + "')")
	@ResponseStatus(value = HttpStatus.CREATED)
	public ResponseEntity<Object> createGym(@PathVariable("brandId") String brandId, 
											@RequestBody PostGymDto request) {
		Gym entity = modelMapper.map(request, Gym.class);

		try {
			gymService.create(entity);
		} catch (DomainException e) {
			logger.error(e.getMessage(), e);
			
			if (e.getCode() == DomainException.GYM_CODE_ALREADY_EXIST) {
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

		return ResponseEntity.created(
				ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(entity.getId()).toUri())
				.body(modelMapper.map(entity, GymDto.class));
	}

	@PutMapping("/brands/{brandId}/gyms/{gymId}")
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
	public ResponseEntity<Object> updateGym(@PathVariable("brandId") String brandId,
										    @PathVariable("gymId") String gymId, 
											@RequestBody PutGymDto request) {
		Gym entity = modelMapper.map(request, Gym.class);
		
		try {
			entity = gymService.update(entity);
		} catch (DomainException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == DomainException.GYM_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), gymId));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), gymId));
		}

		return ResponseEntity.ok(modelMapper.map(entity, GymDto.class));
	}

	@PatchMapping("/brands/{brandId}/gyms/{gymId}")
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
	public ResponseEntity<Object> patchGym(@PathVariable("gymId") String gymId, 
										   @PathVariable("brandId") String brandId,
										   @RequestBody PatchGymDto request) {
		Gym entity = modelMapper.map(request, Gym.class);
		
		try {
			entity = gymService.patch(entity);
		} catch (DomainException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == DomainException.GYM_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), gymId));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), gymId));
		}

		return ResponseEntity.ok(modelMapper.map(entity, GymDto.class));
	}

	
	@PostMapping("/brands/{brandId}/gyms/{gymId}/activate")
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
			@PathVariable("gymId") String gymId,
		    @PathVariable("brandId") String brandId) {
		Gym entity;
		
		try {
			entity = gymService.activate(brandId, gymId);
		} catch (DomainException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == DomainException.GYM_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), gymId));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), gymId));
		}

		return ResponseEntity.ok(modelMapper.map(entity, GymDto.class));
	}
	
	@PostMapping("/brands/{brandId}/gyms/{gymId}/deactivate")
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
			@PathVariable("gymId") String gymId,
			@PathVariable("brandId") String brandId) {
		
		Gym entity;
		
		try {
			entity = gymService.deactivate(brandId, gymId);
		} catch (DomainException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == DomainException.GYM_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), gymId));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), gymId));
		}

		return ResponseEntity.ok(modelMapper.map(entity, GymDto.class));
	}
	
	@DeleteMapping("/brands/{brandId}/gyms/{gymId}")
	@Operation(summary = "Delete a gym")
	@ApiResponses({ @ApiResponse(responseCode = "202"),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasRole('" + Roles.Admin + "')")
	@ResponseStatus(value = HttpStatus.ACCEPTED)
	public ResponseEntity<Object> deleteGym(@PathVariable("brandId") String brandId,
											@PathVariable("gymId") String gymId) {
		try {
			gymService.delete(brandId, gymId);
		} catch (DomainException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == DomainException.GYM_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), gymId));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), gymId));
		}

		return ResponseEntity.ok(gymId);
	}
}
