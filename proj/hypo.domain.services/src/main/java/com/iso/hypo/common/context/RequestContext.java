package com.iso.hypo.common.context;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iso.hypo.domain.security.RoleEnum;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestContext {

	private String username;
	
	private String trackingNumber;
	
	private String brandUuid;
	
	private List<RoleEnum> roles;

	public RequestContext() {
		// default constructor for non-web contexts (e.g., unit tests)
	}

	public RequestContext(final HttpServletRequest request) {
		super();
		
		username = request.getHeader("x-credentials");
		trackingNumber = request.getHeader("x-tracking-number");
		brandUuid = request.getParameter("brandUuid");
		
		String roles = request.getHeader("x-authorization");

		ObjectMapper mapper = new ObjectMapper();
		try {
			AuthorizationDto authorizationDto = mapper.readValue(roles, new TypeReference<AuthorizationDto>(){});
			this.setRoles(authorizationDto.getRoles());
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}
}