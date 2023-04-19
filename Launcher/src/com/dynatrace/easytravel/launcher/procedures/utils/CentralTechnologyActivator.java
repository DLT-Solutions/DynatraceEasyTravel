package com.dynatrace.easytravel.launcher.procedures.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.engine.ProcedureStateListener;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.engine.StatefulProcedure;


public class CentralTechnologyActivator implements UserSelectionTechListener, ProcedureStateListener {

	private static CentralTechnologyActivator instance = new CentralTechnologyActivator();

	private Set<TechnologyActivator> activators;

	private CentralTechnologyActivator() {
		activators = new HashSet<TechnologyActivator>();

		activators.add(new DotNetActivator());
	}

	public static CentralTechnologyActivator getIntance() {
		return instance;
	}

	public void registerFrontendListener(TechnologyActivatorListener listener) {
		for (TechnologyActivator activator : activators) {
			activator.registerFrontendListener(listener);
		}
	}

	public void registerBackendListener(TechnologyActivatorListener listener) {
		for (TechnologyActivator activator : activators) {
			activator.registerBackendListener(listener);
		}
	}

	public void unregisterBackendListener(TechnologyActivatorListener listener) {
		for (TechnologyActivator activator : activators) {
			activator.unregisterBackendListener(listener);
		}
	}

	@Override
	public void notifyUserChangedState(Technology technology, boolean enabled) {
		for (TechnologyActivator activator : activators) {
			if (activator.getTechnology() == technology) {
				activator.setEnabled(enabled);
				return;
			}
		}
	}

	public Collection<TechnologyActivator> getActivators() {
		return Collections.unmodifiableSet(activators);
	}

	public void setActivator(String name, boolean enabled) {
		for (TechnologyActivator activator : activators) {
			if (activator.getTechnology().getName().equals(name)) {
				activator.setInitiallyEnabled(enabled);
			}
		}
	}

	public TechnologyActivator getActivator(Technology technology) {
		for (TechnologyActivator activator : activators) {
			if (activator.getTechnology() == technology) {
				return activator;
			}
		}
		throw new IllegalArgumentException("Cannot find activator for given technology: " + technology.getName());
	}

	public boolean isSavingNecessary() {
		for (TechnologyActivator activator : activators) {
			if (activator.isChanged()) {
				return true;
			}
		}
		return false;
	}

	public boolean isPluginAllowed(String name) {
		for (TechnologyActivator activator : activators) {
			if (activator.isEnabled()) {
				continue;
			}
			for (String plugin : activator.getDefaultPlugins()) {
				if (plugin.equals(name)) {
					return false; // procedure tries to enable plugin of a technology that is not enabled
				}
			}
		}
		return true;
	}

	public boolean isProcedureAllowed(String procedureID) {
		for (TechnologyActivator activator : activators) {
			if (!activator.isEnabled() && activator.getTechnology().belongToProcedureId(procedureID)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void notifyProcedureStateChanged(StatefulProcedure subject, State oldState, State newState) {
		for (TechnologyActivator activator : activators) {
			activator.notifyProcedureStateChanged(subject, oldState, newState);
		}
	}

	public boolean isAllowed(Technology technology) {
		for (TechnologyActivator activator : activators) {
			if (!activator.isEnabled() && activator.getTechnology() == technology) {
				return false;
			}
		}
		return true;
	}

}
