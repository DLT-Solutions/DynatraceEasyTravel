/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: ColumnPrefix.java
 * @date: 28.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.hbase.serializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Joiner;


/**
 *
 * @author stefan.moschinski
 */
public class ColumnPrefix {

	private final List<String> namespace;

	private ColumnPrefix(String namespace, String[] subnamespaces) {
		this.namespace = new ArrayList<String>(1 + subnamespaces.length);
		this.namespace.add(namespace);
		this.namespace.addAll(Arrays.asList(subnamespaces));
	}

	private ColumnPrefix(ColumnPrefix basePrefix, String addNamespace, String[] addNs) {
		this.namespace = new ArrayList<String>(basePrefix.namespace);
		this.namespace.add(addNamespace);
		this.namespace.addAll(Arrays.asList(addNs));
	}

	public static ColumnPrefix createPrefix(String namespace, String... subnamespaces) {
		return new ColumnPrefix(namespace, subnamespaces);
	}

	public static ColumnPrefix apprehendPrefix(ColumnPrefix basePrefix, String addNamespace, String... addNs) {
		return new ColumnPrefix(basePrefix, addNamespace, addNs);
	}

	@Override
	public String toString() {
		return join(namespace);
	}

	public String getPrefixedColumnName(String columnName) {
		List<String> list = new ArrayList<String>(namespace);
		list.add(columnName);
		return join(list);
	}

	/**
	 * 
	 * @return
	 * @author stefan.moschinski
	 */
	private String join(List<String> coll) {
		return Joiner.on("_").join(coll);
	}



}
