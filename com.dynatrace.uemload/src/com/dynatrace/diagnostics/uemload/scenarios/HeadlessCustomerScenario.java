package com.dynatrace.diagnostics.uemload.scenarios;

import com.dynatrace.diagnostics.uemload.HeadlessCustomerSimulator;
import com.dynatrace.diagnostics.uemload.IterableSet;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.RandomSet;
import com.dynatrace.diagnostics.uemload.Simulator;
import com.dynatrace.diagnostics.uemload.SyntheticAndRobotRandomLocation;
import com.dynatrace.diagnostics.uemload.Visit;
import com.dynatrace.diagnostics.uemload.headless.HeadlessCustomerAlmostConvertedVisit;
import com.dynatrace.diagnostics.uemload.headless.HeadlessCustomerBounceVisit;
import com.dynatrace.diagnostics.uemload.headless.HeadlessCustomerConvertedVisit;
import com.dynatrace.diagnostics.uemload.headless.HeadlessCustomerMagentoShopVisit;
import com.dynatrace.diagnostics.uemload.headless.HeadlessCustomerPageWandererVisit;
import com.dynatrace.diagnostics.uemload.headless.HeadlessCustomerSearchVisit;
import com.dynatrace.diagnostics.uemload.headless.HeadlessCustomerSelectMenuOptionsVisit;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeMonitor;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants.ScenarioNames;
import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class HeadlessCustomerScenario extends HeadlessScenario {

	private static final Logger LOGGER = LoggerFactory.make();

	@Override
	public boolean hasHosts() {
		return getHostsManager().hasCustomerFrontendHost();
	}

	@Override
	protected String getName() {
		return ScenarioNames.HEADLESS_CUSTOMER;
	}

	@Override
	protected IterableSet<Visit> createVisits() {
		LOGGER.trace("Calling createVisits for HeadlessCustomerScenario");
		RandomSet<Visit> set = new RandomSet<>();
		for(String host : getHostsManager().getCustomerFrontendHosts()) {
			set.add(new HeadlessCustomerSelectMenuOptionsVisit(host),32);
			set.add(new HeadlessCustomerBounceVisit(host), 32);
			set.add(new HeadlessCustomerSearchVisit(host), 31);
			set.add(new HeadlessCustomerAlmostConvertedVisit(host), 60);
			set.add(new HeadlessCustomerConvertedVisit(host), 30);
			set.add(new HeadlessCustomerPageWandererVisit(host, true, false), 8);
			set.add(new HeadlessCustomerPageWandererVisit(host, true, true), 1);
		}
		return set;
	}

	@Override
	protected IterableSet<Visit> createRushHourVisits() {
		LOGGER.trace("Calling createRushHourVisits for HeadlessCustomerScenario");
		RandomSet<Visit> set = new RandomSet<>();
		for(String host : getHostsManager().getCustomerFrontendHosts()) {
			set.add(new HeadlessCustomerSelectMenuOptionsVisit(host),32);
			set.add(new HeadlessCustomerBounceVisit(host), 32);
			set.add(new HeadlessCustomerSearchVisit(host), 31);
			set.add(new HeadlessCustomerAlmostConvertedVisit(host), 60);
			set.add(new HeadlessCustomerConvertedVisit(host), 30);
			set.add(new HeadlessCustomerPageWandererVisit(host, true, false), 3);
			set.add(new HeadlessCustomerPageWandererVisit(host, true, true), 1);
			set.add(new HeadlessCustomerPageWandererVisit(host, false, false), 6);
			set.add(new HeadlessCustomerPageWandererVisit(host, false, true), 1);
			if(PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.MAGENTO_SHOP)) {
				String magentoUrl = EasyTravelConfig.read().magentoShopUrl;
				if(magentoUrl != null && "".equals(magentoUrl)) {
					set.add(new HeadlessCustomerMagentoShopVisit(host, magentoUrl), 10);
				}
			}
		}
		return set;
	}

	@Override
	protected IterableSet<Visit> createAnonymousVisits() {
		LOGGER.trace("Calling createAnonymousVisits for HeadlessCustomerScenario");
		RandomSet<Visit> set = new RandomSet<>();
		for(String host : getHostsManager().getCustomerFrontendHosts()) {
			set.add(new HeadlessCustomerSelectMenuOptionsVisit(host),32);
			set.add(new HeadlessCustomerBounceVisit(host), 32);
			set.add(new HeadlessCustomerSearchVisit(host), 31);
			if(PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.MAGENTO_SHOP)) {
				String magentoUrl = EasyTravelConfig.read().magentoShopUrl;
				if(magentoUrl != null && "".equals(magentoUrl)) {
					set.add(new HeadlessCustomerMagentoShopVisit(host, magentoUrl), 10);
				}
			}
		}
		return set;
	}

	@Override
	public Location getRandomLocation() {
		if(SyntheticAndRobotRandomLocation.SINGLETON.isNextLocationRobotOrSynthetic()) {
			return SyntheticAndRobotRandomLocation.SINGLETON.get();
		}
		return super.getRandomLocation();
	}

	@Override
	public Simulator createSimulator() {
		return new HeadlessCustomerSimulator(this);
	}

}
