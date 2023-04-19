/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: ColumnPrefixTest.java
 * @date: 28.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.hbase.serializer;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;


/**
 *
 * @author stefan.moschinski
 */
public class ColumnPrefixTest {


	@Test
	public void testPrefixCreation() throws Exception {
		assertThat(ColumnPrefix.createPrefix("main").toString(), is("main"));
		assertThat(ColumnPrefix.createPrefix("main", "sub").toString(), is("main_sub"));
	}

	@Test
	public void testApprehendPrefix() throws Exception {
		ColumnPrefix base = ColumnPrefix.createPrefix("main");
		assertThat(ColumnPrefix.apprehendPrefix(base, "apprehend").toString(), is("main_apprehend"));
	}
}
