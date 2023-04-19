package com.dynatrace.diagnostics.uemload.headless;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.misc.CommonUser;

/**
 * superclass for Overload scripts.
 * Sets up a list of actions to perform to simulate cluster overload
 *
 * @author Paul.Johnson
 *
 */
public abstract class HeadlessOverloadBase extends HeadlessVisit  {

	// list of actions Ids to select - set up in subclass constructor
	protected List<By> actionIds = new ArrayList<>();
	private int index = -1;

	public HeadlessOverloadBase(String host) {
		super(host);
	}

	/**
	 * generates a list of actions to execute
	 *
	 */
	@Override
	public Action[] getActions(CommonUser user, Location location) {
		EasyTravelConfig config = EasyTravelConfig.read();
		int overloadActionsPerVisit = config.overloadActionsPerVisit;
		int pause = config.overloadActionPause;

		boolean clickByJS = true;

		List<Action> actions = new ArrayList<>();
		actions.add(new HeadlessGetAction(host));

		while (overloadActionsPerVisit-- > 0) {
			actions.add(new HeadlessClickAction(getNextId(), clickByJS));
			if (pause > 0) {
				actions.add( new HeadlessPauseAction( pause) );
			}
		}
		return actions.toArray(new Action[actions.size()]);
	}

	private By getNextId( ) {
		index++;
		if (index >= actionIds.size()) {
			index = 0;
		}
		return actionIds.get(index);
	}

}
