package com.routerating.api.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class APIFilter implements Filter {

	private static Logger LOG = LoggerFactory.getLogger(APIFilter.class.getName());

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(
			ServletRequest req,
			ServletResponse res,
			FilterChain chain
	) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		LOG.debug("Filtering: method: {}, origin: {}, endpoint: {}, authorization: {}",
				request.getMethod(),
				request.getHeader("origin"),
				request.getRequestURI() + "?" + request.getQueryString(),
				request.getHeader("Authorization")
		);

		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Methods", "POST, PUT, GET, DELETE, OPTIONS");
		response.setHeader("Access-Control-Max-Age", "3600");
		response.setHeader(
				"Access-Control-Allow-Headers",
				"Authorization, Refresh, origin, content-type, accept, x-requested-with"
		);

		chain.doFilter(req, res);
	}

	@Override
	public void destroy() {

	}
}
