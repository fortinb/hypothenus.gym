package com.isoceles.hypothenus.gym.domain.context;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestContext {

	private String username;
	
	private String trackingNumber;
	
	private String gymId;

	public RequestContext(final HttpServletRequest request) {
		super();
		
		username = request.getHeader("x-credentials");
		trackingNumber = request.getHeader("x-tracking-number");
		gymId = request.getParameter("gymId");
	}
	
	
}
