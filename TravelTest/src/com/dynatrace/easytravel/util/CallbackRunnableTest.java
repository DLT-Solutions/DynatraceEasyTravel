package com.dynatrace.easytravel.util;

import java.util.Arrays;

import org.junit.Assert;

import org.junit.Test;

public class CallbackRunnableTest {

	@Test
	public void testRunOrder() {
		final int[] actual = new int[4];
		Runnable run1 = new Runnable() {
			@Override
			public void run() {
				actual[1] = ++actual[0];
			}
		};
		Runnable run2 = new Runnable() {
			@Override
			public void run() {
				actual[2] = ++actual[0];
			}
		};
		Runnable run3 = new Runnable() {
			@Override
			public void run() {
				actual[3] = ++actual[0];
			}
		};
		CallbackRunnable callback = new CallbackRunnable().add(run1).add(run2).add(run3);
		callback.run();
		int[] expected = { 3, 1, 2, 3 }; // the result if the runnables are executed in correct order;
		Assert.assertTrue("Expected " + Arrays.toString(expected) + ", but was: " + Arrays.toString(actual), Arrays.equals(expected, actual));
		callback.run();
		expected = new int[] { 6, 4, 5, 6 }; // the result if the runnables are executed in correct order;
		Assert.assertTrue("Expected " + Arrays.toString(expected) + ", but was: " + Arrays.toString(actual), Arrays.equals(expected, actual));
		callback.clear();
		callback.run();
		Assert.assertTrue("Expected " + Arrays.toString(expected) + ", but was: " + Arrays.toString(actual), Arrays.equals(expected, actual));
	}

	@Test
	public void testEmpty() {
		new CallbackRunnable().run(); // don't throw exception
	}

	@Test
	public void testNesting() {
		final int[] array = new int[1];
		Runnable countUpRun = new Runnable() {
			@Override
			public void run() {
				array[0]++;
			}
		};
		CallbackRunnable c1 = new CallbackRunnable().add(countUpRun).add(countUpRun).add(countUpRun);
		CallbackRunnable c2 = new CallbackRunnable().add(countUpRun).add(countUpRun).add(countUpRun);
		CallbackRunnable all = new CallbackRunnable().add(c1).add(c2).add(countUpRun);
		all.run();
		Assert.assertEquals("Expecting 7 runs", 7, array[0]);
	}
}
