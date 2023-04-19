package com.dynatrace.easytravel.spring;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;


public class AbstractPagePluginTest {
	private AbstractPagePlugin plugin = new AbstractPagePlugin() {
	};

	@Test
	public void test() throws Exception {
		assertNull(plugin.doExecute(null));
		assertNull(plugin.doResource("somepath"));
		assertNull(plugin.getResponse());
		assertNull(plugin.getHeadInjection());
		assertNull(plugin.getHeader());
		assertNull(plugin.getFooter());
		assertNull(plugin.getFooterScript());
		assertArrayEquals("text".getBytes(), AbstractPagePlugin.getContent("text"));
		AbstractPagePlugin.setResponse(null);
	}

	@Test
	public void testDoExecute() throws Exception {
		assertNull(plugin.doExecute(""));
		assertNull(plugin.doExecute(PluginConstants.FRONTEND_PAGE_RESOURCE, "somepath"));
		assertNull(plugin.doExecute(PluginConstants.FRONTEND_PAGE_EXECUTE));
		assertNull(plugin.doExecute(PluginConstants.FRONTEND_PAGE_EXECUTE, (Object)null));
		assertNull(plugin.doExecute(PluginConstants.FRONTEND_PAGE_HEADINJECTION));
		assertNull(plugin.doExecute(PluginConstants.FRONTEND_PAGE_HEADER));
		assertNull(plugin.doExecute(PluginConstants.FRONTEND_PAGE_FOOTER));
		assertNull(plugin.doExecute(PluginConstants.FRONTEND_PAGE_FOOTER_SCRIPT));
	}

	@Test
	public void testDoExecuteExceptions() throws Exception {
		plugin = new AbstractPagePlugin() {

			@Override
			protected Object doExecute(Map<String, Object> params) throws Exception {
				throw new Exception("testexception");
			}

		};
		assertNull(plugin.doExecute(PluginConstants.FRONTEND_PAGE_EXECUTE));
	}

	@Test
	public void testDoExecuteValues() throws Exception {
		plugin = new AbstractPagePlugin() {

			@Override
			protected Object doExecute(Map<String, Object> params) throws Exception {
				return "execute";
			}

			@Override
			protected Object doResource(String path) {
				return "resource";
			}

			@Override
			public String getHeadInjection() {
				return "headinjection";
			}

			@Override
			public String getHeader() {
				return "header";
			}

			@Override
			public String getFooter() {
				return "footer";
			}

			@Override
			public String getFooterScript() {
				return "footerscript";
			}
		};

		assertEquals("execute", plugin.doExecute(null));
		assertEquals("resource", plugin.doResource("somepath"));
		assertEquals("headinjection", plugin.getHeadInjection());
		assertEquals("header", plugin.getHeader());
		assertEquals("footer", plugin.getFooter());
		assertEquals("footerscript", plugin.getFooterScript());
	}
}