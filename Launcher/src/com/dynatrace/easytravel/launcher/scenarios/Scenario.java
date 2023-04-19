package com.dynatrace.easytravel.launcher.scenarios;

import java.util.List;
import java.util.Map;

import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.launcher.config.Persistable;


public interface Scenario extends Persistable, Copyable<Scenario> {

	public static final String TYPE_SCENARIO_CONFIG = "scenario_config";
	public static final String TYPE_SCENARIO_PROCEDURE_CONFIG = "procedure_config";

    String getTitle();

    void setGroup(String group);

    String getGroup();

    /**
     * @return <tt>Classic</tt> if this scenario is supposed to be available in dynaTrace Classic Mode only,
     * 			<tt>APM</tt> if this scenario is supposed to be available in APM Mode only,
     * 			<tt>Both</tt> if this scenario is supposed to be available in dynaTrace Classic Mode and APM Mode<br />.
     *
     * @author cwat-rpilz
     */
    InstallationType getCompatibility();

    String getDescription();

    List<ProcedureMapping> getProcedureMappings(InstallationType compatibility);

    boolean isEnabled();

    ProcedureMapping addProcedureMapping(ProcedureMapping create);

    void setEnabled(boolean b);

	/**
	 * @return the scenario specific configuration
	 * 			if the scenario has none, it returns a empty map,
	 * 			but never <code>null</code>
	 * @author stefan.moschinski
	 */
	Map<String,String> getCustomSettings();


	/**
	 *
	 * @param setting {@link Setting} that should be added to the scenario
	 * @author stefan.moschinski
	 */
	void addSetting(Setting setting);

}
