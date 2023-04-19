package com.dynatrace.diagnostics.uemload;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.dynatrace.diagnostics.uemload.http.base.UemLoadHttpClient;
import com.dynatrace.diagnostics.uemload.thirdpartycontent.ResourceRequestSummary;
import com.dynatrace.easytravel.constants.BaseConstants;


public class AbstractUemActionTest {

	@Test
	public void testGetStartTime() throws InterruptedException {
		AbstractUemAction action = new MockUemAction(new NullJavaScriptAgent(null, null, null, null, null));

		assertTrue(action.getStartTime() > 0);
		assertTrue(action.getStartTime() <= System.currentTimeMillis());
	}

	@Test
	public void testElapsedMillis() throws InterruptedException {
		AbstractUemAction action = new MockUemAction(new NullJavaScriptAgent(null, null, null, null, null));

		long elapsedMillis = action.elapsedMillis();
		assertTrue("Had: " + elapsedMillis, elapsedMillis >= 0);

		Thread.sleep(100);

		elapsedMillis = action.elapsedMillis();
		assertTrue(action.elapsedMillis() > 50);

		action.sendActionPreview();

		action.resetTimer();
		elapsedMillis = action.elapsedMillis();
		assertTrue("Had: " + elapsedMillis, elapsedMillis < 50);
		assertEquals("Zero when comparing with itself", 0, action.compareTo(action));
	}

	@Test
	public void testCompareTo() throws InterruptedException {
		AbstractUemAction action1 = new MockUemAction(new NullJavaScriptAgent(null, null, null, null, null));
		Thread.sleep(100);	// let first action get some elapsed time
		AbstractUemAction action2 = new MockUemAction(new NullJavaScriptAgent(null, null, null, null, null));

		System.out.println("Action1: " + action1.elapsedMillis() + ", action2: " + action2.elapsedMillis());

		// TODO: the implementation of compareTo is strange, it only returns 0 if the hashes are equal, but does not implement hashCode()!
		// TestHelpers.CompareToTest((UemAction)action1, action2, action);
		assertEquals("-1 when comparing with one another", -1, action1.compareTo(action2));
		assertEquals("1 when comparing with one another", 1, action2.compareTo(action1));
	}

	@Test
	public void testCompareEqualElapsedTime() {
		// one more test, now have the same millis to test on hashcode
		AbstractUemAction action1 = new MockUemAction(new NullJavaScriptAgent(null, null, null, null, null)) {
			@Override
			public long elapsedMillis() {
				return 1;
			}
		};
		AbstractUemAction action2 = new MockUemAction(new NullJavaScriptAgent(null, null, null, null, null)) {
			@Override
			public long elapsedMillis() {
				return 1;
			}
		};

		assertEquals("Usually elapsed time will be equal, but it may happen that we get another ms between the two actions",
				action1.elapsedMillis(), action2.elapsedMillis());
		assertTrue("should not be equal now because is based on hashCode", 0 != action1.compareTo(action2));
		assertTrue("should not be equal now because is based on hashCode", 0 != action2.compareTo(action1));
		assertEquals("should be equal when comparing to itself", 0, action1.compareTo(action1));
		assertEquals("should be equal when comparing to itself", 0, action2.compareTo(action2));

	}

	@Test
	public void testActionPreviewException() {
		AbstractUemAction action = new MockUemAction(new NullJavaScriptAgent(null, null, null, null, null)) {

			@Override
			protected void sendActionPreviewInternal() throws IOException {
				throw new IOException("testexception");
			}

		};

		// internal action is only logged, but not thrown to the outside
		action.sendActionPreview();
	}

    private class MockUemAction extends AbstractUemAction {

		private MockUemAction(JavaScriptAgent agent) {
			super(agent);
		}

		@Override
		public ActionType getTye() {
			return null;
		}

		@Override
		protected void sendActionPreviewInternal() throws IOException {
		}
	}

	static class NullJavaScriptAgent extends JavaScriptAgent {

        public NullJavaScriptAgent(UemLoadHttpClient http, String url, String title, Bandwidth bandwidth, String sourceAction) {
            super(http, url, title, bandwidth, sourceAction, null, 0);
        }

        @Override
        public void pageLoadStarted(String html) throws IOException {
        }

        @Override
		public void pageLoadFinished(List<ResourceRequestSummary> loadedResources, NavigationTiming nt, BrowserWindowSize bws,
				long viewDuration)

                throws IOException {
        }

        @Override
		public void startCustomAction(String customActionName, String customActionType, String customActionInfo) {
        }

        @Override
        public int stopCustomAction(boolean isIncomplete, List<ResourceRequestSummary> loadedResources, NavigationTiming nt,
				BrowserWindowSize bws, long viewDuration) throws IOException {
        	return -1;
        }

        @Override
        public String getSourceAction() {
            return BaseConstants.EMPTY_STRING;
        }
    }
}
