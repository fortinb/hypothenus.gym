package com.isoceles.hypothenus.gym.admin.papi;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.isoceles.hypothenus.gym.admin.papi.dto.GymSearchDto;
import com.isoceles.hypothenus.tests.http.HttpUtils;
import com.isoceles.hypothenus.tests.security.Roles;
import com.isoceles.hypothenus.tests.security.SecurityUtils;
import com.isoceles.hypothenus.tests.security.Users;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GymControllerTests {

	public static final String searchURI = "/v1/admin/gyms/search";
	public static final String searchCriteria = "criteria";
	
	@LocalServerPort
	private int port;

	private TestRestTemplate restTemplate = new TestRestTemplate();
	
	
	@Test
	void testSearchSuccess() throws MalformedURLException, JsonProcessingException {
		HttpHeaders httpHeaders = SecurityUtils.getHeaders(Roles.Admin, Users.Admin);
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add(searchCriteria, "bou");
		
		HttpEntity<String> entity = new HttpEntity<>(null, httpHeaders);
		
		ResponseEntity<List<GymSearchDto>> response = restTemplate.exchange(
				HttpUtils.createURL(URI.create(searchURI), port, params),
                HttpMethod.GET, entity, new ParameterizedTypeReference<List<GymSearchDto>>(){});
		
		Assert.isTrue(response.getStatusCode() == HttpStatus.OK, String.format("Search error: %s", response.getStatusCode()));
	}

}
