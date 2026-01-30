package com.iso.hypo.admin.papi.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.iso.hypo.common.context.RequestContext;
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
import com.iso.hypo.admin.papi.dto.model.CourseDto;
import com.iso.hypo.admin.papi.dto.patch.PatchCourseDto;
import com.iso.hypo.admin.papi.dto.post.PostCourseDto;
import com.iso.hypo.admin.papi.dto.put.PutCourseDto;
import com.iso.hypo.services.exception.CourseException;
import com.iso.hypo.services.CourseQueryService;
import com.iso.hypo.services.CourseService;
import com.iso.hypo.admin.papi.controller.util.ControllerErrorHandler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/v1")
@Validated
public class CourseController {

	private static final Logger logger = LoggerFactory.getLogger(CourseController.class);

	private final ModelMapper modelMapper;
	private final RequestContext requestContext;

	private final CourseService courseService;
	private final CourseQueryService courseQueryService;

	public CourseController(ModelMapper modelMapper, CourseService courseService, CourseQueryService courseQueryService, RequestContext requestContext) {
		this.modelMapper = modelMapper;
		this.courseService = courseService;
		this.courseQueryService = courseQueryService;
		this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
	}

	@GetMapping("/brands/{brandUuid}/gyms/{gymUuid}/courses")
	@Operation(summary = "Retrieve a list of courses")
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
	public ResponseEntity<Object> listCourse(
			@PathVariable String brandUuid,
			@PathVariable String gymUuid,
			@Parameter(description = "page number") @RequestParam int page,
			@Parameter(description = "page size") @RequestParam int pageSize,
			@Parameter(description = "includeInactive") @RequestParam(required = false, defaultValue = "false") boolean includeInactive) {

		Page<com.iso.hypo.domain.dto.CourseDto> domainDtos = null;
		try {
			domainDtos = courseQueryService.list(brandUuid, gymUuid, page, pageSize, includeInactive);
		} catch (CourseException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, null);
		}

