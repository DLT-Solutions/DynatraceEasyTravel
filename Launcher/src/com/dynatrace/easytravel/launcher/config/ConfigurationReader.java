package com.dynatrace.easytravel.launcher.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration.tree.ConfigurationNode;

import com.dynatrace.easytravel.config.ConfigurationException;
import com.dynatrace.easytravel.util.TextUtils;


public class ConfigurationReader {

    /**
     *
     * @param node
     * @param childName
     * @return
     * @throws ConfigurationException if no child node with specified name could not be found
     * @author martin.wurzinger
     */
    public String readTextChild(ConfigurationNode node, String childName) throws ConfigurationException {
        List<ConfigurationNode> childNodes = getChildren(node, childName);
        if (childNodes.isEmpty()) {
            throw new ConfigurationException(TextUtils.merge("Node ''{0}'' node must have a child node called ''{1}''.", node.getName(), childName));
        }

        return (String) childNodes.get(0).getValue();
    }

    /**
     *
     * @param node
     * @param attributeName
     * @return
     * @throws ConfigurationException if no integer with specified name could not be found
     * @author martin.wurzinger
     */
    public int readIntAttribute(ConfigurationNode node, String attributeName) throws ConfigurationException {
        List<ConfigurationNode> intAttributes = getAttributes(node, attributeName);
        if (intAttributes.isEmpty()) {
            throw new ConfigurationException(TextUtils.merge("The node ''{0}'' must have a ''{1}'' attribute.", node.getName(), attributeName));
        }

        try {
            return Integer.parseInt((String) intAttributes.get(0).getValue());
        } catch (NumberFormatException e) {
            throw new ConfigurationException(TextUtils.merge("Unable to parse attribute ''{0}'' of node ''{1}''.", attributeName, node.getName()), e);
        }
    }

    /**
     *
     * @param node
     * @param attributeName
     * @return the parsed <code>int</code> value of the attribute or -1 if given attribute name could not be found
     * or the attribute value is no <code>int</code>
     * @author richrd.uttenthaler
     */
    public int readOptionalIntAttribute(ConfigurationNode node, String attributeName) {
        List<ConfigurationNode> attributes = getAttributes(node, attributeName);
        if (attributes.isEmpty()) {
            return -1;
        }
        try {
        	return Integer.parseInt((String) attributes.get(0).getValue());
        } catch (NumberFormatException e) {
        	return -1;
        }
    }

    /**
     *
     * @param node
     * @param attributeName
     * @return
     * @throws ConfigurationException if no attribute with specified name could not be found
     * @author martin.wurzinger
     */
    public String readStringAttribute(ConfigurationNode node, String attributeName) throws ConfigurationException {
        String attribute = readOptionalStringAttribute(node, attributeName);
        if (attribute == null) {
            throw new ConfigurationException(TextUtils.merge("The node ''{0}'' must have a ''{1}'' attribute.",
            		node.getName() == null ? "<null>" : node.getName(), attributeName));
        }
        return attribute;
    }

    /**
     *
     * @param node
     * @param attributeName
     * @return
     * @throws ConfigurationException if no attribute with specified name could not be found
     * @author martin.wurzinger
     */
    public String readOptionalStringAttribute(ConfigurationNode node, String attributeName) {
        List<ConfigurationNode> attributes = getAttributes(node, attributeName);
        if (attributes.isEmpty()) {
            return null;
        }
        return (String) attributes.get(0).getValue();
    }

    /**
     *
     * @param node
     * @param attributeName
     * @return
     * @throws ConfigurationException if no boolean attribute with specified name could not be found
     * @author martin.wurzinger
     */
    public boolean readBooleanAttribute(ConfigurationNode node, String attributeName) throws ConfigurationException {
        List<ConfigurationNode> attributes = getAttributes(node, attributeName);

        if (attributes.isEmpty()) {
            throw new ConfigurationException(TextUtils.merge("The node ''{0}'' must have a ''{1}'' attribute.", node.getName(), attributeName));
        }

        try {
            return Boolean.parseBoolean((String) attributes.get(0).getValue());
        } catch (Exception e) {
            throw new ConfigurationException(TextUtils.merge("Unable to parse attribute ''{0}'' of node ''{1}''.", attributeName, node.getName()), e);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<ConfigurationNode> getChildren(ConfigurationNode node, String childrenName) {
        List childrenRaw = node.getChildren(childrenName);
        List<ConfigurationNode> childrenTyped = Collections.checkedList(new ArrayList<ConfigurationNode>(childrenRaw.size()), ConfigurationNode.class);
        childrenTyped.addAll(childrenRaw);

        return childrenTyped;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<ConfigurationNode> getAttributes(ConfigurationNode node, String attributeName) {
        List attributesRaw = node.getAttributes(attributeName);
        List<ConfigurationNode> attributesTyped = Collections.checkedList(new ArrayList<ConfigurationNode>(attributesRaw.size()), ConfigurationNode.class);
        attributesTyped.addAll(attributesRaw);

        return attributesTyped;
    }
}
