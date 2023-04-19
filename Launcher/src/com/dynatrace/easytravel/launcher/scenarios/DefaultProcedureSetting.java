package com.dynatrace.easytravel.launcher.scenarios;

import static com.dynatrace.easytravel.launcher.misc.Constants.Misc.AUTOMATIC_PLUGIN_ON_OFF_DISABLED;

import java.util.StringTokenizer;

import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.ConfigurationException;
import com.dynatrace.easytravel.launcher.config.ConfigurationReader;
import com.dynatrace.easytravel.launcher.config.NodeFactory;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.logging.LoggerFactory;

public class DefaultProcedureSetting implements ProcedureSetting {

	private static final Logger LOGGER = LoggerFactory.make();

	public static final String DELIMITER = "###";

	private String type;
	private String name;
	private String value;
	private int stayOffDuration = AUTOMATIC_PLUGIN_ON_OFF_DISABLED;
	private int stayOnDuration = AUTOMATIC_PLUGIN_ON_OFF_DISABLED;

	/**
	 * For passing setting objects as part of REST interfaces
	 *
	 * @param value
	 * @author dominik.stadler
	 */
	public DefaultProcedureSetting(String value) {
		// TODO: instead of this strange parsing that depends on there being either 2, 3, 4 or 5 elements,
		// we should rather transfer the setting as XML-representation generated via write(), this way
		// we can rely that even more values in this class will not break existing code
		StringTokenizer token = new StringTokenizer(value, DELIMITER);
		if (token.countTokens() == 3) {
			this.type = token.nextToken();
			this.name = token.nextToken();
			this.value = token.nextToken();
		} else if (token.countTokens() == 2) {
			type = null;
			this.name = token.nextToken();
			this.value = token.nextToken();
		} else if (token.countTokens() == 4) {
			type = null;
			this.name = token.nextToken();
			this.value = token.nextToken();
			parseStayOnOffDuration(value, token);
		} else if (token.countTokens() == 5) {
			this.type = token.nextToken();
			this.name = token.nextToken();
			this.value = token.nextToken();
			parseStayOnOffDuration(value, token);
		} else {
			LOGGER.warn("Could not parse Procedure Setting: " + value);
		}
	}

	private void parseStayOnOffDuration(String value, StringTokenizer token) {
		try {
			this.stayOnDuration = Integer.parseInt(token.nextToken());
			this.stayOffDuration = Integer.parseInt(token.nextToken());
		} catch (NumberFormatException e) {
			LOGGER.warn("Could not parse numbers in Procedure Setting: " + value);
			this.stayOnDuration = AUTOMATIC_PLUGIN_ON_OFF_DISABLED;
			this.stayOffDuration = AUTOMATIC_PLUGIN_ON_OFF_DISABLED;
		}
	}

	/**
	 * For passing object-data via REST
	 *
	 * @return
	 * @author dominik.stadler
	 */
	@Override
	public String toREST() {
		String add = "";

		// if set, transfer the stayOn/OffDuration to the remote launcher as well
		if(stayOnDuration != AUTOMATIC_PLUGIN_ON_OFF_DISABLED || stayOffDuration != AUTOMATIC_PLUGIN_ON_OFF_DISABLED) {
			add = DELIMITER + stayOnDuration + DELIMITER + stayOffDuration;
		}

		if (type == null) {
			return name + DELIMITER + value + add;
		} else {
			return type + DELIMITER + name + DELIMITER + value + add;
		}
	}

	// default constructor for read(..) method
	DefaultProcedureSetting() {
	}

	/**
	 *
	 * @param name the setting name
	 * @param value the setting value
	 * @throws IllegalArgumentException if one of the arguments is null
	 * @author martin.wurzinger
	 */
	public DefaultProcedureSetting(String name, String value) {
		this(null, name, value, AUTOMATIC_PLUGIN_ON_OFF_DISABLED, AUTOMATIC_PLUGIN_ON_OFF_DISABLED);
	}


