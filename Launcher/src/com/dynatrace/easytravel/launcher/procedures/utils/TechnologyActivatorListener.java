package com.dynatrace.easytravel.launcher.procedures.utils;

import java.util.Collection;

import com.dynatrace.easytravel.launcher.agent.Technology;


public interface TechnologyActivatorListener {

	void notifyTechnologyStateChanged(Technology technology, boolean enabled, Collection<String> plugins, Collection<String> substitutes);

}
