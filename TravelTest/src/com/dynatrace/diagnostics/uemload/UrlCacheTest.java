/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: UrlCacheTest.java
 * @date: 27.11.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.diagnostics.uemload;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.util.concurrent.TimeUnit;

import org.junit.Test;



/**
 *
 * @author stefan.moschinski
 */
public class UrlCacheTest {


	@Test
	public void testReturnedInstancesAreEqual() {
		UrlCache cache = UrlCache.createWithExpiryTime(5, TimeUnit.MINUTES);
		String blub = cache.cashify(new String("blub"));
		String blub2 = cache.cashify(new String("blub"));
		String blub3 = cache.cashify(new String("blub"));

		assertThat(blub2, is(sameInstance(blub)));
		assertThat(blub3, is(sameInstance(blub)));
	}

	@Test
	public void testElementExpiresAfterDeterminedTimeOfNoAccess() throws InterruptedException {
		UrlCache cache = UrlCache.createWithExpiryTime(500, TimeUnit.MILLISECONDS);
		String blub = cache.cashify(new String("blub"));

		TimeUnit.MILLISECONDS.sleep(600);

		assertThat(cache.get(blub), is(nullValue()));
		assertThat(cache.cashify(new String("blub")), is(not(sameInstance(blub))));
	}
}
