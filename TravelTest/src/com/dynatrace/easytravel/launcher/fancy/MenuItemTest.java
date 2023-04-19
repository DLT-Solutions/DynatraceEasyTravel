package com.dynatrace.easytravel.launcher.fancy;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import com.dynatrace.easytravel.utils.TestHelpers;



public class MenuItemTest {
	@Test
	public void test() {
		MenuItem item = new MenuItem("image", "title", "desc", null, null);

		assertEquals("title", item.getTitle());
		item.setTitle("another");
		assertEquals("another", item.getTitle());

		assertEquals("image", item.getImage());
		item.setImage("anotheri");
		assertEquals("anotheri", item.getImage());

		assertEquals("desc", item.getDescriptionText());
		item.setDescriptionText("anotherd");
		assertEquals("anotherd", item.getDescriptionText());

		assertNull(item.getAction());
		item.setAction(new MenuActionCallback() {

			@Override
			public void stop() {
			}

			@Override
			public void setText(String text) {
			}

			@Override
			public void run() {
			}

			@Override
			public String getText() {
				return null;
			}

			@Override
			public Boolean isEnabled() {
				return null;
			}
		});
		assertNotNull(item.getAction());

		item = new MenuItem("image", "title", "desc", new MenuActionCallback() {

			@Override
			public void stop() {
			}

			@Override
			public void setText(String text) {
			}

			@Override
			public void run() {
			}

			@Override
			public String getText() {
				return null;
			}

			@Override
			public Boolean isEnabled() {
				return null;
			}
		}, null, "unavailable");
		assertNotNull(item.getAction());

		assertEquals("unavailable", item.getUnavailableLinkText());
		item.setUnavailableLinkText("anotheru");
		assertEquals("anotheru", item.getUnavailableLinkText());

		// cannot test SWT stuff: item.createComponent(parent, page, layoutCallback)
	}

	@Test
	public void testEquals() {
		MenuItem item = new MenuItem("image", "title", "desc", null, null);
		MenuItem equal = new MenuItem("image", "title", "desc", null, null);
		MenuItem other = new MenuItem("image", "title2", "desc", null, null);

		TestHelpers.EqualsTest(item, equal, other);
		TestHelpers.HashCodeTest(item, equal);

		item = new MenuItem("image", "title", "desc", null, null);
		equal = new MenuItem("image", "title", "desc", null, null);
		other = new MenuItem("image2", "title", "desc", null, null);

		TestHelpers.EqualsTest(item, equal, other);
		TestHelpers.HashCodeTest(item, equal);

		item = new MenuItem("image", "title", "desc", null, null);
		equal = new MenuItem("image", "title", "desc", null, null);
		other = new MenuItem("image", "title", "desc2", null, null);

		TestHelpers.EqualsTest(item, equal, other);
		TestHelpers.HashCodeTest(item, equal);

		item = new MenuItem(null, null, null, null, null);
		equal = new MenuItem(null, null, null, null, null);

		TestHelpers.EqualsTest(item, equal, other);
		TestHelpers.HashCodeTest(item, equal);
	}

	@Test
	public void testCheckFilter() {
		MenuItem item = new MenuItem("image", "title", "desc", null, null);
		assertTrue(item.checkFilter());

		final AtomicBoolean filterState = new AtomicBoolean();
		item = new MenuItem("image", "title", "desc", new MenuActionCallback() {
			@Override
			public void stop() {
			}

			@Override
			public void setText(String text) {
			}

			@Override
			public void run() {
			}

			@Override
			public Boolean isEnabled() {
				return filterState.get();
			}

			@Override
			public String getText() {
				return null;
			}
		}, null);
		assertTrue(item.checkFilter());

		// boolean filter
		item.applyFilter(new FilterTaskParams(Boolean.TRUE));
		assertFalse("Returns isEnabled state", item.checkFilter());
		item.applyFilter(new FilterTaskParams(Boolean.FALSE));
		assertTrue("Returns isEnabled state", item.checkFilter());

		filterState.set(true);
		item.applyFilter(new FilterTaskParams(Boolean.TRUE));
		assertTrue("Returns isEnabled state", item.checkFilter());
		item.applyFilter(new FilterTaskParams(Boolean.FALSE));
		assertFalse("Returns isEnabled state", item.checkFilter());

		// text filter
		item.applyFilter(new FilterTaskParams((String)null));
		assertTrue(item.checkFilter());

		item.applyFilter(new FilterTaskParams("sometext"));
		assertFalse("Title/Description and filter should not match now", item.checkFilter());

		item.applyFilter(new FilterTaskParams("title"));
		assertTrue("Title/Description and filter should match now", item.checkFilter());

		item.applyFilter(new FilterTaskParams("desc"));
		assertTrue("Title/Description and filter should match now", item.checkFilter());

		item.setTitle(null);
		assertTrue("Title/Description and filter should match now", item.checkFilter());

		item.setDescriptionText(null);
		assertFalse("no match any more", item.checkFilter());
	}
}
