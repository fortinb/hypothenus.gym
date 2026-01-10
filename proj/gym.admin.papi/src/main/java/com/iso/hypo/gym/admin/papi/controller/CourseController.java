package com.iso.hypo.gym.admin.papi.controller;

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

import com.iso.hypo.gym.admin.papi.config.security.Roles;
import com.iso.hypo.gym.admin.papi.dto.ErrorDto;
import com.iso.hypo.gym.admin.papi.dto.MessageDto;
import com.iso.hypo.gym.admin.papi.dto.enumeration.MessageSeverityEnum;
import com.iso.hypo.gym.admin.papi.dto.model.CourseDto;
import com.iso.hypo.gym.admin.papi.dto.patch.PatchCourseDto;
import com.iso.hypo.gym.admin.papi.dto.post.PostCourseDto;
import com.iso.hypo.gym.admin.papi.dto.put.PutCourseDto;
import com.iso.hypo.gym.domain.exception.DomainException;
import com.iso.hypo.gym.domain.model.aggregate.Course;
import com.iso.hypo.gym.domain.services.CourseService;

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

	@Autowired
	private Logger logger;

	@Autowired
	private ModelMapper modelMapper;

	private CourseService courseService;

	public CourseController(CourseService courseService) {
		this.courseService = courseService;
	}

	@GetMapping("/brands/{brandId}/gyms/{gymId}/courses")
	@Operation(summary = "Retrieve a list of courses")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = Page.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> listCourse(
			@PathVariable("brandId") String brandId,
			@PathVariable("gymId") String gymId,
			@Parameter(description = "page number") @RequestParam(name = "page", required = true) int page,
			@Parameter(description = "page size") @RequestParam(name = "pageSize", required = true) int pageSize,
			@Parameter(description = "includeInactive") @RequestParam(name = "includeInactive", required = false, defaultValue = "false") boolean includeInactive) {

		Page<Course> entities = null;
		try {
			entities = courseService.list(brandId, gymId, page, pageSize, includeInactive);
		} catch (DomainException e) {
			logger.error(e.getMessage(), e);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), null));
		}

		return ResponseEntity.ok(entities.map(item -> modelMapper.map(item, CourseDto.class)));
	}

	@GetMapping("/brands/{brandId}/gyms/{gymId}/courses/{courseId}")
	@Operation(summary = "Retrieve a specific course")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = CourseDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> getCourse(
			@PathVariable("brandId") String brandId,
			@PathVariable("gymId") String gymId,
			@PathVariable("courseId") String courseId) {
		Course entity = null;
		try {
			entity = courseService.findByCourseId(brandId, gymId, courseId);
		} catch (DomainException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == DomainException.COURSE_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), courseId));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), courseId));
		}

		return ResponseEntity.ok(modelMapper.map(entity, CourseDto.class));
	}

	@PostMapping("/brands/{brandId}/gyms/{gymId}/courses")
	@Operation(summary = "Create a new course")
	@ApiResponses({
			@ApiResponse(responseCode = "201", content = {
					@Content(schema = @Schema(implementation = CourseDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.CREATED)
	public ResponseEntity<Object> createCourse(
			@PathVariable("brandId") String brandId,
			@PathVariable("gymId") String gymId,
			@RequestBody PostCourseDto request) {
		Course entity = modelMapper.map(request, Course.class);

		try {
			courseService.create(brandId, gymId, entity);
		} catch (DomainException e) {
			logger.error(e.getMessage(), e);
			
			if (e.getCode() == DomainException.COURSE_CODE_ALREADY_EXIST) {
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

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}

		return ResponseEntity.created(
				ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(entity.getId()).toUri())
				.body(modelMapper.map(entity, CourseDto.class));
	}

	@PutMapping("/brands/{brandId}/gyms/{gymId}/courses/{courseId}")
	@Operation(summary = "Update a course")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = CourseDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> updateCourse(
			@PathVariable("brandId") String brandId,
			@PathVariable("gymId") String gymId,
			@PathVariable("courseId") String courseId,
			@Parameter(description = "activate or deactivate course") @RequestParam(name = "isActive", required = false, defaultValue = "true") boolean isActive,
			@RequestBody PutCourseDto request) {
		Course entity = modelMapper.map(request, Course.class);

		try {
			entity = courseService.update(brandId, gymId, entity);
		} catch (DomainException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == DomainException.COURSE_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), courseId));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), courseId));
		}

		return ResponseEntity.ok(modelMapper.map(entity, CourseDto.class));
	}

	@PostMapping("/brands/{brandId}/gyms/{gymId}/courses/{courseId}/activate")
	@Operation(summary = "Activate a course")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = CourseDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> activateCourse(
			@PathVariable("brandId") String brandId,
			@PathVariable("gymId") String gymId,
			@PathVariable("courseId") String courseId) {
		Course entity;

		try {
			entity = courseService.activate(brandId, gymId, courseId);
		} catch (DomainException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == DomainException.COURSE_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), courseId));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), courseId));
		}

		return ResponseEntity.ok(modelMapper.map(entity, CourseDto.class));
	}

	@PostMapping("/brands/{brandId}/gyms/{gymId}/courses/{courseId}/deactivate")
	@Operation(summary = "Deactivate a course")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = CourseDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> deactivateCourse(
			@PathVariable("brandId") String brandId,
			@PathVariable("gymId") String gymId,
			@PathVariable("courseId") String courseId) {
		Course entity;

		try {
			entity = courseService.deactivate(brandId, gymId, courseId);
		} catch (DomainException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == DomainException.COURSE_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), courseId));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), courseId));
		}

		return ResponseEntity.ok(modelMapper.map(entity, CourseDto.class));
	}

	@PatchMapping("/brands/{brandId}/gyms/{gymId}/courses/{courseId}")
	@Operation(summary = "Patch a course")
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = CourseDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> patchCourse(
			@PathVariable("brandId") String brandId,
			@PathVariable("gymId") String gymId,
			@PathVariable("courseId") String courseId, @RequestBody PatchCourseDto request) {
		Course entity = modelMapper.map(request, Course.class);

		try {
			entity = courseService.patch(brandId, gymId, entity);
		} catch (DomainException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == DomainException.COURSE_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), courseId));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), courseId));
		}

		return ResponseEntity.ok(modelMapper.map(entity, CourseDto.class));
	}

	@DeleteMapping("/brands/{brandId}/gyms/{gymId}/courses/{courseId}")
	@Operation(summary = "Delete a course")
	@ApiResponses({ @ApiResponse(responseCode = "202"),
			@ApiResponse(responseCode = "404", description = "Not found.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Unexpected error.", content = {
					@Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json") }) })
	@PreAuthorize("hasAnyRole('" + Roles.Admin + "','" + Roles.Manager + "')")
	@ResponseStatus(value = HttpStatus.ACCEPTED)
	public ResponseEntity<Object> deleteCourse(
			@PathVariable("brandId") String brandId,
			@PathVariable("gymId") String gymId,
			@PathVariable("courseId") String courseId) {
		try {
			courseService.delete(brandId, gymId, courseId);
		} catch (DomainException e) {
			logger.error(e.getMessage(), e);

			if (e.getCode() == DomainException.COURSE_NOT_FOUND) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorDto(e.getCode(), e.getMessage(), courseId));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorDto(e.getCode(), e.getMessage(), courseId));
		}

		return ResponseEntity.ok(courseId);
	}
}
