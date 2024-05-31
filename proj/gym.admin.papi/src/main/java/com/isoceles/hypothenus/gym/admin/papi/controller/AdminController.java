package com.isoceles.hypothenus.gym.admin.papi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import com.isoceles.hypothenus.gym.admin.papi.dto.Gym;
import com.isoceles.hypothenus.gym.admin.papi.dto.Response;
import com.isoceles.hypothenus.gym.admin.papi.dto.post.PostGym;
import com.isoceles.hypothenus.gym.admin.papi.dto.put.PutGym;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/admin")
@Validated
public class AdminController {

	public AdminController() {
		// TODO Auto-generated constructor stub
	}
	
	@GetMapping("/gyms/search")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "Search for gym")
	public ResponseEntity<Response<List<Gym>>> searchGym (@Parameter(description = "page number") @RequestParam (name="page", required = true) int page,
														  @Parameter(description = "page size") @RequestParam (name="pageSize", required = true) int pageSize,
														  @Parameter(description = "search criteria") @RequestParam (name="criteria", required = true) String criteria) {
		return null;
		
	} 
	
	@GetMapping("/gyms/list")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "Retrieve a list of gym")
	public ResponseEntity<Response<List<Gym>>> listGym (@Parameter(description = "page number") @RequestParam (name="page", required = true) int page,
														@Parameter(description = "page size") @RequestParam (name="pageSize", required = true) int pageSize) {
		return null;
		
	} 
	
	@GetMapping("/gyms/{gymId}")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "Retrieve a specific gym")
	public ResponseEntity<Response<Gym>> getGym (@PathVariable("gymId") String gymId, @RequestBody PutGym request) {
		return null;
		
	}
	
	@PostMapping("/gyms")
	@ResponseStatus(value = HttpStatus.CREATED)
	@Operation(summary = "Create a new gym")
	public ResponseEntity<Response<Gym>> createGym (@RequestBody PostGym request) {
		return null;
		
	}
	
	@PutMapping("/gyms/{gymId}")
	@ResponseStatus(value = HttpStatus.OK)
	//@Operation(summary = "Create a new gym")
	public ResponseEntity<Response<Gym>> updateGym (@PathVariable("gymId") String gymId, @RequestBody PutGym request) {
		return null;
		
	}
	
	@PatchMapping("/gyms/{gymId}")
	//@Operation(summary = "Create a new gym")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Response<Gym>> patchGym (@PathVariable("gymId") String gymId, @RequestBody PutGym request) {
		return null;
		
	}
	
	@DeleteMapping("/gyms/{gymId}")
	//@Operation(summary = "Create a new gym")
	@ResponseStatus(value = HttpStatus.ACCEPTED)
	public void deleteGym (@PathVariable("gymId") String gymId) {
		
		
	}

}
