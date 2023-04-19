package com.dynatrace.diagnostics.uemload.headless;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.HeadlessBySelectors;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.Visit;
import com.dynatrace.diagnostics.uemload.headless.HeadlessVisit.HeadlessClickAction;
import com.dynatrace.diagnostics.uemload.headless.HeadlessVisit.HeadlessGetAction;
import com.dynatrace.diagnostics.uemload.headless.HeadlessVisit.HeadlessSelectAction;
import com.dynatrace.diagnostics.uemload.headless.HeadlessVisit.HeadlessWaitAction;
import com.dynatrace.diagnostics.uemload.headless.actions.HeadlessAngularScroll;
import com.dynatrace.easytravel.constants.BaseConstants.VisitExtras.OnlineBoutique.SelectQuantity;
import com.dynatrace.easytravel.constants.BaseConstants.VisitNames;
import com.dynatrace.easytravel.misc.CommonUser;

public class HeadlessOnlineBoutiqueBuyVisit implements Visit {
	private static final List<HeadlessBySelectors> productSelectors = HeadlessBySelectors.getOnlineBoutiqueProducts();
	private static final Random random = new Random();

	protected String host;

	public HeadlessOnlineBoutiqueBuyVisit(String host) {
		this.host = host;
	}

	@Override
	public Action[] getActions(CommonUser user, Location location) {
		List<Action> actions = new ArrayList<>();
		actions.add(new HeadlessGetAction(host));
		actions.addAll(getOrderProductActions(getRandomProduct(), getRandomQuantity()));
		actions.add(new HeadlessClickAction(HeadlessBySelectors.OnlineBoutiqueViewCartButton.get()));
		actions.add(new HeadlessWaitAction(1000));
		actions.add(new HeadlessClickAction(HeadlessBySelectors.OnlineBoutiquePlaceYourOrderButton.get()));

		return actions.toArray(new Action[actions.size()]);
	}

	private List<HeadlessBySelectors> getRandomProductPermutation() {
		List<HeadlessBySelectors> copy = new ArrayList<>(productSelectors);
		Collections.shuffle(copy);
		return copy;
	}

	private HeadlessBySelectors getRandomProduct(){
		return productSelectors.get(random.nextInt(productSelectors.size()));
	}

	private List<Action> getOrderProductActions(HeadlessBySelectors product, SelectQuantity quantity) {
		return Arrays.asList(
				new HeadlessClickAction(product.get()),
				new HeadlessWaitAction(1000),
				new HeadlessSelectAction(HeadlessBySelectors.OnlineBoutiqueQuantitySelect.get(), quantity.get()),
				new HeadlessWaitAction(1000),
				new HeadlessAngularScroll(2000, -10),
				new HeadlessClickAction(HeadlessBySelectors.OnlineBoutiqueAddToCartButton.get()),
				new HeadlessWaitAction(1000),
				new HeadlessClickAction(HeadlessBySelectors.OnlineBoutiqueKeepBrowsingButton.get()),
				new HeadlessWaitAction(1000));
	}

	private SelectQuantity getRandomQuantity() {
		return SelectQuantity.values.get(random.nextInt(SelectQuantity.values.size()));
	}

	@Override
	public String getVisitName() {
		return VisitNames.HEADLESS_ONLINE_BOUTIQUE_BUY_VISIT;
	}

}
