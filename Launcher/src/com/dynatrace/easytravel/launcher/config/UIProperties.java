package com.dynatrace.easytravel.launcher.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: cwpl-zsokolow
 * Date: 24.01.13
 * Time: 12:59
 */
public enum UIProperties {
	PROBLEM_PATTERNS(false, "UIProblemPatterns", false);
    private static List<UIChangeListener> listeners = new ArrayList<UIChangeListener>();
	private boolean enabled;
	private String propertyName;
	private boolean switched; // this property is used when want to be sure that state  property enabled has been recently changed,
		//so we want to refresh UI

	UIProperties(boolean enabled, String propertyName, boolean switched) {
		this.enabled = enabled;
		this.propertyName = propertyName;
		this.switched = switched;
	}

	public boolean getEnabled() {
		return enabled;
	}

	public String getPropertyName() {
		return  propertyName;
	}

	public void setEnabled(boolean state) {
		if (this.enabled != state) {
            switched = true;
            notifyUIListeners(this);  //interface changed
        }
		else {
			switched = false;
		}
		enabled = state;
	}

	public boolean isSwitched() {
		return switched;
	}

    public void clearSwitchFlag() {
        this.switched = false;
    }

	public static UIProperties findByPropName(String propName) {
		for (UIProperties UIProperty: UIProperties.values()) {
			if (propName.equals(UIProperty.getPropertyName())) return UIProperty;
		}
		return null;
	}


    /**
     * adds new UI listener
     * @param newListener - new observer to be registered
     */
    public static void addUIChangeListener(UIChangeListener newListener) {
        listeners.add(newListener);
    }

    public static void removeUIChangeListener(UIChangeListener oldListener) {
    	listeners.remove(oldListener);
    }

    private static void notifyUIListeners(UIProperties elem) {
        for (UIChangeListener listener : listeners) {
            listener.notifyUIChanged(elem);
        }
    }

    }