		return ResponseEntity.ok(domainDtos.map(item -> modelMapper.map(item, CourseDto.class)));
	}

	@GetMapping("/brands/{brandUuid}/gyms/{gymUuid}/courses/{uuid}")
	@Operation(summary = "Retrieve a specific course")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = CourseDto.class), mediaType = "application/json") }),
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
	public ResponseEntity<Object> getCourse(
			@PathVariable String brandUuid,
			@PathVariable String gymUuid,
			@PathVariable String uuid) {
		com.iso.hypo.domain.dto.CourseDto domainDto = null;
		try {
			domainDto = courseQueryService.find(brandUuid, gymUuid, uuid);
		} catch (CourseException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, uuid);
		}

		return ResponseEntity.ok(modelMapper.map(domainDto, CourseDto.class));
	}

	@PostMapping("/brands/{brandUuid}/gyms/{gymUuid}/courses")
	@Operation(summary = "Create a new course")
	@ApiResponses({
			@ApiResponse(responseCode = "201", content = {
					@Content(schema = @Schema(implementation = CourseDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "400", description = "Bad request. The request is invalid or missing required data.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "403", description = "Forbidden. The client does not have permission to access this resource.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found. The requested resource does not exist.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected server error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.CREATED)
	public ResponseEntity<Object> createCourse(
			@PathVariable String brandUuid,
			@PathVariable String gymUuid,
			@RequestBody PostCourseDto request) {
		
		if (!request.getBrandUuid().equals(brandUuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		
		if (!request.getGymUuid().equals(gymUuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		
		com.iso.hypo.domain.dto.CourseDto domainDto = modelMapper.map(request, com.iso.hypo.domain.dto.CourseDto.class);

		try {
			domainDto = courseService.create(domainDto);
		} catch (CourseException e) {
			logger.error(e.getMessage(), e);
			
			if (e.getCode() == CourseException.COURSE_CODE_ALREADY_EXIST) {
				CourseDto errorResponse = modelMapper.map(request, CourseDto.class);
				List<MessageDto> messages = new ArrayList<MessageDto>();
				
				MessageDto message = new MessageDto();
				message.setCode(e.getCode());
				message.setDescription(e.getMessage());
				message.setSeverity(MessageSeverityEnum.Warning);
				messages.add(message);
				
				errorResponse.setMessages(messages);
				return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
			}

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, null);
		}

		return ResponseEntity.created(
				ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(domainDto.getUuid()).toUri())
				.body(modelMapper.map(domainDto, CourseDto.class));
	}

	@PutMapping("/brands/{brandUuid}/gyms/{gymUuid}/courses/{uuid}")
	@Operation(summary = "Update a course")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = CourseDto.class), mediaType = "application/json") }),
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
	public ResponseEntity<Object> updateCourse(
			@PathVariable String brandUuid,
			@PathVariable String gymUuid,
			@PathVariable String uuid,
			@Parameter(description = "activate or deactivate course") @RequestParam(required = false, defaultValue = "true") boolean isActive,
			@RequestBody PutCourseDto request) {
		
		if (!request.getBrandUuid().equals(brandUuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		
		if (!request.getGymUuid().equals(gymUuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		
		if (!request.getUuid().equals(uuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		
		com.iso.hypo.domain.dto.CourseDto domainDto = modelMapper.map(request, com.iso.hypo.domain.dto.CourseDto.class);

		try {
			domainDto = courseService.update(domainDto);
		} catch (CourseException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, uuid);
		}

		return ResponseEntity.ok(modelMapper.map(domainDto, CourseDto.class));
	}

	@PostMapping("/brands/{brandUuid}/gyms/{gymUuid}/courses/{uuid}/activate")
	@Operation(summary = "Activate a course")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = CourseDto.class), mediaType = "application/json") }),
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
	public ResponseEntity<Object> activateCourse(
			@PathVariable String brandUuid,
			@PathVariable String gymUuid,
			@PathVariable String uuid) {
		com.iso.hypo.domain.dto.CourseDto domainDto;

		try {
			domainDto = courseService.activate(brandUuid, gymUuid, uuid);
		} catch (CourseException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, uuid);
		}

		return ResponseEntity.ok(modelMapper.map(domainDto, CourseDto.class));
	}

	@PostMapping("/brands/{brandUuid}/gyms/{gymUuid}/courses/{uuid}/deactivate")
	@Operation(summary = "Deactivate a course")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = CourseDto.class), mediaType = "application/json") }),
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
	public ResponseEntity<Object> deactivateCourse(
			@PathVariable String brandUuid,
			@PathVariable String gymUuid,
			@PathVariable String uuid) {
		com.iso.hypo.domain.dto.CourseDto domainDto;

		try {
			domainDto = courseService.deactivate(brandUuid, gymUuid, uuid);
		} catch (CourseException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, uuid);
		}

		return ResponseEntity.ok(modelMapper.map(domainDto, CourseDto.class));
	}

	@PatchMapping("/brands/{brandUuid}/gyms/{gymUuid}/courses/{uuid}")
	@Operation(summary = "Patch a course")
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = CourseDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "400", description = "Bad request. The request is invalid or missing required data.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "403", description = "Forbidden. The client does not have permission to access this resource.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found. The requested resource does not exist.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected server error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> patchCourse(
			@PathVariable String brandUuid,
			@PathVariable String gymUuid,
			@PathVariable String uuid, 
			@RequestBody PatchCourseDto request) {
		
		if (!request.getBrandUuid().equals(brandUuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		
		if (!request.getGymUuid().equals(gymUuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		
		if (!request.getUuid().equals(uuid)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		
		com.iso.hypo.domain.dto.CourseDto domainDto = modelMapper.map(request, com.iso.hypo.domain.dto.CourseDto.class);

		try {
			domainDto = courseService.patch(domainDto);
		} catch (CourseException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, uuid);
		}

		return ResponseEntity.ok(modelMapper.map(domainDto, CourseDto.class));
	}

	@DeleteMapping("/brands/{brandUuid}/gyms/{gymUuid}/courses/{uuid}")
	@Operation(summary = "Delete a course")
	@ApiResponses({ @ApiResponse(responseCode = "202"),
			@ApiResponse(responseCode = "400", description = "Bad request. The request is invalid or missing required data.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "403", description = "Forbidden. The client does not have permission to access this resource.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found. The requested resource does not exist.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected server error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.ACCEPTED)
	public ResponseEntity<Object> deleteCourse(
			@PathVariable String brandUuid,
			@PathVariable String gymUuid,
			@PathVariable String uuid) {
		try {
			courseService.delete(brandUuid, gymUuid, uuid);
		} catch (CourseException e) {
			logger.error(e.getMessage(), e);

			return ControllerErrorHandler.buildErrorResponse(e, requestContext, uuid);
		}

		return ResponseEntity.accepted().build();
	}
}