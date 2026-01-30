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
import com.iso.hypo.admin.papi.dto.model.MemberDto;
import com.iso.hypo.admin.papi.dto.patch.PatchMemberDto;
import com.iso.hypo.admin.papi.dto.post.PostMemberDto;
import com.iso.hypo.admin.papi.dto.put.PutMemberDto;
import com.iso.hypo.admin.papi.dto.search.MemberSearchDto;
import com.iso.hypo.common.context.RequestContext;
import com.iso.hypo.services.MemberQueryService;
import com.iso.hypo.services.MemberService;
import com.iso.hypo.services.exception.MemberException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/v1")
@Validated
public class MemberController {

	private static final Logger logger = LoggerFactory.getLogger(MemberController.class);

	private final ModelMapper modelMapper;
	private final RequestContext requestContext;

	private final MemberService memberService;
	private final MemberQueryService memberQueryService;

	public MemberController(ModelMapper modelMapper, MemberService memberService, MemberQueryService memberQueryService, RequestContext requestContext) {
		this.modelMapper = modelMapper;
		this.memberService = memberService;
		this.memberQueryService = memberQueryService;
		this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
	}

	@GetMapping("/brands/{brandUuid}/members/search")
	@Operation(summary = "Search for brands")
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
	public ResponseEntity<Object> searchBrand(
			@Parameter(description = "search criteria") @RequestParam String criteria,
			@Parameter(description = "page number") @RequestParam int page,
			@Parameter(description = "page size") @RequestParam int pageSize,
			@Parameter(description = "includeInactive") @RequestParam(required = false, defaultValue = "false") boolean includeInactive) {

		Page<com.iso.hypo.domain.dto.search.MemberSearchDto> domainDtos = null;
		try {
			domainDtos = memberQueryService.search(page, pageSize, criteria, includeInactive);
		} catch (MemberException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, criteria);
		}
		
