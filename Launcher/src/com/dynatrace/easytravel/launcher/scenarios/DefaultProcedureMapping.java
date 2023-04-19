package com.dynatrace.easytravel.launcher.scenarios;

import java.util.*;

import org.apache.commons.configuration.tree.ConfigurationNode;

import com.dynatrace.easytravel.config.ConfigurationException;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.launcher.config.ConfigurationReader;
import com.dynatrace.easytravel.launcher.config.NodeFactory;
import com.dynatrace.easytravel.launcher.misc.Constants.ConfigurationXml;


public class DefaultProcedureMapping implements ProcedureMapping {

    private static final String PROCEDURE_SETTING_NODE = "setting";
    private final Collection<ProcedureSetting> settings = new ArrayList<ProcedureSetting>();

    private String mappingId;
	private Collection<Setting> customSettings;
	private String host;
	private String apmTenantUUID;
    private InstallationType compatibility = InstallationType.Both;

    DefaultProcedureMapping() {
    	this(null);
    }

    public DefaultProcedureMapping(String mappingId) {
    	this(mappingId, InstallationType.Both);
    }

    public DefaultProcedureMapping(String mappingId, InstallationType compatibility) {
        this.mappingId = mappingId;
        this.host = null;
        this.apmTenantUUID = null;
        this.compatibility = compatibility;
    }

    @Override
    public String getId() {
        return mappingId;
    }

	@Override
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	@Override
	public String getAPMTenantUUID() {
		return apmTenantUUID;
	}

	public void setAPMTenantUUID(String apmTenantUUID) {
		this.apmTenantUUID = apmTenantUUID;
	}

    @Override
    public void read(ConfigurationNode node, ConfigurationReader reader) throws ConfigurationException {
        mappingId = reader.readStringAttribute(node, ConfigurationXml.ATTRIBUTE_ID);
        host = reader.readOptionalStringAttribute(node, ConfigurationXml.ATTRIBUTE_HOST);
        apmTenantUUID = reader.readOptionalStringAttribute(node, ConfigurationXml.ATTRIBUTE_APM_TENANT);
        compatibility = InstallationType.fromString(reader.readOptionalStringAttribute(node, ConfigurationXml.ATTRIBUTE_COMPATIBILITY));

        List<ConfigurationNode> settingNodes = reader.getChildren(node, PROCEDURE_SETTING_NODE);
        for (ConfigurationNode settingNode : settingNodes) {
            ProcedureSetting setting = new DefaultProcedureSetting();
            setting.read(settingNode, reader);
            addToCustomSettingsIfRequired(setting);
            this.settings.add(setting);
        }
    }

    private void addToCustomSettingsIfRequired(ProcedureSetting setting) {
		if (Scenario.TYPE_SCENARIO_PROCEDURE_CONFIG.equals(setting.getType())) {
			customSettings = getCustomSettings();
			customSettings.add(setting);
		}
	}

    @Override
    public Collection<Setting> getCustomSettings() {
    	return customSettings == null ? new HashSet<Setting>(2, 1F) : customSettings;
    }

    @Override
	public boolean hasCustomSettings() {
    	return !getCustomSettings().isEmpty();
    }

	@Override
    public void write(ConfigurationNode node, NodeFactory factory) {
        node.addAttribute(factory.createNode(ConfigurationXml.ATTRIBUTE_ID, mappingId));
        if (host != null) {
        	node.addAttribute(factory.createNode(ConfigurationXml.ATTRIBUTE_HOST, host));
        }
        if (apmTenantUUID != null) {
        	node.addAttribute(factory.createNode(ConfigurationXml.ATTRIBUTE_APM_TENANT, apmTenantUUID));
        }
        // only write non-Both settings!
        if (compatibility != null && !InstallationType.Both.equals(compatibility)) {
        	node.addAttribute(factory.createNode(ConfigurationXml.ATTRIBUTE_COMPATIBILITY, compatibility.toString()));
        }

        for (ProcedureSetting setting : this.settings) {
            ConfigurationNode settingNode = factory.createNode(PROCEDURE_SETTING_NODE);
            setting.write(settingNode, factory);
            node.addChild(settingNode);
        }
    }

