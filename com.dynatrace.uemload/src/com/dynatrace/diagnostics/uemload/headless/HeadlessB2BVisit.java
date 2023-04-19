package com.dynatrace.diagnostics.uemload.headless;

import java.util.ArrayList;
import java.util.List;

import static com.dynatrace.diagnostics.uemload.HeadlessBySelectors.*;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.easytravel.constants.BaseConstants.B2BAccount;


/**
 * Class that holds utility functions / actions specific to b2b frontend.
 * @author krzysztof.sajko
 * @Date 2021.10.05
 */
public abstract class HeadlessB2BVisit extends HeadlessVisit {

	public HeadlessB2BVisit(String host) {
		super(host);
	}
	
	/**
	 * Requires the driver to already be on b2b frontend site
	 * @return List of actions that will log on account
	 */
	protected List<Action> getLoginActions(B2BAccount account){
		List<Action> actions = new ArrayList<>();
		actions.add(new HeadlessClickAction(B2BHomeJourneys.get()));
		actions.add(new HeadlessSendKeysAction(B2BLoginFormUsername.get(), account.getLogin()));
		actions.add(new HeadlessSendKeysAction(B2BLoginFormPassword.get(), account.getPassword()));
		actions.add(new HeadlessClickAction(B2BLoginFormSubmit.get()));
		return actions;
	}
}
