package com.dynatrace.easytravel.launcher.scenarios;

import java.util.*;

import org.apache.commons.configuration.tree.ConfigurationNode;

import com.dynatrace.easytravel.config.ConfigurationException;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.launcher.config.ConfigurationReader;
import com.dynatrace.easytravel.launcher.config.NodeFactory;
import com.dynatrace.easytravel.launcher.misc.Constants.ConfigurationXml;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;


public class DefaultScenario implements Scenario {

    private static final String NODE_PROCEDURE = "procedure";
    private static final String NODE_DESCRIPTION = "description";
    private static final String SCENARIO_SETTING = "setting";

    private final List<ProcedureMapping> procedureMappings = new ArrayList<ProcedureMapping>();

    private String title = null;
    private String description = null;
    private InstallationType compatibility = InstallationType.Both;
    private String group = null;
    private boolean isEnabled = true;
	private final Collection<Setting> customSettings = new ArrayList<Setting>();
	private final Collection<Setting> scenarioSettings = new ArrayList<Setting>();

    public DefaultScenario() {}

    public DefaultScenario(String title, String description) {
    	this(title, description, true, InstallationType.Both);
    }

    public DefaultScenario(String title, String description, boolean isEnabled, InstallationType compatibility) {
        this.title = title;
        this.description = description;
        this.isEnabled = isEnabled;
        this.compatibility = compatibility;
    }

    /** {@inheritDoc} */
    @Override
    public String getTitle() {
        return title;
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return description;
    }

    /** {@inheritDoc} */
    @Override
    public InstallationType getCompatibility() {
    	return compatibility;
    }

