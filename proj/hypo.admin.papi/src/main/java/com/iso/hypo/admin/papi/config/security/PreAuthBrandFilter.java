package com.iso.hypo.admin.papi.config.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class PreAuthBrandFilter extends OncePerRequestFilter {

	private final JwtDecoder jwtDecoder;
	// Exclude 'code' path segments from being treated as brand UUIDs
	private static final Pattern BRAND_UUID_PATTERN = Pattern.compile("/brands/(?!code)([^/]+)(?:/|$)");

	public PreAuthBrandFilter(JwtDecoder jwtDecoder) {
		this.jwtDecoder = jwtDecoder;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		// BrandUuid validation: extract from path variable /brands/{brandUuid}
		String requestUri = request.getRequestURI();
		Matcher m = BRAND_UUID_PATTERN.matcher(requestUri);
		final String brandUuid = m.find() ? m.group(1) : null;
		
		if (brandUuid == null ) {
			// No brandUuid in path, skip auth (e.g. /brands/code/{code} or non-brand endpoints)
			filterChain.doFilter(request, response);
			return;
		}

		String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (auth == null || !auth.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = auth.substring("Bearer ".length()).trim();
		Jwt jwt;
		try {
			jwt = jwtDecoder.decode(token);
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
			return;
		}

		// Map claims to authorities
		Collection<GrantedAuthority> authorities = new ArrayList<>();

		List<String> roles = jwt.getClaim("roles");
		List<String> groups = jwt.getClaim("groups");

		boolean isAdmin = roles.stream().anyMatch(a -> a.equals("admin"));

		if (!isAdmin) {
			if (brandUuid != null && !brandUuid.isBlank()) {
				// groups claim may contain brand ids; check membership
				boolean hasBrand = false;
				if (groups instanceof Collection) {
					hasBrand = groups.stream().anyMatch(a -> a.equals(brandUuid));
				}

				if (!hasBrand) {
					response.sendError(HttpServletResponse.SC_FORBIDDEN, "Not a member of brand");
					return;
				}
			}
		}

		// Build Authentication and set into context (use Jwt subject as principal name)
		Authentication authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
				jwt.getSubject(), token, authorities);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		filterChain.doFilter(request, response);
	}
}