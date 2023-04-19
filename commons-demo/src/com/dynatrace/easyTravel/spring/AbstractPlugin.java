package com.dynatrace.easytravel.spring;

import java.util.Collections;
import java.util.logging.Logger;

import org.apache.commons.lang3.ArrayUtils;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.util.TextUtils;

/**
 * Base implementation of common properties for a plugin.
 *
 * @author philipp.grasboeck
 */
public class AbstractPlugin implements Plugin {

	private static final Logger log = Logger.getLogger(AbstractPlugin.class.getName());

	private String name = getClass().getSimpleName();
	protected String groupName;
    private String compatibility = InstallationType.Both.name();
	private String description;
	private String[] hosts = null;

	// if no dependencies are given
	private Iterable<PluginDependency> pluginDependencies;
	private boolean enabled;

	private String[] dependencies;

	public AbstractPlugin() {
	}

	public AbstractPlugin(String name, String groupName, String compatibility, String description) {
		this.name = name;
		this.groupName = groupName;
		if(compatibility != null) {
			this.compatibility = compatibility;
		} else {
			this.compatibility = InstallationType.Both.name();
		}
        this.description = description;
	}

    public AbstractPlugin(String name, String groupName, String description) {
        this.name = name;
        this.groupName = groupName;
        this.description = description;
    }

	@Override
	public final String getName() {
		return name;
	}

    @Override
	public String getCompatibility() {
        return compatibility;
    }

    @Override
	public void setCompatibility(String compatibility) {
        this.compatibility = compatibility;
    }

    public final void setName(String name) {
		this.name = name;
	}

	@Override
	public final String getGroupName() {
		return groupName;
	}

	public final void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	@Override
	public final String getDescription() {
		return description;
	}

	public final void setDescription(String description) {
		this.description = description;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public final void setDependencies(String[] dependencies) {		// NOPMD - false positive, we do copy the array if non-empty
		if (ArrayUtils.isEmpty(dependencies)) {
			this.pluginDependencies = Collections.emptySet();
			this.dependencies = new String[0];
			return;
		}
		this.dependencies = ArrayUtils.clone(dependencies);
		this.pluginDependencies = PluginDependency.forNames(dependencies);
	}

	@Override
	public final String[] getDependencies() {
		return dependencies;
	}

	@Override
	public Iterable<PluginDependency> getPluginDependencies() {
		return pluginDependencies;
	}

	@Override
	public final void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [name=" + name + ", groupName=" + groupName + ", compatibility=" + compatibility +"]";
	}


	@Override
	public boolean isActivatable() {
		if (pluginDependencies == null) {
			return true;
		}

		for (PluginDependency dependency : pluginDependencies) {
			if (!dependency.isAvailable()) {
				log.warning(TextUtils.merge("The plugin ''{0}'' cannot be used, because its dependency ''{1}'' is not available",
						name, dependency));
				return false;
			}
		}
		return true;

	}

	@Override
	public final String[] getHosts() {
		return hosts;
	}

	@Override
	public final void setHosts(String[] hosts) {
		this.hosts = ArrayUtils.clone(hosts);
	}

	protected final boolean isEnabledForCurrentHost() {
		if (hosts == null) {
			return true;
		}
		String officialHost = EasyTravelConfig.read().officialHost;
		if (officialHost == null) {
			log.warning("hosts for plugin but no official host set in properties");
			return false;
		}
		for (String host : hosts) {
			if (host == null) {
				continue;
			}
			if (officialHost.equals(host)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public final boolean isEnabledFor(String host) {
		if (host == null) {
			return false;
		}
		if (hosts == null) {
			return true;
		}
		for (String curHost : hosts) {
			if (curHost == null) {
				continue;
			}
			if (curHost.equals(host)) {
				return true;
			}
		}
		return false;
	}

}
