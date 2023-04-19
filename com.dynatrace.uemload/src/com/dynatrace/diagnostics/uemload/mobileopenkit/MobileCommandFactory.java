package com.dynatrace.diagnostics.uemload.mobileopenkit;

import com.dynatrace.diagnostics.uemload.mobileopenkit.action.EventNameMapper;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.MobileActionSetPatterns;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.set.MobileActionSet;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.type.MobileActionType;
import com.dynatrace.diagnostics.uemload.mobileopenkit.device.MobileDevice;
import com.dynatrace.diagnostics.uemload.openkit.CommandFactory;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.root.RootAction;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.root.RootActionDefinition;
import com.dynatrace.diagnostics.uemload.openkit.cloudevents.BizEventHelper;
import com.dynatrace.easytravel.frontend.rest.data.JourneyDTO;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.dynatrace.diagnostics.uemload.openkit.event.lifetime.LiveCallbackSet.forDuration;

public class MobileCommandFactory extends CommandFactory {
	private static final Logger LOGGER = Logger.getLogger(MobileCommandFactory.class.getName());

	private static final String LOAD_PREFIX = "LOAD_";

	public static MobileCommandFactory init(MobileDevice device) {
		final MobileCommandFactory factory = new MobileCommandFactory();

		final EventNameMapper eventNameMapper = new EventNameMapper(device.isIOS());
		eventNameMapper.register(MobileActionType.TOUCH_LOGIN, "LoginActivity", "DTLoginViewController");
		eventNameMapper.register(MobileActionType.SEARCH, "SearchJourneyActivity", "DTSearchViewController");
		eventNameMapper.register(MobileActionType.LOAD_SEARCH, "SearchJourneyActivity", "DTSearchViewController");
		eventNameMapper.register(MobileActionType.LOAD_WEBVIEW, "OfflineWebViewActivity", "DTOfflineWebViewController"); // Android name?

		for (MobileActionType action : MobileActionType.values()) {
			if (action.getImplementation() != null)
				bindImplementation(factory, action, device);
			else if (action.name().startsWith(LOAD_PREFIX))
				factory.addCommand(action, () -> run(MobileActionSetPatterns.simpleDisplayViewAction(eventNameMapper.get(action)), device));
		}
		factory.addCommand(MobileActionType.TOUCH_SEARCH, () -> run(MobileActionSetPatterns.touchWidgetAction("Search", eventNameMapper.get(MobileActionType.SEARCH)), device));
		factory.addCommand(MobileActionType.TOUCH_LOGIN, () -> run(MobileActionSetPatterns.touchWidgetAction("User Account", eventNameMapper.get(MobileActionType.TOUCH_LOGIN)), device));
		factory.addCommand(MobileActionType.TOUCH_SPECIAL_OFFERS, () -> run(RootAction.named("Special Offers").live(forDuration(400, 600)), device));
		factory.addCommand(MobileActionType.IDENTIFY_USER, () -> device.identifyUser());
		factory.addCommand(MobileActionType.SELECT_RANDOM_JOURNEY, () -> selectRandomJourneyAction(device));
		return factory;
	}
	
	private static void selectRandomJourneyAction(MobileDevice device) {
		device.selectRandomJourney();
		JourneyDTO dto = device.getSelectedJourney();
		if (dto != null) {
			BizEventHelper.reportS04Event(device, dto.getName(), 
					(int)((dto.getToDate().getTimeInMillis() - dto.getFromDate().getTimeInMillis()) / (24 * 60 * 60 * 1000)), dto.getAmount(), dto.getAverageTotal(), 1);
		}
	}

	private static void run(RootActionDefinition action, MobileDevice device) {
		action.start(device.getActiveSession());
	}

	private static void bindImplementation(MobileCommandFactory cf, MobileActionType actionType, MobileDevice device) {
		MobileActionSet implementation = getImplementation(actionType, device);
		if(implementation == null) return;
		cf.addCommand(actionType, implementation::run);
	}

	public static MobileActionSet getImplementation(MobileActionType actionType, MobileDevice device) {
		try {
			Constructor<? extends MobileActionSet> constructor = actionType.getImplementation().getConstructor(MobileDevice.class);
			return constructor.newInstance(device);
		} catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
			LOGGER.log(Level.SEVERE, "Creating mobile action implementation object " +
					actionType.getImplementation().getName() + " for action " + actionType.toString() + " failed", e);
		}
		return null;
	}
}
