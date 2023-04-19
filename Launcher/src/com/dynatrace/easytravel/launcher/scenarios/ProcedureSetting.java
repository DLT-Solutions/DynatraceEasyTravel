package com.dynatrace.easytravel.launcher.scenarios;



/**
 * Used to specify custom settings of {@link ProcedureMapping}s.
 *
 * @author martin.wurzinger
 */
public interface ProcedureSetting extends Setting {

    /**
     *
     * @return the duration of this procedure to stay enabled (in seconds)
     * @author richard.uttenthaler
     */
    int getStayOffDuration();

    /**
     *
     * @return the duration of this procedure to stay disabled (in seconds)
     * @author richard.uttenthaler
     */
    int getStayOnDuration();
}
