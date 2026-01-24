package com.iso.hypo.common.context;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestContext {

	private String username;
	
	private String trackingNumber;
	
	private String brandUuid;
	
	private String gymUuid;

	public RequestContext() {
		// default constructor for non-web contexts (e.g., unit tests)
	}

	public RequestContext(final HttpServletRequest request) {
		super();
		
		username = request.getHeader("x-credentials");
		trackingNumber = request.getHeader("x-tracking-number");
		brandUuid = request.getParameter("brandUuid");
		gymUuid = request.getParameter("gymUuid");
	}
	
	
}