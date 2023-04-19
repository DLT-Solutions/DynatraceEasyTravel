package com.dynatrace.diagnostics.uemload;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.dynatrace.diagnostics.uemload.ExtendedCommonUser.ExtendedCommonUserBuilder;
import com.dynatrace.easytravel.misc.LoyaltyStatus;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class VisitorInfoTest {
	
	private static ExtendedCommonUserBuilder builder = new ExtendedCommonUserBuilder("test", "test", LoyaltyStatus.GOLD, "test", 1);
	private static ExtendedCommonUser returningUser = builder.setVisitorInfo(new VisitorInfo("1487753868791R6P0K9F0FEUTG6EQ54U3WNVL6A2KNE9N")).build();
	private static ExtendedCommonUser temporaryUser = builder.setVisitorInfo(new VisitorInfo(true)).build();
	private static ExtendedCommonUser newUser = builder.setVisitorInfo(new VisitorInfo(false)).build();
	
	private static final int noOfChecks = VisitorInfo.BASE_NUMBER_OF_RECURRING_VISITS
			+ VisitorInfo.MAX_NUMBER_OF_ADDITIONAL_VISITS + 1;

	UEMLoadScenario scenario = new UEMLoadScenario() {
		
		@Override
		protected IterableSet<Visit> createVisits() {
			return null;
		}
		
		@Override
		public Simulator createSimulator() {
			return null;
		}
		
	}; 
	
	@Test
	public void returningUserTest() {
		Set<String> ids = drawUsersForTest(returningUser);
		Assert.assertTrue(ids.size() == 1);
	}
	
	@Test
	public void temporaryUserTest() {
		Set<String> ids = drawUsersForTest(temporaryUser);
		Assert.assertTrue(ids.size() > 1 && ids.size() < noOfChecks);
	}
	
	@Test
	public void newUserTest() {
		Set<String> ids = drawUsersForTest(newUser);
		Assert.assertTrue(ids.size() == noOfChecks);
	}
	
	@Test
	public void userTypeTest() {
		Assert.assertTrue(isReturningUser(returningUser));
		Assert.assertFalse(isReturningUser(temporaryUser));
		Assert.assertFalse(isReturningUser(newUser));
	}
	
	private Set<String> drawUsersForTest(ExtendedCommonUser user) {
		Set<String> ids = new HashSet<>();		
		for(int i=0;i<noOfChecks;i++) {
			VisitorInfo visitorInfo = scenario.getVisitorInfo(user);
			ids.add(visitorInfo.createVisitorID().getVisitorId());
		}
		return ids;
	}
	
	private boolean isReturningUser(ExtendedCommonUser user) {
		return scenario.getVisitorInfo(user).isReturningVisitor();
	}

}
