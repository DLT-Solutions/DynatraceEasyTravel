package com.dynatrace.easytravel.launcher.engine;

import java.util.Collection;
import java.util.HashSet;

import com.dynatrace.easytravel.launcher.agent.Technology;


class TechnologyStopper extends AbstractStopper {

	TechnologyStopper(Collection<? extends Procedure> procedures, Technology technology) {
		super(filter(procedures, technology));
	}

	private static Collection<Procedure> filter(Collection<? extends Procedure> procedures, Technology technology) {
		HashSet<Procedure> filtered = new HashSet<Procedure>();
		for (Procedure procedure : procedures) {
			if (procedure.getTechnology() == technology) {
				filtered.add(procedure);
			}
		}
		return filtered;
	}

	@Override
	public boolean notStoppable(final Procedure proc) {
		return !proc.isStoppable(); // ignore whether enabled or not
	}

}
