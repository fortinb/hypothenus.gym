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

import com.iso.hypo.model.exception.BrandException;
import com.iso.hypo.model.services.BrandService;
import com.iso.hypo.admin.papi.config.security.Roles;
import com.iso.hypo.admin.papi.dto.ErrorDto;
import com.iso.hypo.admin.papi.dto.MessageDto;
import com.iso.hypo.admin.papi.dto.enumeration.MessageSeverityEnum;
import com.iso.hypo.admin.papi.dto.model.BrandDto;
import com.iso.hypo.admin.papi.dto.patch.PatchBrandDto;
import com.iso.hypo.admin.papi.dto.post.PostBrandDto;
import com.iso.hypo.admin.papi.dto.put.PutBrandDto;
import com.iso.hypo.admin.papi.dto.search.BrandSearchDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/v1")
@Validated
public class BrandController {

	@Autowired
	private Logger logger;

	@Autowired
	private ModelMapper modelMapper;

	private BrandService brandService;

	public BrandController(BrandService brandService) {
		this.brandService = brandService;
	}

	@GetMapping("/brands/search")
	@Operation(summary = "Search for brands")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = Page.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasRole('" + Roles.Admin + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> searchBrand(
			@Parameter(description = "search criteria") @RequestParam String criteria,
			@Parameter(description = "page number") @RequestParam int page,
			@Parameter(description = "page size") @RequestParam int pageSize,
			@Parameter(description = "includeInactive") @RequestParam(required = false, defaultValue = "false") boolean includeInactive) {

		Page<com.iso.hypo.model.dto.BrandSearchDto> entities = null;
		try {
			entities = brandService.search(page, pageSize, criteria, includeInactive);
		} catch (BrandException e) {
			logger.error(e.getMessage(), e);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), criteria));
		}
		
		return ResponseEntity.ok(entities.map(item -> modelMapper.map(item, BrandSearchDto.class)));
	}

	@GetMapping("/brands")
	@PreAuthorize("hasRole('" + Roles.Admin + "')")
	@Operation(summary = "Retrieve a list of brands")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = Page.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> listBrand(
			@Parameter(description = "page number") @RequestParam int page,
			@Parameter(description = "page size") @RequestParam int pageSize,
			@Parameter(description = "includeInactive") @RequestParam(required = false, defaultValue = "false") boolean includeInactive) {

		Page<com.iso.hypo.model.dto.BrandDto> entities = null;
		try {
			entities = brandService.list(page, pageSize, includeInactive);
		} catch (BrandException e) {
			logger.error(e.getMessage(), e);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), null));

		}

		return ResponseEntity.ok(entities.map(item -> modelMapper.map(item, BrandDto.class)));
	}

	@GetMapping("/brands/{uuid}")
	@Operation(summary = "Retrieve a specific brand")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = BrandDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "','" + Roles.Member + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> getBrand(@PathVariable String uuid) {
		com.iso.hypo.model.dto.BrandDto entity = null;
		try {
			entity = brandService.findByBrandUuid(uuid);
		} catch (BrandException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == BrandException.BRAND_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), uuid));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), uuid));
		}

		return ResponseEntity.ok(modelMapper.map(entity, BrandDto.class));
	}

	@PostMapping("/brands")
	@Operation(summary = "Create a new brand")
	@ApiResponses({
			@ApiResponse(responseCode = "201", content = {
					@Content(schema = @Schema(implementation = BrandDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasRole('" + Roles.Admin + "')")
	@ResponseStatus(value = HttpStatus.CREATED)
	public ResponseEntity<Object> createBrand(@RequestBody PostBrandDto request) {
		// map controller POST DTO to domain DTO
		com.iso.hypo.model.dto.BrandDto domainDto = modelMapper.map(request, com.iso.hypo.model.dto.BrandDto.class);

		try {
			domainDto = brandService.create(domainDto);
		} catch (BrandException e) {
			logger.error(e.getMessage(), e);
			
			if (e.getCode() == BrandException.BRAND_CODE_ALREADY_EXIST) {
				BrandDto errorResponse = modelMapper.map(request, BrandDto.class);
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

		// map returned domain DTO to controller DTO for response
		return ResponseEntity.created(
				ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(domainDto.getUuid()).toUri())
				.body(modelMapper.map(domainDto, BrandDto.class));
	}

	@PutMapping("/brands/{uuid}")
	@Operation(summary = "Update a brand")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = BrandDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> updateBrand(@PathVariable String uuid, @RequestBody PutBrandDto request) {
		com.iso.hypo.model.dto.BrandDto domainDto = modelMapper.map(request, com.iso.hypo.model.dto.BrandDto.class);
		
		try {
			domainDto = brandService.update(domainDto);
		} catch (BrandException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == BrandException.BRAND_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), uuid));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), uuid));
		}

		return ResponseEntity.ok(modelMapper.map(domainDto, BrandDto.class));
	}

	@PatchMapping("/brands/{uuid}")
	@Operation(summary = "Patch a brand")
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = BrandDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> patchBrand(@PathVariable String uuid, @RequestBody PatchBrandDto request) {
		com.iso.hypo.model.dto.BrandDto domainDto = modelMapper.map(request, com.iso.hypo.model.dto.BrandDto.class);
		
		try {
			domainDto = brandService.patch(domainDto);
		} catch (BrandException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == BrandException.BRAND_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), uuid));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), uuid));
		}

		return ResponseEntity.ok(modelMapper.map(domainDto, BrandDto.class));
	}

	
	@PostMapping("/brands/{uuid}/activate")
	@Operation(summary = "Activate a brand")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = BrandDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> activateBrand(
			@PathVariable String uuid) {
		com.iso.hypo.model.dto.BrandDto entity;
		
		try {
			entity = brandService.activate(uuid);
		} catch (BrandException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == BrandException.BRAND_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), uuid));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), uuid));
		}

		return ResponseEntity.ok(modelMapper.map(entity, BrandDto.class));
	}
	
	@PostMapping("/brands/{uuid}/deactivate")
	@Operation(summary = "Deactivate a brand")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = BrandDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> deactivateBrand(
			@PathVariable String uuid) {
		
		com.iso.hypo.model.dto.BrandDto entity;
		
		try {
			entity = brandService.deactivate(uuid);
		} catch (BrandException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == BrandException.BRAND_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), uuid));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), uuid));
		}

		return ResponseEntity.ok(modelMapper.map(entity, BrandDto.class));
	}
	
	@DeleteMapping("/brands/{uuid}")
	@Operation(summary = "Delete a brand")
	@ApiResponses({ @ApiResponse(responseCode = "202"),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasRole('" + Roles.Admin + "')")
	@ResponseStatus(value = HttpStatus.ACCEPTED)
	public ResponseEntity<Object> deleteBrand(@PathVariable String uuid) {
		try {
			brandService.delete(uuid);
		} catch (BrandException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == BrandException.BRAND_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), uuid));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), uuid));
		}

		return ResponseEntity.ok(uuid);
	}
}
