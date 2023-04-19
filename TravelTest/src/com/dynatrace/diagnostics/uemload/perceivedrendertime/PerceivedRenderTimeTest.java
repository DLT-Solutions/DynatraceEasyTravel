package com.dynatrace.diagnostics.uemload.perceivedrendertime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.dynatrace.diagnostics.uemload.thirdpartycontent.ResourceRequestSummary;


public class PerceivedRenderTimeTest {
	@Test
	public void testCreate() {
		List<ResourceRequestSummary> res = Collections.emptyList();
		PerceivedRenderTime.create(res, null, 0);
	}

	@Test
	public void test() {
		List<ResourceRequestSummary> res = Collections.emptyList();
		PerceivedRenderTime time = PerceivedRenderTime.create(res, null, 0);
		assertNull(time.getBrowserWindowSize());

		assertTrue(time.getValue() != 0);
		assertTrue(time.getValue() != 0);

		assertTrue(time.calcValue() != 0);
	}

	@Test
	public void testWithResource() {
		List<ResourceRequestSummary> res = Collections.singletonList(new ResourceRequestSummary("someurl", 123423, 0, 200, "text/plain"));
		PerceivedRenderTime time = PerceivedRenderTime.create(res, null, 0);
		assertNull(time.getBrowserWindowSize());

		assertTrue(time.getValue() != 0);
		assertTrue(time.getValue() != 0);

		assertTrue(time.calcValue() != 0);
	}

	@Test
	public void testWithMultipleResources() {
		List<ResourceRequestSummary> res = new ArrayList<ResourceRequestSummary>();
		res.add(new ResourceRequestSummary("someurl", 123423, 0, 200, "text/plain"));
		res.add(new ResourceRequestSummary("someurl", 123423, 999999999, 200, null));
		res.add(new ResourceRequestSummary("someurl", 123423, 9999999, 200, "image/png"));
		PerceivedRenderTime time = PerceivedRenderTime.create(res, null, 0);
		assertNull(time.getBrowserWindowSize());

		assertTrue(time.getValue() != 0);
		assertTrue(time.getValue() != 0);

		assertTrue(time.calcValue() != 0);
	}

	@Test
	public void simpleSlowestImageUrlTest() {
		List<ResourceRequestSummary> res = new ArrayList<ResourceRequestSummary>();
		res.add(new ResourceRequestSummary("url1", 0, 1000, 200, "text/plain"));
		res.add(new ResourceRequestSummary("url2", 0, 4000, 200, null));
		res.add(new ResourceRequestSummary("url3", 0, 5000, 200, "image/png"));

		PerceivedRenderTime time = PerceivedRenderTime.create(res, null, 0);

		assertEquals(5000, time.getValue());
		assertEquals("url3", time.getSlowestImageUrl());
	}

	@Test
	public void slowestImageUrlTest() {
		List<ResourceRequestSummary> res = new ArrayList<ResourceRequestSummary>();
		res.add(new ResourceRequestSummary("url1", 0, 1000, 200, "text/plain"));
		res.add(new ResourceRequestSummary("url2", 200, 4000, 200, null));
		res.add(new ResourceRequestSummary("url3", 500, 5000, 200, "image/png"));
		res.add(new ResourceRequestSummary("url4", 1500, 6000, 200, "text/plain"));
		res.add(new ResourceRequestSummary("url5", 5800, 6000, 200, "image/png"));
		res.add(new ResourceRequestSummary("url6", 6000, 6500, 200, "text/plain"));

		PerceivedRenderTime time = PerceivedRenderTime.create(res, null, 0);

		assertEquals(6000, time.getValue());
		assertEquals("url5", time.getSlowestImageUrl());
	}
}
