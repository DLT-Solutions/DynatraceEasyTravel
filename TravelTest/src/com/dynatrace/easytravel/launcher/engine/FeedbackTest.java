package com.dynatrace.easytravel.launcher.engine;

import static org.junit.Assert.*;

import org.junit.Test;



public class FeedbackTest {
	@Test
	public void testIsOk() {
		assertTrue(Feedback.Success.isOk());
		assertTrue(Feedback.Neutral.isOk());
		assertFalse(Feedback.Failure.isOk());
	}
	
	@Test
	public void testGetMostSevere() {
		assertEquals(Feedback.Failure, Feedback.getMostSevere(Feedback.Failure, Feedback.Failure));
		assertEquals(Feedback.Failure, Feedback.getMostSevere(Feedback.Neutral, Feedback.Failure));
		assertEquals(Feedback.Failure, Feedback.getMostSevere(Feedback.Failure, Feedback.Success));

		assertEquals(Feedback.Neutral, Feedback.getMostSevere(Feedback.Neutral, Feedback.Neutral));
		assertEquals(Feedback.Neutral, Feedback.getMostSevere(Feedback.Success, Feedback.Neutral));
		assertEquals(Feedback.Neutral, Feedback.getMostSevere(Feedback.Neutral, Feedback.Success));

		assertEquals(Feedback.Success, Feedback.getMostSevere(Feedback.Success, Feedback.Success));
	}
}