	/**
	 *
	 * @param type the type of the setting
	 * @param name the setting name
	 * @param value the setting value
	 * @throws IllegalArgumentException if one of the arguments is null
	 * @author martin.wurzinger
	 */
	public DefaultProcedureSetting(String type, String name, String value) {
		this(type, name, value, AUTOMATIC_PLUGIN_ON_OFF_DISABLED, AUTOMATIC_PLUGIN_ON_OFF_DISABLED);
	}

	/**
	 *
	 * @param type the type of the setting
	 * @param name the setting name
	 * @param value the setting value
	 * @throws IllegalArgumentException if one of the arguments is null
	 * @author martin.wurzinger
	 */
	public DefaultProcedureSetting(String type, String name, String value, int stayOnDuration, int stayOffDuration) {
		if (name == null || value == null) {
			throw new IllegalArgumentException("Name and Value arguments must not be null.");
		}
		if (stayOnDuration == 0 || stayOnDuration < AUTOMATIC_PLUGIN_ON_OFF_DISABLED || stayOffDuration == 0 ||
				stayOffDuration < AUTOMATIC_PLUGIN_ON_OFF_DISABLED) {
			throw new IllegalArgumentException("stayOnDuration and/or stayOffDuration must be greater than 0.");
		}

		this.type = type;
		this.name = name;
		this.value = value;
		this.stayOnDuration = stayOnDuration;
		this.stayOffDuration = stayOffDuration;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public void read(ConfigurationNode node, ConfigurationReader reader) throws ConfigurationException {
		String type = reader.readOptionalStringAttribute(node, Constants.ConfigurationXml.ATTRIBUTE_TYPE);

		String name = reader.readStringAttribute(node, Constants.ConfigurationXml.ATTRIBUTE_NAME);
		String value = reader.readStringAttribute(node, Constants.ConfigurationXml.ATTRIBUTE_VALUE);
		int stayoffDuration = reader.readOptionalIntAttribute(node, Constants.ConfigurationXml.ATTRIBUTE_STAY_OFF_DURATION);
		int stayonDuration = reader.readOptionalIntAttribute(node, Constants.ConfigurationXml.ATTRIBUTE_STAY_ON_DURATION);

		/* not needed, ConfigurationReader.readStringAttribute() never returns null
		if (name == null || value == null) {
			throw new ConfigurationException(TextUtils.merge("Some attributes of a node ''{0}'' are invalid.", node.getName()));
		}*/

		this.type = type;
		this.name = name;
		this.value = value;
		this.stayOffDuration = stayoffDuration;
		this.stayOnDuration = stayonDuration;
	}

	@Override
	public void write(ConfigurationNode node, NodeFactory factory) {
		if (type != null) {
			node.addAttribute(factory.createNode(Constants.ConfigurationXml.ATTRIBUTE_TYPE, type));
		}
		node.addAttribute(factory.createNode(Constants.ConfigurationXml.ATTRIBUTE_NAME, name));
		node.addAttribute(factory.createNode(Constants.ConfigurationXml.ATTRIBUTE_VALUE, value));
		if (stayOffDuration != AUTOMATIC_PLUGIN_ON_OFF_DISABLED) {
			node.addAttribute(factory.createNode(Constants.ConfigurationXml.ATTRIBUTE_STAY_OFF_DURATION, Integer.toString(stayOffDuration)));
		}
		if (stayOnDuration != AUTOMATIC_PLUGIN_ON_OFF_DISABLED) {
			node.addAttribute(factory.createNode(Constants.ConfigurationXml.ATTRIBUTE_STAY_ON_DURATION, Integer.toString(stayOnDuration)));
		}
	}

	@Override
	public DefaultProcedureSetting copy() {
		return new DefaultProcedureSetting(type, name, value, stayOnDuration, stayOffDuration);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}


	@Override
	public int getStayOffDuration() {
		return stayOffDuration;
	}


	public void setStayOffDuration(int stayOffDuration) {
		this.stayOffDuration = stayOffDuration;
	}


	public void setStayOnDuration(int stayOnDuration) {
		this.stayOnDuration = stayOnDuration;
	}

	@Override
	public int getStayOnDuration() {
		return stayOnDuration;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefaultProcedureSetting other = (DefaultProcedureSetting) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
}
