package com.dynatrace.easytravel.business;

import static org.junit.Assert.assertEquals;

import javax.management.ObjectName;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.easymock.EasyMock;
import org.junit.Test;


public class MBeanPostProcessorTest {

	@Test
	public void testPostProcessAfterInitialization() throws Exception {
		MBeanPostProcessor proc = new MBeanPostProcessor();
		assertEquals("String", proc.postProcessAfterInitialization("String", "somename"));
		
		DataSource source = EasyMock.createStrictMock(DataSource.class);
		EasyMock.expect(source.preRegister(null, new ObjectName("*:*"))).andReturn(null);
		
		EasyMock.replay(source);
		assertEquals(source, proc.postProcessAfterInitialization(source, "somename"));
		EasyMock.verify(source);
	}

	@Test
	public void testPostProcessAfterInitializationFails() throws Exception {
		MBeanPostProcessor proc = new MBeanPostProcessor();
		assertEquals("String", proc.postProcessAfterInitialization("String", "somename"));
		
		DataSource source = EasyMock.createStrictMock(DataSource.class);
		EasyMock.expect(source.preRegister(null, new ObjectName("*:*"))).andThrow(new IllegalStateException("testexception"));
		
		EasyMock.replay(source);
		assertEquals(source, proc.postProcessAfterInitialization(source, "somename"));
		EasyMock.verify(source);
	}

	@Test
	public void testPostProcessBeforeInitialization() {
		MBeanPostProcessor proc = new MBeanPostProcessor();
		assertEquals("String", proc.postProcessBeforeInitialization("String", "somename"));

		DataSource source = EasyMock.createStrictMock(DataSource.class);
		
		EasyMock.replay(source);
		assertEquals(source, proc.postProcessBeforeInitialization(source, "somename"));
		EasyMock.verify(source);
	}
}
