package com.routerating.api.common;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Handler<T> implements RequestStreamHandler {
	private static SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;


	private Class<T> type;

	public Handler(Class<T> type) {
		this.type = type;
	}

	private static String[] getProfiles() {
		String activeProfiles = System.getenv("SPRING_PROFILES_ACTIVE");

		Set<String> profiles = activeProfiles == null ? new HashSet<>() :
				new HashSet<>(Arrays.asList(activeProfiles.split(",")));
		profiles.add("lambda");
		return profiles.toArray(new String[0]);
	}

	@Override
	public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
		if (handler == null) {
			try {
				handler =
						SpringBootLambdaContainerHandler.getAwsProxyHandler(type, getProfiles());
			} catch (ContainerInitializationException e) {
				e.printStackTrace();
				throw new RuntimeException("Could not initialize Spring Boot application", e);
			}
		}

		handler.proxyStream(inputStream, outputStream, context);
	}
}
