package com.dynatrace.easytravel;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import com.dynatrace.easytravel.spring.AbstractPagePlugin;
import com.dynatrace.easytravel.utils.TestHelpers;


public class WPOPagePluginTest {

	@Test
	public void test() {
		WPOPagePlugin plugin = new WPOPagePlugin();

		// just cover the methods to ensure we do not fail in there
		assertNotNull(plugin.getJavaScriptMessage());
		assertNotNull(plugin.getHeader());
		assertNotNull(plugin.doExecute(null));
		//assertNotNull(plugin.doExecute("loc", (Object[])null));
		TestHelpers.ToStringTest(plugin);
		assertNotNull(WPOPagePlugin.someStaticMethod("testarg"));
		assertNotNull(WPOPagePlugin.someStaticMethod(null));

		assertNotNull(plugin.getHeadInjection());

		plugin.loadMinimized = true;
		assertNotNull(plugin.getHeadInjection());

		plugin.loadOptimized = true;
		assertNotNull(plugin.getHeadInjection());

		plugin.loadMinimized = false;
		assertNotNull(plugin.getHeadInjection());

		plugin.loadjQueryUI = false;
		assertNotNull(plugin.getHeadInjection());

		plugin.simulate400s = false;
		assertNotNull(plugin.getHeadInjection());


		assertNull(plugin.getFooter());
		plugin.loadjQueryUI = true;
		assertNotNull(plugin.getFooter());

		HttpServletResponse response = createStrictMock(HttpServletResponse.class);

		response.addHeader("Cache-Control", "private,public,max-age=-1");
		expectLastCall().times(1);
		response.addHeader("Cache-Control", "private,public,max-age=5000000");
		expectLastCall().times(2);

		replay(response);
		AbstractPagePlugin.setResponse(response);

		assertNull(plugin.doResource("testpath"));
		assertNull(plugin.doResource("something.html"));

		plugin.enableCache = true;
		assertNull(plugin.doResource("testpath"));
		assertNull(plugin.doResource("something.html"));

		assertNotNull(plugin.doResource("logback.xml"));

		verify(response);
	}
}
