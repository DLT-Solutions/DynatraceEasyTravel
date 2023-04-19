package com.dynatrace.easytravel.launcher.scenarios;

import java.util.Collection;

import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.launcher.config.Persistable;
import com.dynatrace.easytravel.launcher.plugin.CyclicPluginManager;


public interface ProcedureMapping extends Persistable, Copyable<ProcedureMapping> {

    /**
     * <p>
     * Get the ID of the procedure mapping. The ID represents the type of a procedure.
     * </p>
     * <p>
     * <em>Note: As a convention we are comparing IDs in a case-insensitive way.</em>
     * </p>
     *
     * @return the ID of this procedure mapping
     * @author martin.wurzinger
     */
    String getId();

    /**
     * @return the configured settings for this procedure which must not be <code>null</code>
     * @author martin.wurzinger
     */
    Collection<ProcedureSetting> getSettings();

    /**
     * @param type the setting type to be interested in
     * @return the settings of this type for this procedure which must not be <code>null</code>
     * @author martin.wurzinger
     */
    Collection<ProcedureSetting> getSettings(String type);

    /**
     *
     * @return the settings of all procedures that are defined to get enabled and disabled by the {@link CyclicPluginManager}
     * @author richard.uttenthaler
     */
    Collection<ProcedureSetting> getScheduledOnOffSettings();

    /**
     * Get the value of the setting that matches the specified name and has has no type.
     *
     * @param name the name of the setting
     * @return the setting value or <code>null</code> if the setting is not defined
     * @author martin.wurzinger
     */
    String getSettingValue(String name);

    /**
     * Get the value of the setting that matches the specified name and type.
     *
     * @param name the type of the setting
     * @param name the name of the setting
     * @return the setting value or <code>null</code> if the setting is not defined
     * @author martin.wurzinger
     */
    String getSettingValue(String type, String name);

    /**
     * Adds a setting to the procedure
     * @param setting
     * @return
     * @author stefan.moschinski
     */
    ProcedureMapping addSetting(ProcedureSetting setting);

    /**
     * Removes a setting from the procedure
     * @param setting
     * @return
     * @author stefan.moschinski
     */
    ProcedureMapping removeSetting(ProcedureSetting setting);

    /**
     *
     * @return <code>true</code> if the procedure has custom (scenario specific) settings
     * @author stefan.moschinski
     */
    boolean hasCustomSettings();

    /**
     *
     * @return collection of the custom settings or empty collection if the mapping has no custom settings,
     * but never <code>null</code>
     * @author stefan.moschinski
     */
    Collection<Setting> getCustomSettings();

    /**
     * @return Returns a hostname if this procedure should run on a specific host or null if not specified.
     */
    String getHost();

    /**
     * @return A specific Tenant UUID which should be used for this procedure or null if not specified.
     */
    String getAPMTenantUUID();

    /**
     * @return <tt>Classic</tt> if this scenario is supposed to be available in dynaTrace Classic Mode only,
     * 			<tt>APM</tt> if this scenario is supposed to be available in APM Mode only,
     * 			<tt>Both</tt> if this scenario is supposed to be available in dynaTrace Classic Mode and APM Mode<br />.
     *
     * @author cwat-rpilz
     */
    InstallationType getCompatibility();
}
