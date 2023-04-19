package com.dynatrace.diagnostics.uemload.mobileopenkit.action;

import com.dynatrace.diagnostics.uemload.openkit.action.EventType;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class EventNameMapper {
	private final boolean isIOS;

	public EventNameMapper(boolean isIOS) {
		this.isIOS = isIOS;
	}

	private final Logger log = Logger.getLogger(EventNameMapper.class.getName());

	private final Map<EventType, String> actionTypeNameMap = new HashMap<>();

	public void register(EventType action, String commonPrefix, String androidName, String iOSName) {
		actionTypeNameMap.put(action, isIOS ? commonPrefix + iOSName : commonPrefix + androidName);
	}

	public void register(EventType action, String androidName, String iOSName) {
		register(action, "", androidName, iOSName);
	}

	public String get(EventType actionType) {
		if (!actionTypeNameMap.containsKey(actionType)) {
			log.warning(() -> "Action to device view map does not contain action " + actionType.toString());
			return "";
		}
		return actionTypeNameMap.get(actionType);
	}
}
