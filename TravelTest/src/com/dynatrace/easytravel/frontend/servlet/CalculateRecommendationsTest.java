package com.dynatrace.easytravel.frontend.servlet;

import static org.easymock.EasyMock.expect;

import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.easymock.EasyMock.*;

import org.junit.Test;

import com.dynatrace.easytravel.config.EasyTravelConfig;

import junit.framework.AssertionFailedError;

public class CalculateRecommendationsTest extends ServletTestBase {

	@Test
	public void testTimeout() throws ServletException, IOException {
		CalculateRecommendations cr = new CalculateRecommendations();

		HttpServletRequest requestMock = createNiceMock(HttpServletRequest.class);
		HttpServletResponse responseMock = createNiceMock(HttpServletResponse.class);
		AsyncContext asyncContextMock = createNiceMock(AsyncContext.class);

		expect(requestMock.startAsync()).andReturn(asyncContextMock).anyTimes();
		asyncContextMock.setTimeout(10);

		replay(requestMock, responseMock, asyncContextMock);

		EasyTravelConfig config = EasyTravelConfig.read();
		try {
			config.asyncContextTimeout = 10;
			config.enableRecommendationBean = true;
			cr.init();
			cr.doGet(requestMock, responseMock);
		} finally {
			EasyTravelConfig.resetSingleton();
		}

		verify(asyncContextMock);
	}

	@Test
	public void testEnableFlag() throws ServletException, IOException {
		CalculateRecommendations cr = new CalculateRecommendations();

		HttpServletRequest requestMock = createNiceMock(HttpServletRequest.class);
		HttpServletResponse responseMock = createNiceMock(HttpServletResponse.class);
		AsyncContext asyncContextMock = createNiceMock(AsyncContext.class);

		expect(requestMock.startAsync())
				.andThrow(new AssertionFailedError("Method startAsync() was called, but wasn't expected.")).anyTimes();

		replay(requestMock, responseMock, asyncContextMock);

		EasyTravelConfig config = EasyTravelConfig.read();
		config.enableRecommendationBean = false;
		try {
			cr.init();
			cr.doGet(requestMock, responseMock);
		} finally {
			EasyTravelConfig.resetSingleton();
		}

		verify(asyncContextMock);
	}
}
