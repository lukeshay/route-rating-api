package com.routerating.api.common.security;

import com.routerating.api.common.jwt.JwtService;
import com.routerating.api.common.session.SessionService;
import com.routerating.api.common.user.User;
import com.routerating.api.common.user.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

	private static Logger LOG = LoggerFactory.getLogger(JwtAuthorizationFilter.class.getName());

	private UserRepository userRepository;
	private JwtService jwtService;
	private SessionService sessionService;

	public JwtAuthorizationFilter(
			AuthenticationManager authenticationManager,
			JwtService jwtService,
			UserRepository userRepository
	) {
		super(authenticationManager);
		this.jwtService = jwtService;
		this.userRepository = userRepository;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request, HttpServletResponse response, FilterChain chain
	) throws IOException, ServletException {

		String header = request.getHeader(SecurityProperties.JWT_HEADER_STRING);

		if (header == null || !header.startsWith(SecurityProperties.TOKEN_PREFIX)) {
			chain.doFilter(request, response);
			return;
		}

		Authentication authentication = getUsernamePasswordAuthentication(request, response);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		chain.doFilter(request, response);
	}

	private Authentication getUsernamePasswordAuthentication(
			HttpServletRequest request, HttpServletResponse response
	) {

		String jwtToken =
				request.getHeader(SecurityProperties.JWT_HEADER_STRING).replace(SecurityProperties.TOKEN_PREFIX, "");

		if (jwtToken.trim().length() == 0) {
			return null;
		}

		Claims jwtClaims;

		try {
			jwtClaims = jwtService.parseJwtToken(jwtToken);
		} catch (ExpiredJwtException expiredJwtException) {

			Claims refreshClaims;

			try {
				String refreshToken = request.getHeader(SecurityProperties.REFRESH_HEADER_STRING)
				                             .replace(SecurityProperties.TOKEN_PREFIX, "");

				refreshClaims = jwtService.parseJwtToken(refreshToken);
				jwtClaims = expiredJwtException.getClaims();

				if (refreshClaims.getId().equals(jwtClaims.getId()) && refreshClaims.getSubject()
				                                                                    .equals(SecurityProperties.REFRESH_HEADER_STRING)) {

					String newJwtToken = jwtService.buildToken(jwtClaims);

					response.addHeader(
							SecurityProperties.JWT_HEADER_STRING,
							SecurityProperties.TOKEN_PREFIX + newJwtToken
					);
				}
			} catch (ExpiredJwtException | NullPointerException ignored) {
				LOG.debug("No refresh token present or refresh token is expired.");
				jwtClaims = null;
			}
		}

		UsernamePasswordAuthenticationToken authentication = null;

		if (jwtClaims != null && jwtClaims.getSubject().equals(SecurityProperties.JWT_HEADER_STRING)) {
			User user = userRepository.findById(jwtClaims.getId()).orElse(null);

			if (user == null) {
				return null;
			}

			UserPrincipal principal = new UserPrincipal(user);

			LOG.debug("This user token is being created.");

			authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
		}

		return authentication;
	}
}
