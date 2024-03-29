package com.routerating.api.common.jwt;

import com.routerating.api.common.user.User;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface JwtService {

	Logger LOG = LoggerFactory.getLogger(JwtService.class.getName());

	static Long getExpirationInMinutes(Claims claims) {
		return (claims.getExpiration().getTime() - claims.getIssuedAt().getTime()) / 60000;
	}

	Claims buildJwtClaims(User user);

	Claims buildRefreshClaims(User user);

	String buildToken(Claims claims);

	Claims parseJwtToken(String token);
}
