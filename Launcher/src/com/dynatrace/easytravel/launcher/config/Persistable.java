package com.dynatrace.easytravel.launcher.config;

import org.apache.commons.configuration.tree.ConfigurationNode;

import com.dynatrace.easytravel.config.ConfigurationException;


/**
 * Enables to store and restore configuration instances to and from a tree-structured persistence
 * unit like an XML file.
 * 
 * @author martin.wurzinger
 */
public interface Persistable {

    /**
     * Load instance data from tree-structured persisted configuration (e.g. XML file).
     * 
     * @param node the tree node to read attributes and children from
     * @param reader provides helper methods to read value, attributes and child nodes
     * @throws ConfigurationException if an the persisted configuration does not meet the
     *         expectations (out-dated structure, corrupt, invalid values,...)
     * @author martin.wurzinger
     */
    void read(ConfigurationNode node, ConfigurationReader reader) throws ConfigurationException;

    /**
     * Write instance data to tree-structured persistence unit (e.g. XML file).
     * 
     * @param node the tree node to write this instance data to
     * @param factory provides helper methods to create child and attribute nodes
     * @author martin.wurzinger
     */
    void write(ConfigurationNode node, NodeFactory factory);

}