		return ResponseEntity.ok(domainDtos.map(item -> modelMapper.map(item, MemberSearchDto.class)));
	}
	
	@GetMapping("/brands/{brandUuid}/members")
	@Operation(summary = "Retrieve a list of members")
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
	public ResponseEntity<Object> listMembers(
			@PathVariable String brandUuid,
			@Parameter(description = "page number") @RequestParam int page,
			@Parameter(description = "page size") @RequestParam int pageSize,
			@Parameter(description = "includeInactive") @RequestParam(required = false, defaultValue = "false") boolean includeInactive) {

		Page<com.iso.hypo.domain.dto.MemberDto> domainDtos = null;
		try {
			domainDtos = memberQueryService.list(brandUuid, page, pageSize, includeInactive);
		} catch (MemberException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, null);
		}

		return ResponseEntity.ok(domainDtos.map(item -> modelMapper.map(item, MemberDto.class)));
	}

	@GetMapping("/brands/{brandUuid}/members/{uuid}")
	@Operation(summary = "Retrieve a specific member")
	@ApiResponses({ @ApiResponse(responseCode = "200", content = {
			@Content(schema = @Schema(implementation = MemberDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> getMember(
			@PathVariable String brandUuid,
			@PathVariable String uuid) {
		com.iso.hypo.domain.dto.MemberDto domainDto = null;
		try {
			domainDto = memberQueryService.find(brandUuid, uuid);
		} catch (MemberException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, uuid);
		}

		return ResponseEntity.ok(modelMapper.map(domainDto, MemberDto.class));
	}

	@PostMapping("/brands/{brandUuid}/members/register")
	@Operation(summary = "Create a new member")
	@ApiResponses({ @ApiResponse(responseCode = "201", content = {
			@Content(schema = @Schema(implementation = MemberDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.CREATED)
	public ResponseEntity<Object> createMember(
			@PathVariable String brandUuid,
			@RequestBody PostMemberDto request) {
		
		if (!request.getBrandUuid().equals(brandUuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Brand UUID in path and request body do not match");
		}
		
		com.iso.hypo.domain.dto.MemberDto domainDto = modelMapper.map(request, com.iso.hypo.domain.dto.MemberDto.class);

		try {
			domainDto = memberService.create(domainDto, request.getPassword());
		} catch (MemberException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, null);
		}

		return ResponseEntity.created(
				ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(domainDto.getUuid()).toUri())
				.body(modelMapper.map(domainDto, MemberDto.class));
	}

	@PutMapping("/brands/{brandUuid}/members/{uuid}")
	@Operation(summary = "Update a member")
	@ApiResponses({ @ApiResponse(responseCode = "200", content = {
			@Content(schema = @Schema(implementation = MemberDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> updateMember(
			@PathVariable String brandUuid,
			@PathVariable String uuid,
			@Parameter(description = "activate or deactivate Member") 
			@RequestParam(required = false, defaultValue = "true") boolean isActive,
			@RequestBody PutMemberDto request) {
		
		if (!request.getBrandUuid().equals(brandUuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		
		if (!request.getUuid().equals(uuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		
		com.iso.hypo.domain.dto.MemberDto domainDto = modelMapper.map(request, com.iso.hypo.domain.dto.MemberDto.class);

		try {
			domainDto = memberService.update(domainDto);
		} catch (MemberException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, uuid);
		}

		return ResponseEntity.ok(modelMapper.map(domainDto, MemberDto.class));
	}

	@PostMapping("/brands/{brandUuid}/members/{uuid}/activate")
	@Operation(summary = "Activate a member")
	@ApiResponses({ @ApiResponse(responseCode = "200", content = {
			@Content(schema = @Schema(implementation = MemberDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> activateMember(
			@PathVariable String brandUuid,
			@PathVariable String uuid) {
		
		com.iso.hypo.domain.dto.MemberDto domainDto;

		try {
			domainDto = memberService.activate(brandUuid, uuid);
		} catch (MemberException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, uuid);
		}

		return ResponseEntity.ok(modelMapper.map(domainDto, MemberDto.class));
	}

	@PostMapping("/brands/{brandUuid}/members/{uuid}/deactivate")
	@Operation(summary = "Deactivate a member")
	@ApiResponses({ @ApiResponse(responseCode = "200", content = {
			@Content(schema = @Schema(implementation = MemberDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> deactivateMember(
			@PathVariable String brandUuid,
			@PathVariable String uuid) {
		
		com.iso.hypo.domain.dto.MemberDto domainDto;

		try {
			domainDto = memberService.deactivate(brandUuid, uuid);
		} catch (MemberException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, uuid);
		}

		return ResponseEntity.ok(modelMapper.map(domainDto, MemberDto.class));
	}

	@PatchMapping("/brands/{brandUuid}/members/{uuid}")
	@Operation(summary = "Patch a member")
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ApiResponses({ @ApiResponse(responseCode = "200", content = {
			@Content(schema = @Schema(implementation = MemberDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> patchMember(
			@PathVariable String brandUuid,
			@PathVariable String uuid, 
			@RequestBody PatchMemberDto request) {
		
		if (!request.getBrandUuid().equals(brandUuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		
		if (!request.getUuid().equals(uuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		
		com.iso.hypo.domain.dto.MemberDto domainDto = modelMapper.map(request, com.iso.hypo.domain.dto.MemberDto.class);

		try {
			domainDto = memberService.patch(domainDto);
		} catch (MemberException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, uuid);
		}

		return ResponseEntity.ok(modelMapper.map(domainDto, MemberDto.class));
	}

	@DeleteMapping("/brands/{brandUuid}/members/{uuid}")
	@Operation(summary = "Delete a member")
	@ApiResponses({ @ApiResponse(responseCode = "202"),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.ACCEPTED)
	public ResponseEntity<Object> deleteMember(
			@PathVariable String brandUuid,
			@PathVariable String uuid) {
		try {
			memberService.delete(brandUuid, uuid);
		} catch (MemberException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, uuid);
		}

		return ResponseEntity.accepted().build();
	}
}
