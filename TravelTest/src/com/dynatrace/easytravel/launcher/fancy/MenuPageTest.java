package com.dynatrace.easytravel.launcher.fancy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class MenuPageTest {

	@Test
	public void testMenuPage() {
		MenuPage page = new MenuPage("image", "title", null, null);
		
		assertEquals("image", page.getImage());
		page.setImage("anotheri");
		assertEquals("anotheri", page.getImage());
		
		assertEquals("title", page.getTitle());
		page.setTitle("anothert");
		assertEquals("anothert", page.getTitle());
		
		assertNull(page.getAction());
		page.setAction(new MenuActionCallback() {
			
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
		assertNotNull(page.getAction());
		
		page.addItem(new MenuItem("image", "title", "desc", null, null));
		
		// TODO: cannot test this without SWT
		//page.createButton(tabs)
		//page.createContent(pageArea, layoutCallback)
		

		page.add(new MenuPage("image2", "title2", null, null));
	}

	/**
	 * @author christoph.neumueller
	 */
	@Test
	public void testFilterMenuPage() {
		MenuPage page = new MenuPage("image", "title", null, null);
		
		MenuItem itemAAAA = new MenuItem("img", "aaaa", "", null, null);
		MenuItem itemBBBB = new MenuItem("img", "bbbb", "", null, null);
		MenuItem itemCCCC = new MenuItem("img", "cccc", "", null, null);
		MenuItem itemABCD = new MenuItem("img", "abcd", "", null, null);

		itemAAAA.setAction(new MenuActionCallback() {
			
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
		
		itemBBBB.setAction(new MenuActionCallback() {
			
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
		
		itemCCCC.setAction(new MenuActionCallback() {
			
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

		itemABCD.setAction(new MenuActionCallback() {
			
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

		
		page.addItem(itemAAAA);
		page.addItem(itemBBBB);
		page.addItem(itemCCCC);
		page.addItem(itemABCD);

		assertTrue(itemAAAA.checkVisible());
		assertTrue(itemBBBB.checkVisible());
		assertTrue(itemCCCC.checkVisible());
		assertTrue(itemABCD.checkVisible());

		page.applyFilter(new FilterTaskParams("a"));
		assertTrue(itemAAAA.checkVisible());
		assertFalse(itemBBBB.checkVisible());
		assertFalse(itemCCCC.checkVisible());
		assertTrue(itemABCD.checkVisible());

		page.applyFilter(new FilterTaskParams("bb"));
		assertFalse(itemAAAA.checkVisible());
		assertTrue(itemBBBB.checkVisible());
		assertFalse(itemCCCC.checkVisible());
		assertFalse(itemABCD.checkVisible());

		page.applyFilter(new FilterTaskParams("bcd"));
		assertFalse(itemAAAA.checkVisible());
		assertFalse(itemBBBB.checkVisible());
		assertFalse(itemCCCC.checkVisible());
		assertTrue(itemABCD.checkVisible());

		page.clearFilter();
		assertTrue(itemAAAA.checkVisible());
		assertTrue(itemBBBB.checkVisible());
		assertTrue(itemCCCC.checkVisible());
		assertTrue(itemABCD.checkVisible());
	}
}
