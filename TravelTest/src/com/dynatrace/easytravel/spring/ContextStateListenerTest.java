package com.dynatrace.easytravel.spring;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.junit.Test;
import org.springframework.web.context.WebApplicationContext;

import com.dynatrace.easytravel.utils.TestHelpers;

public class ContextStateListenerTest {

	@Test
	public void test() {
		ServletContext context = createStrictMock(ServletContext.class);
		WebApplicationContext webContext = createStrictMock(WebApplicationContext.class);

		expect(context.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).andReturn(webContext);
		expect(context.getInitParameter("extension.point.start")).andReturn("lifecycle.frontend.start");
		expect(context.getInitParameter("extension.point.shutdown")).andReturn("lifecycle.frontend.shutdown");

		PluginHolder pluginHolder = new PluginHolder();
		List<Plugin> plugins = Collections.emptyList();
		pluginHolder.setPlugins(plugins);
		expect(webContext.getBean("pluginHolder")).andReturn(pluginHolder).anyTimes();

		replay(context, webContext);

		ContextStateListener listener = new ContextStateListener();
		listener.contextInitialized(new ServletContextEvent(context));
		listener.contextDestroyed(new ServletContextEvent(context));

		verify(context, webContext);
	}

	@Test
	public void testInvalidExtensionParam() {
		ServletContext context = createStrictMock(ServletContext.class);
		WebApplicationContext webContext = createStrictMock(WebApplicationContext.class);

		expect(context.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).andReturn(webContext);
		expect(context.getInitParameter("extension.point.start")).andReturn(null);

		replay(context, webContext);

		ContextStateListener listener = new ContextStateListener();

		try {
			listener.contextInitialized(new ServletContextEvent(context));
			fail("Should catch Exception here");
		} catch (IllegalStateException e) {
			TestHelpers.assertContains(e, "Missing servlet-context param", "extension.point.start");
		}

		verify(context, webContext);
	}
}
