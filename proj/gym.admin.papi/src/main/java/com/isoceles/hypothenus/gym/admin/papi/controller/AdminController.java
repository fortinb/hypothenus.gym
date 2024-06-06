package com.isoceles.hypothenus.gym.admin.papi.controller;

import java.util.List;

import org.modelmapper.ModelMapper;
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

import com.isoceles.hypothenus.gym.admin.papi.dto.GymDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.patch.PatchGymDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.post.PostGymDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.put.PutGymDto;
import com.isoceles.hypothenus.gym.domain.model.Gym;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/v1/admin")
@Validated
public class AdminController {

	public AdminController() {
		// TODO Auto-generated constructor stub
	}
	
	@GetMapping("/gyms/search")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "Search for gym")
	public ResponseEntity<List<GymDto>> searchGym (@Parameter(description = "page number") @RequestParam (name="page", required = true) int page,
												   @Parameter(description = "page size") @RequestParam (name="pageSize", required = true) int pageSize,
												   @Parameter(description = "search criteria") @RequestParam (name="criteria", required = true) String criteria) {
		return null;
		
	} 
	
	@GetMapping("/gyms/list")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "Retrieve a list of gym")
	public ResponseEntity<List<GymDto>> listGym (@Parameter(description = "page number") @RequestParam (name="page", required = true) int page,
												 @Parameter(description = "page size") @RequestParam (name="pageSize", required = true) int pageSize) {
		return null;
		
	} 
	
	@GetMapping("/gyms/{gymId}")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "Retrieve a specific gym")
	public ResponseEntity<GymDto> getGym (@PathVariable("gymId") String gymId) {
		return null;
		
	}
	
	@PostMapping("/gyms")
	@ResponseStatus(value = HttpStatus.CREATED)
	@Operation(summary = "Create a new gym")
	@PreAuthorize("hasRole('Admin')")
	public ResponseEntity<GymDto> createGym (@RequestBody PostGymDto request) {
		ModelMapper modelMapper = new ModelMapper();
		Gym gym = modelMapper.map(request, Gym.class);

		GymDto gymDto = modelMapper.map(gym, GymDto.class);
		return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(gym.getId()).toUri()).body(gymDto);
	}
	
	@PutMapping("/gyms/{gymId}")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "Update a gym")
	public ResponseEntity<GymDto> updateGym (@PathVariable("gymId") String gymId, @RequestBody PutGymDto request) {
		return null;
		
	}
	
	@PatchMapping("/gyms/{gymId}")
	@Operation(summary = "Patch a gym")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<GymDto> patchGym (@PathVariable("gymId") String gymId, @RequestBody PatchGymDto request) {
		return null;
		
	}
	
	@DeleteMapping("/gyms/{gymId}")
	@Operation(summary = "Delete a gym")
	@ResponseStatus(value = HttpStatus.ACCEPTED)
	public void deleteGym (@PathVariable("gymId") String gymId) {
		
		
	}

}
