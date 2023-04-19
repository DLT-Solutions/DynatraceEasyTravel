package com.dynatrace.diagnostics.uemload;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


public class BrowserCacheTest {

	@Before
	public void setUp() throws Exception {
	}


	@Test
	public void regexCheckIsNoCachedFiletype() {
		assertFalse(BrowserCache.isNoCachedFiletype("http://www.test.com/glub.png"));
		assertFalse(BrowserCache.isNoCachedFiletype("http://www.test.de/jQuery-234.34.js/"));
		assertFalse(BrowserCache.isNoCachedFiletype("http://www.test.de/jQuery-234.JS/"));
		assertFalse(BrowserCache.isNoCachedFiletype("http://www.test.de/styles.css"));
		assertFalse(BrowserCache.isNoCachedFiletype("http://www.test.de/styles.jpg"));
		assertFalse(BrowserCache.isNoCachedFiletype("http://www.test.de/styles.jpeg"));


		assertTrue(BrowserCache.isNoCachedFiletype("http://localhost:8080/javax.faces.resource/bridge.js.jsf?rand=1427668659"));
		assertTrue(BrowserCache.isNoCachedFiletype("http://www.test.de/jQuery-234.jsf/"));
		assertTrue(BrowserCache.isNoCachedFiletype("http://www.test.de/javax.faces.context"));
		assertTrue(BrowserCache.isNoCachedFiletype("http://www.test.de/styles.css/fake"));
	}


	@Test
	@Ignore("Fails currently, needs to be done differently to be runnable in CI")
	public void testIsLoadNecessarySequentially() throws InterruptedException {
		final String urlPng = "http://url1.com/picture.png";

		BrowserCache cache = new BrowserCache();
		BrowserCache cache2 = new BrowserCache();

		assertThat(cache.isLoadOfResourceNecessary(urlPng), is(true));

		cache.addResource(urlPng, 2);
		assertThat(cache.isLoadOfResourceNecessary(urlPng), is(false));
		assertThat(cache2.isLoadOfResourceNecessary(urlPng), is(true));

		TimeUnit.MILLISECONDS.sleep(2001);
		cache2.addResource(urlPng, 20000);
		assertThat(cache.isLoadOfResourceNecessary(urlPng), is(true));
		assertThat(cache2.isLoadOfResourceNecessary(urlPng), is(false));

		cache.addResource(urlPng, 10);
		assertThat(cache.isLoadOfResourceNecessary(urlPng), is(false));
		assertThat(cache2.isLoadOfResourceNecessary(urlPng), is(false));
	}

}
