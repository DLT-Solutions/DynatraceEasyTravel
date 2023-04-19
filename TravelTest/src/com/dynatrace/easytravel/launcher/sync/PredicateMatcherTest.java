package com.dynatrace.easytravel.launcher.sync;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;


public class PredicateMatcherTest {

	@Test
	public void testWaitForMatch() throws Exception {
		PredicateMatcher<String> matcher = new PredicateMatcher<String>("stringnot", 100, 100);
		assertFalse("no match if string is not equal",
				matcher.waitForMatch(new MyPredicate()));

		matcher = new PredicateMatcher<String>("stringtrue", 100, 100);
		assertTrue("match if string is equal",
				matcher.waitForMatch(new MyPredicate()));

		stop = true;
		matcher = new PredicateMatcher<String>("stringnot", 100, 100);
		assertTrue("match if stop is selected, even if string is false",
				matcher.waitForMatch(new MyPredicate()));
	}

	@Test
	public void testWaitForMatchInterrupted() throws Exception {
		final AtomicReference<Exception> exception = new AtomicReference<Exception>();
		stop = false;
		Thread thread = new Thread() {
			@Override
			public void run() {
				PredicateMatcher<String> matcher = new PredicateMatcher<String>("stringfalse", 1000, 1000);
				if(matcher.waitForMatch(new MyPredicate())) {
					exception.set(new IllegalStateException("Did not expect to have true here as we should be interrupted from sleeping"));
				}
			}
		};
		thread.start();
		Thread.sleep(100);	// let it start up

		// now interrupt the thread to enter this code path as well
		thread.interrupt();

		thread.join();

		assertNull("Should not have exception, but had: " + exception.get(),
				exception.get());
	}


	@Test
	public void testWaitForNotMatch() throws Exception {
		PredicateMatcher<String> matcher = new PredicateMatcher<String>("stringtrue", 100, 100);
		assertTrue("not match if string is not equal",
				matcher.waitForNotMatch(new MyPredicate()));

		matcher = new PredicateMatcher<String>("stringnot", 100, 100);
		assertFalse("match if string is equal",
				matcher.waitForNotMatch(new MyPredicate()));

		stop = true;
		matcher = new PredicateMatcher<String>("stringtrue", 100, 100);
		assertTrue("not match if stop is selected, even if string is equal",
				matcher.waitForNotMatch(new MyPredicate()));
	}

	@Test
	public void testWaitForNotMatchInterrupted() throws Exception {
		final AtomicReference<Exception> exception = new AtomicReference<Exception>();
		stop = false;
		Thread thread = new Thread() {
			@Override
			public void run() {
				PredicateMatcher<String> matcher = new PredicateMatcher<String>("stringtrue", 1000, 1000);
				if(matcher.waitForNotMatch(new MyPredicate())) {
					exception.set(new IllegalStateException("Did expect to have false here as we should be interrupted from sleeping"));
				}
			}
		};
		thread.start();
		Thread.sleep(100);	// let it start up

		// now interrupt the thread to enter this code path as well
		thread.interrupt();

		thread.join();

		assertNull("Should not have exception, but had: " + exception.get(),
				exception.get());
	}

	private volatile boolean stop = false;

	private final class MyPredicate implements Predicate<String> {
		@Override
		public boolean shouldStop() {
			return stop;
		}

		@Override
		public boolean eval(String obj) {
			return "stringtrue".equals(obj);
		}
	}
}
