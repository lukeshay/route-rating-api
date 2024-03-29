package com.routerating.api.common.jwt;

import com.routerating.api.common.security.SecurityProperties;
import com.routerating.api.common.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtServiceImpl implements JwtService {
	@Value("${security.secret.jwt}") private String jwtSecret;

	private JwtParser jwtParser;
	private SecretKey secretKey;

	private Claims buildClaims(User user, Long expiration, String subject) {
		Instant now = Instant.now();
		Claims claims = Jwts.claims();

		claims.setExpiration(Date.from(now.plusSeconds(expiration)))
		      .setIssuedAt(Date.from(now))
		      .setSubject(subject)
		      .setId(user.getId())
		      .setIssuer(SecurityProperties.ISSUER);

		return claims;
	}

	@Override
	public Claims buildJwtClaims(User user) {
		return buildClaims(user, SecurityProperties.JWT_EXPIRATION_TIME, SecurityProperties.JWT_HEADER_STRING);
	}

	@Override
	public Claims buildRefreshClaims(User user) {
		return buildClaims(user, SecurityProperties.REFRESH_EXPIRATION_TIME, SecurityProperties.REFRESH_HEADER_STRING);
	}

	@Override
	public String buildToken(Claims claims) {
		return Jwts.builder()
		           .signWith(secretKey)
		           .setHeaderParam(SecurityProperties.JWT_HEADER_PARAM, "JWT")
		           .setClaims(claims)
		           .compact();
	}

	@Override
	public Claims parseJwtToken(String token) {
		return jwtParser.parseClaimsJws(token).getBody();
	}

	@PostConstruct
	private void setSigner() {
		byte[] secret = Base64.getMimeDecoder().decode(jwtSecret);

		jwtParser = Jwts.parserBuilder().setSigningKey(secret).build();
		secretKey = Keys.hmacShaKeyFor(secret);
	}
}
