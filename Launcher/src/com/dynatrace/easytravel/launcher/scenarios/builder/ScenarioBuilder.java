package com.dynatrace.easytravel.launcher.scenarios.builder;

import static java.lang.String.format;

import java.util.logging.Logger;

import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.launcher.scenarios.DefaultScenario;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.Scenario;
import com.dynatrace.easytravel.launcher.scenarios.ScenarioGroup;


public class ScenarioBuilder {

	private static final Logger log = Logger.getLogger(ScenarioBuilder.class.getName());

	private Scenario scenario;

	public ScenarioBuilder(String title, String description, InstallationType compatibility) {
		this.scenario = new DefaultScenario(title, description, true, compatibility);
	}

	public static ScenarioBuilder scenario(String title, String description) {
		return scenario(title, description, InstallationType.Both);
	}

	public static ScenarioBuilder scenario(String title, String description, InstallationType compatibility) {
		return new ScenarioBuilder(title, description, compatibility);
	}

	public ScenarioBuilder add(ProcedureMapping... procedures) {
		for (ProcedureMapping procedure : procedures) {
			scenario.addProcedureMapping(procedure);
		}
		return this;
	}

	public ScenarioBuilder add(ProcedureBuilder...procedures) {
		for (ProcedureBuilder procedure : procedures) {
			scenario.addProcedureMapping(procedure.create());
		}
		return this;
	}

	public <T extends SettingBuilder> ScenarioBuilder set(T... settings) {
		if (/*not possible with varargs: settings == null || */settings.length == 0) {
			return this;
		}
		for (T setting : settings) {
			scenario.addSetting(setting.create());
		}
		return this;
	}

	public ScenarioBuilder addIf(boolean condition, ProcedureMapping... procedures) {
		if (condition) {
			add(procedures);
		}
		return this;
	}

	public ScenarioBuilder addIf(boolean condition, ProcedureBuilder... procedures) {
		if (condition) {
			add(procedures);
		}
		return this;
	}

	@SuppressWarnings("unchecked")
	public <T extends Scenario> T create() {
		return (T) scenario;
	}

	public void addToGroup(ScenarioGroup productionGroup) {
		productionGroup.addScenario(scenario);
	}

	public ScenarioBuilder disable() {
		scenario.setEnabled(false);
		return this;
	}

	/**
	 * By default, each eT scenario is enabled. However using this method you can conditionally enable a scenario.
	 * If the passed {@code enableCondition} is <code>true</code>, the scenario will be selectable in the UI. Otherwise it is
	 * grayed out.
	 *
	 * @param enableCondition if the condition is <code>true</code> the scenario is enabled
	 * @param reasonForDisabledScenario reason that should be logged, if the {@code enableCondition} is <code>false</code>
	 * @return the called instance ({@code this}) to implement the builder pattern
	 * @author stefan.moschinski
	 */
	public ScenarioBuilder enableIf(boolean enableCondition, String reasonForDisabledScenario) {
		scenario.setEnabled(enableCondition);
		if (!enableCondition) {
			log.warning(format("Disabling scenario '%s' because of: '%s'", scenario.getTitle(), reasonForDisabledScenario));
		}
		return this;
	}

	public ScenarioBuilder enable() {
		scenario.setEnabled(true);
		return this;
	}
}
