package com.routerating.api.common.config;

import com.routerating.api.common.jwt.JwtService;
import com.routerating.api.common.security.JwtAuthenticationFilter;
import com.routerating.api.common.security.JwtAuthorizationFilter;
import com.routerating.api.common.session.SessionService;
import com.routerating.api.common.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	private UserDetailsService userDetailsService;
	private UserRepository userRepository;
	private SessionService sessionService;
	private JwtService jwtService;

	@Autowired
	public SecurityConfiguration(
			@Qualifier("userPrincipalService") UserDetailsService userDetailsService,
			UserRepository userRepository,
			SessionService sessionService,
			JwtService jwtService
	) {
		this.userDetailsService = userDetailsService;
		this.userRepository = userRepository;
		this.sessionService = sessionService;
		this.jwtService = jwtService;
	}

	@Bean
	DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();

		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
		daoAuthenticationProvider.setUserDetailsService(userDetailsService);

		return daoAuthenticationProvider;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf()
		    .disable()
		    .cors()
		    .and()
		    .sessionManagement()
		    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
		    .and()
		    .addFilter(new JwtAuthenticationFilter(authenticationManager(), jwtService, sessionService, userRepository))
		    .addFilter(new JwtAuthorizationFilter(authenticationManager(), jwtService, userRepository))
		    .authorizeRequests()
		    .anyRequest()
		    .permitAll();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService);
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Collections.singletonList("*"));
		configuration.setAllowedMethods(Arrays.asList(HttpMethod.GET.name(),
				HttpMethod.HEAD.name(),
				HttpMethod.POST.name(),
				HttpMethod.PUT.name(),
				HttpMethod.DELETE.name(),
				HttpMethod.OPTIONS.name()
		));
		configuration.addAllowedHeader("*");
		configuration.addExposedHeader("Access-Control-Allow-Origin");
		configuration.addExposedHeader("Access-Control-Allow-Methods");
		configuration.addExposedHeader("Access-Control-Allow-Headers");
		configuration.addExposedHeader("Access-Control-Max-Age");
		configuration.addExposedHeader("Access-Control-Request-Headers");
		configuration.addExposedHeader("Access-Control-Request-Method");
		configuration.addExposedHeader("Authentication");
		configuration.addExposedHeader("accept");

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