    @Override
    public Collection<ProcedureSetting> getSettings() {
        return Collections.unmodifiableCollection(settings);
    }

    @Override
    public Collection<ProcedureSetting> getSettings(String type) {
        List<ProcedureSetting> result = new ArrayList<ProcedureSetting>();

        for (ProcedureSetting setting : settings) {
            if (areTypesEqual(type, setting.getType())) {
                result.add(setting);
            }
        }

        return Collections.unmodifiableCollection(result);
    }

    @Override
    public DefaultProcedureMapping copy() {
        DefaultProcedureMapping copy = new DefaultProcedureMapping(mappingId);

        for (ProcedureSetting setting : settings) {
            copy.settings.add(setting.copy());
        }
        copy.customSettings = copy.getCustomSettings();
        for (Setting setting : getCustomSettings()) {
        	copy.customSettings.add(setting.copy());
        }

        return copy;
    }

    @Override
	public ProcedureMapping addSetting(ProcedureSetting setting) {
        for (ProcedureSetting set : settings) {
			if (equalType(setting, set) && set.getName().equals(setting.getName())) {
				removeSetting(set); // if the property has been already set
				break;
			}
		}
    	this.settings.add(setting);
    	addToCustomSettingsIfRequired(setting);
        return this;
    }

	private boolean equalType(ProcedureSetting setting, ProcedureSetting set) {
		if (set.getType() == null && setting.getType() == null) {
			return true;
		}
		if (set.getType() == null || setting.getType() == null) { // else possible NPE
			return false;
		}
		return set.getType().equals(setting.getType());
	}

    @Override
	public ProcedureMapping removeSetting(ProcedureSetting setting) {
        this.settings.remove(setting);
        return this;
    }

    /**
     * Get the {@link ProcedureSetting} that matches the specified type and name.
     *
     * @param type
     * @param name
     * @return the matching procedure setting or <code>null</code> if no matching setting was found
     * @author martin.wurzinger
     */
    public ProcedureSetting getSetting(String type, String name) {
        if (name == null) {
            throw new IllegalArgumentException("Invalid setting name argument.");
        }

        for (ProcedureSetting setting : settings) {
            if (areTypesEqual(type, setting.getType()) && name.equalsIgnoreCase(setting.getName())) {
                return setting;
            }
        }

        // no matching setting found
        return null;
    }

    private boolean areTypesEqual(String typeA, String typeB) {
        if (typeA == null) {
            return typeB == null;
        } else {
            return typeA.equalsIgnoreCase(typeB);
        }
    }

    @Override
    public String getSettingValue(String name) {
        ProcedureSetting setting = getSetting(null, name);
        if (setting == null) {
            return null;
        }

        return setting.getValue();
    }

    @Override
    public String getSettingValue(String type, String name) {
        ProcedureSetting setting = getSetting(type, name);
        if (setting == null) {
            return null;
        }

        return setting.getValue();
    }

	@Override
	public Collection<ProcedureSetting> getScheduledOnOffSettings() {
		List<ProcedureSetting> result = new ArrayList<ProcedureSetting>();

		for (ProcedureSetting setting : settings) {
			if (setting.getStayOffDuration() > 0) {
				result.add(setting);
			}
		}
		return Collections.unmodifiableCollection(result);
	}

	/** {@inheritDoc} */
    @Override
    public InstallationType getCompatibility() {
    	return compatibility;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DefaultProcedureMapping other = (DefaultProcedureMapping) obj;
        return Objects.equals(this.mappingId, other.mappingId) &&
                            Objects.equals(this.host, other.host) &&
                            Objects.equals(this.compatibility, other.compatibility);
    }
}
