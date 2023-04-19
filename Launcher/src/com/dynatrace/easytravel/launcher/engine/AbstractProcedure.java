/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: AbstractProcedure.java
 * @date: 18.01.2011
 * @author: martin.wurzinger
 */
package com.dynatrace.easytravel.launcher.engine;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.dynatrace.easytravel.config.ConfigurationException;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants.UrlType;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.launcher.procedures.utils.CentralTechnologyActivator;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureSetting;
import com.dynatrace.easytravel.launcher.scenarios.Setting;
import com.dynatrace.easytravel.launcher.sync.Predicate;
import com.dynatrace.easytravel.launcher.sync.PredicateMatcher;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;


/**
 * Top level class to handle similarities of common {@link Procedure}s.
 *
 * @author martin.wurzinger
 */
public abstract class AbstractProcedure implements Procedure {
    protected String name = MessageConstants.UNKNOWN;
    private ProcedureMapping mapping;

    /**
     * @param mapping the configuration this procedure is mapped to
     * @throws IllegalArgumentException if a argument is <code>null</code>
     * @author martin.wurzinger
     */
    protected AbstractProcedure(ProcedureMapping mapping) throws IllegalArgumentException {
        if (mapping == null) {
            throw new IllegalArgumentException("Mapping is required.");
        }

        this.name = ProcedureFactory.getNameOfProcedure(mapping);
        this.mapping = mapping.copy();
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEnabled() {
    	// allow to disable procedures in the scenario, not set is "ON"!
    	return !Constants.Misc.SETTING_VALUE_OFF.equalsIgnoreCase(mapping.getSettingValue(Constants.Misc.SETTING_ENABLED))
    			&& CentralTechnologyActivator.getIntance().isAllowed(getTechnology());
    }

    @Override
    public void notifyProcedureStateChanged(StatefulProcedure subject, State oldState, State newState) {
        // default implementation does nothing
    }

    @Override
    public void transfer(ProcedureMapping mapping, State state) {
        this.mapping = mapping;
    }

    private static final Predicate<Procedure> IS_RUNNING_PREDICATE = new Predicate<Procedure>() {

        @Override
        public boolean eval(Procedure procedure) {
            return procedure.isRunning();
        }

		@Override
		public boolean shouldStop() {
			return false;
		}
    };

    /**
     * Wait until this procedure is running.
     *
     * @return <code>true</code> if the procedure is finally running or <code>false</code> otherwise
     * @author martin.wurzinger
     */
    public boolean waitUntilRunning() {
    	final EasyTravelConfig config = EasyTravelConfig.read();
		return waitUntilRunning(config.syncProcessTimeoutMs, config.processRunningCheckInterval);
    }

    /**
     * Wait until this procedure is running.
     *
     * @param timeoutMs the maximum time in milliseconds to wait
     * @param intervalMs the interval in milliseconds to check if procedure is running
     * @return <code>true</code> if the procedure is finally running or <code>false</code> otherwise
     * @author martin.wurzinger
     */
    public boolean waitUntilRunning(int timeoutMs, int intervalMs) {
        return new PredicateMatcher<Procedure>(this, timeoutMs, intervalMs).waitForMatch(IS_RUNNING_PREDICATE);
    }

    /**
     * Wait until this procedure was stopped.
     *
     * @return <code>true</code> if the procedure is still running or <code>false</code> otherwise
     * @author martin.wurzinger
     */
    public boolean waitUntilNotRunning() {
    	final EasyTravelConfig config = EasyTravelConfig.read();
		return waitUntilNotRunning(config.shutdownTimeoutMs, config.processRunningCheckInterval);
    }

    /**
     * Wait until this procedure was stopped.
     *
     * @param timeoutMs the maximum in milliseconds time to wait
     * @param intervalMs the interval in milliseconds to check if procedure is running
     * @return <code>true</code> if the procedure is still running or <code>false</code> otherwise
     * @author martin.wurzinger
     */
    public boolean waitUntilNotRunning(int timeoutMs, int intervalMs) {
        return new PredicateMatcher<Procedure>(this, timeoutMs, intervalMs).waitForNotMatch(IS_RUNNING_PREDICATE);
    }

    @Override
    public ProcedureMapping getMapping() {
        return mapping;
    }

    @Override
    public boolean isSynchronous() {
        // by default Procedures are asynchronous, override this for synchronous ones
        return false;
    }

    @Override
    public boolean isTransferableTo(ProcedureMapping otherMapping) {
        if (!mapping.equals(otherMapping)) {
            return false;
        }

        for (ProcedureSetting procedureSetting : mapping.getSettings()) {
            if (!isTransferable(procedureSetting, otherMapping.getSettings())) {
                return false;
            }
        }

        for (ProcedureSetting procedureSetting : otherMapping.getSettings()) {
            if (!isTransferable(procedureSetting, mapping.getSettings())) {
                return false;
            }
        }

        for (Setting customSetting : mapping.getCustomSettings()) {
            if (!isTransferable(customSetting, otherMapping.getCustomSettings())) {
                return false;
            }
        }

        for (Setting customSetting : otherMapping.getCustomSettings()) {
            if (!isTransferable(customSetting, mapping.getCustomSettings())) {
                return false;
            }
        }

        // if all checks where successful the procedure is considered as transferable
        return true;
    }

    /**
     * <p>
     * Check if the {@link ProcedureSetting} supports a procedure transfer.
     * </p>
     *
     * @param setting the setting to be checked if it's transferable
     * @return <code>false</code> in the default implementation
     * @author martin.wurzinger
     * @author cwpl-rorzecho
     */
    protected boolean isTransferable(ProcedureSetting setting) {
        return isTransferable(setting, mapping.getSettings());
    }

    protected boolean isTransferable(final Setting setting, final Collection<Setting> settings) {
        return FluentIterable.from(settings)
                .anyMatch(new com.google.common.base.Predicate<Setting>() {
                    @Override
                    public boolean apply(Setting input) {
                        return input.equals(setting) &&
                                input.getValue().equals(setting.getValue());
                    }
                    
                    //@Override
                    public boolean test(Setting input) {
                    	return apply(input);
                    }
                });
    }

    protected boolean isTransferable(final ProcedureSetting setting, final Collection<ProcedureSetting> settings) {
        return FluentIterable.from(settings)
                .anyMatch(new com.google.common.base.Predicate<ProcedureSetting>() {
                    @Override
                    public boolean apply(ProcedureSetting input) {
                        return input.equals(setting) &&
                                input.getValue().equals(setting.getValue()) &&
                                input.getStayOffDuration() == setting.getStayOffDuration() &&
                                input.getStayOnDuration() == setting.getStayOnDuration();
                    }
                    
                    //@Override
                    public boolean test(ProcedureSetting input) {
                        return apply(input);
                    }
                });
    }

    /**
     * Default implementation which returns empty.
     */
	@Override
	public String getURI() {
		return null;
	}

    @Override
    public List<String> getDependingProcedureIDs() {
    	return Collections.emptyList();
    }

	@Override
	public String getURI(UrlType urlType) {
		return null;
	}

    @Override
    public boolean isInstrumentationSupported() {
        return getTechnology() != null;
    }

    protected boolean agentFound(DtAgentConfig dtAgentConfig) {
        if(!isInstrumentationSupported()) {
			return false;
		}
		Preconditions.checkNotNull(dtAgentConfig, "The passed %s for procedure '%s' is null",
				DtAgentConfig.class.getSimpleName(), this.getName());
        String path;
        try {
            path = dtAgentConfig.getAgentPath(getTechnology());
        } catch (ConfigurationException e) {
            return false;
        }
        if(path == null) {
			return false;
		}
        return new File(path).exists();
    }

	@Override
	public File getPropertyFile() {
		return null;
	}

	@Override
	public String getURIDNS() {
		return getURI();
	}
	
	@Override
	public String getURIDNS(UrlType urlType) {
		return getURI(urlType);
	}
	
	@Override
	public int getTimeout(){
		return EasyTravelConfig.read().syncProcessTimeoutMs;
	}

}
