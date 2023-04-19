package com.dynatrace.easytravel.launcher.config;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;


public class NodeFactory {

    public ConfigurationNode createNode(String name) {
        return new HierarchicalConfiguration.Node(name);
    }

    public ConfigurationNode createNode(String name, Object value) {
        ConfigurationNode node = createNode(name);
        node.setValue(value);
        return node;
    }
}
