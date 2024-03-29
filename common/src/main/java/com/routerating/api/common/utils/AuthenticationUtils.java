package com.routerating.api.common.utils;

import com.routerating.api.common.security.UserPrincipal;
import com.routerating.api.common.user.User;
import org.springframework.security.core.Authentication;

import java.util.Optional;

public class AuthenticationUtils {

	public static User getUser(Authentication authentication) {
		if (authentication == null) {
			return null;
		}
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		return userPrincipal == null ? null : userPrincipal.getUser();
	}

	public static Optional<User> getUserOptional(Authentication authentication) {
		if (authentication == null) {
			return Optional.empty();
		}
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		return Optional.ofNullable(userPrincipal.getUser());
	}
}