    /** {@inheritDoc} */
    @Override
    public String getGroup() {
        return group;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    /** {@inheritDoc} */
    @Override
    public List<ProcedureMapping> getProcedureMappings(final InstallationType compatibility) {
        Predicate<ProcedureMapping> findMatching = new Predicate<ProcedureMapping>() {
            @Override
            public boolean apply(ProcedureMapping plugin) {
            	// we should prevent null earlier, but currently there are cases where this is theoretically possible (e.g. BatchTest)
                return plugin == null || plugin.getCompatibility().matches(compatibility);
            }
            
            //@Override
            public boolean test(ProcedureMapping plugin) {
                return apply(plugin);
            }
        };

        return Collections.unmodifiableList(new ArrayList<ProcedureMapping>(Collections2.filter(procedureMappings, findMatching)));
    }

    @Override
	public ProcedureMapping addProcedureMapping(ProcedureMapping procedureMapping) {
        this.procedureMappings.add(procedureMapping);
        return procedureMapping;
    }

    public void addProcedureMappings(List<ProcedureMapping> procedureMappings) {
        this.procedureMappings.addAll(procedureMappings);
    }

    public void addSettings(Collection<Setting> settings) {
    	this.scenarioSettings.addAll(settings);
    }

    @Override
    public void read(ConfigurationNode scenario, ConfigurationReader reader) throws ConfigurationException {
        title = reader.readStringAttribute(scenario, ConfigurationXml.ATTRIBUTE_TITLE);
        description = reader.readTextChild(scenario, DefaultScenario.NODE_DESCRIPTION);
        isEnabled = reader.readBooleanAttribute(scenario, ConfigurationXml.ATTRIBUTE_ENABLED);
        compatibility = InstallationType.fromString(reader.readOptionalStringAttribute(scenario, ConfigurationXml.ATTRIBUTE_COMPATIBILITY));

        addProcedureMappings(readProcedureMappings(scenario, reader));
        addSettings(readSettings(scenario, reader));
    }

    private List<ProcedureMapping> readProcedureMappings(ConfigurationNode scenario, ConfigurationReader reader) throws ConfigurationException {
        List<ProcedureMapping> mappings = new ArrayList<ProcedureMapping>();

        for (ConfigurationNode procedure : reader.getChildren(scenario, DefaultScenario.NODE_PROCEDURE)) {
            ProcedureMapping procedureMapping = new DefaultProcedureMapping();
            procedureMapping.read(procedure, reader);
        	if (procedureMapping.hasCustomSettings()) {
        		adaptConfig(procedureMapping.getCustomSettings());
        	}
            mappings.add(procedureMapping); // NOPMD
        }

        return mappings;
    }

    private Collection<Setting> readSettings(ConfigurationNode scenario, ConfigurationReader reader) throws ConfigurationException {
    	List<Setting> settings = new ArrayList<Setting>();
    	for (ConfigurationNode settingNode : reader.getChildren(scenario, SCENARIO_SETTING)) {
    		ProcedureSetting setting = new DefaultProcedureSetting();
    		setting.read(settingNode, reader);
    		settings.add(setting);
    	}
    	return settings;
    }

    private void adaptConfig(Collection<Setting> overriddenProperties) {
		for (Setting newSetting : overriddenProperties) {
			customSettings.add(newSetting.copy());
		}
	}

	@Override
    public void write(ConfigurationNode scenarioNode, NodeFactory factory) {
        scenarioNode.addAttribute(factory.createNode(ConfigurationXml.ATTRIBUTE_TITLE, title));
        scenarioNode.addAttribute(factory.createNode(ConfigurationXml.ATTRIBUTE_ENABLED, isEnabled));
        if (compatibility != null) {
        	scenarioNode.addAttribute(factory.createNode(ConfigurationXml.ATTRIBUTE_COMPATIBILITY, compatibility.toString()));
        }
        scenarioNode.addChild(factory.createNode(DefaultScenario.NODE_DESCRIPTION, description));

        writeProceduresMappings(scenarioNode, factory);
        writeScenarioSettings(scenarioNode, factory);
    }

    private void writeProceduresMappings(ConfigurationNode scenarioNode, NodeFactory factory) {
        for (ProcedureMapping procedureMapping : procedureMappings) {
            ConfigurationNode procedureNode = factory.createNode(DefaultScenario.NODE_PROCEDURE);
            procedureMapping.write(procedureNode, factory);
            scenarioNode.addChild(procedureNode);
        }
    }

    private void writeScenarioSettings(ConfigurationNode scenarioNode, NodeFactory factory) {
    	for (Setting setting : this.scenarioSettings) {
    		ConfigurationNode settingNode = factory.createNode(SCENARIO_SETTING);
    		setting.write(settingNode, factory);
    		scenarioNode.addChild(settingNode);
    	}
    }

    @Override
    public DefaultScenario copy() {
        DefaultScenario copy = new DefaultScenario(title, description, isEnabled, compatibility);

        for (ProcedureMapping procedure : procedureMappings) {
            copy.procedureMappings.add(procedure.copy());
        }
        for (Setting setting : customSettings) {
        	copy.customSettings.add(setting.copy());
        }
        for (Setting setting : scenarioSettings) {
        	copy.scenarioSettings.add(setting.copy());
        }

        // remove group assignment
        copy.group = null;

        return copy;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
	public void setGroup(String group) {
        this.group = group;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
	public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		DefaultScenario other = (DefaultScenario) obj;

		if (title == null) {
			if (other.title != null) {
				return false;
			}
		} else if (!title.equals(other.title)) {
			return false;
		}

		if (group == null) {
			if (other.group != null) {
				return false;
			}
		} else if (!group.equals(other.group)) {
			return false;
		}

		return true;
	}

	@Override
	public Map<String, String> getCustomSettings() {
		if (customSettings.isEmpty() && scenarioSettings.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<String, String> settings = new HashMap<String, String>(customSettings.size(), 1F);
		// add the procedure scenario specific values
		for (Setting setting : customSettings) {
			settings.put(setting.getName(), setting.getValue());
		}

		// add the global scenario specific values
		for (Setting setting : scenarioSettings) {
			settings.put(setting.getName(), setting.getValue());
		}

		return Collections.unmodifiableMap(settings);
	}

	@Override
	public void addSetting(Setting setting) {
		scenarioSettings.add(setting);
	}
}

