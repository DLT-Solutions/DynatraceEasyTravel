package com.dynatrace.easytravel.launcher.scenarios.builder;

import static com.dynatrace.easytravel.launcher.misc.Constants.Misc.AUTOMATIC_PLUGIN_ON_OFF_DISABLED;

import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.procedures.AntProcedure;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureSetting;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureSetting;
import com.dynatrace.easytravel.launcher.scenarios.Scenario;


public abstract class SettingBuilder {

	private String name;
	protected String value;
	private String type;
	private String settingOn;
	private String settingOff;
	private int stayOnDuration = AUTOMATIC_PLUGIN_ON_OFF_DISABLED;
	private int stayOffDuration = AUTOMATIC_PLUGIN_ON_OFF_DISABLED;


	private SettingBuilder(String name, String type) {
		this.name = name;
		this.type = type;
	}

	private SettingBuilder(String type, String settingOn, String settingOff) {
		this.type = type;
		this.settingOn = settingOn;
		this.settingOff = settingOff;
	}

	private SettingBuilder() {}

	public static SettingBuilder plugin(String name) {
		return new PluginBuilder(name);
	}

	public static SettingBuilder procedure() {
		return new ProcedureSettingBuilder();
	}

	public static SettingBuilder property(String name) {
		return new ProcedureSettingBuilder(name);
	}

	public static SettingBuilder antProperty(String name) {
		return new AntSettingBuilder(name);
	}

	public static SettingBuilder config(String name) {
		return new ScenarioProcedureSettingBuilder(name);
	}

	public static SettingBuilder scenarioConfig(String name) {
		return new ScenarioSettingBuilder(name);
	}

	public SettingBuilder value(String value) {
		this.value = value;
		return this;
	}

	public SettingBuilder type(String type) {
		this.type = type;
		return this;
	}

	public ProcedureSetting create() {
		return new DefaultProcedureSetting(getType(), name, value, stayOnDuration, stayOffDuration);
	}

	protected String getType() {
		return type;
	}

	public SettingBuilder enable() {
		value = settingOn;
		return this;
	}

	public SettingBuilder disable() {
		value = settingOff;
		return this;
	}

	public SettingBuilder setStayOnDuration(int duration) {
		this.stayOnDuration = duration;
		return this;
	}

	public SettingBuilder setStayOffDuration(int duration) {
		this.stayOffDuration = duration;
		return this;
	}

	protected void setName(String name) {
		this.name = name;
	}

	private static class PluginBuilder extends SettingBuilder {

		private PluginBuilder(String name) {
			super(Constants.Misc.SETTING_TYPE_PLUGIN, Constants.Misc.SETTING_VALUE_ON, Constants.Misc.SETTING_VALUE_OFF);
			setName(name);
		}
	}

	private static class ProcedureSettingBuilder extends SettingBuilder {

		private ProcedureSettingBuilder() {
			super(null, Constants.Misc.SETTING_VALUE_ON, Constants.Misc.SETTING_VALUE_OFF);
		}

		public ProcedureSettingBuilder(String name) {
			setName(name);
		}

		@Override
		public SettingBuilder enable() {
			setName(Constants.Misc.SETTING_ENABLED);
			return super.enable();
		}

		@Override
		public SettingBuilder disable() {
			setName(Constants.Misc.SETTING_ENABLED);
			return super.disable();
		}
	}

	private static class AntSettingBuilder extends SettingBuilder {

		public AntSettingBuilder(String name) {
			super(name, AntProcedure.PROPERTY);
		}

	}

	private static class ScenarioProcedureSettingBuilder extends SettingBuilder {

		public ScenarioProcedureSettingBuilder(String name) {
			super(name, Scenario.TYPE_SCENARIO_PROCEDURE_CONFIG);
		}

	}

	private static class ScenarioSettingBuilder extends SettingBuilder {
		public ScenarioSettingBuilder(String name) {
			super(name, Scenario.TYPE_SCENARIO_CONFIG);
		}

	}

}