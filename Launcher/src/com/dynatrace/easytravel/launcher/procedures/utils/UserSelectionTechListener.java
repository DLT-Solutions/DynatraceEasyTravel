package com.dynatrace.easytravel.launcher.procedures.utils;

import com.dynatrace.easytravel.launcher.agent.Technology;


public interface UserSelectionTechListener {

	void notifyUserChangedState(Technology technology, boolean enabled);
}
