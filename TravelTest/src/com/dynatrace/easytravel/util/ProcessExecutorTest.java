/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: ProcessExecutorTest.java
 * @date: 22.11.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.dynatrace.easytravel.launcher.agent.OperatingSystem;


/**
 *
 * @author stefan.moschinski
 */
@RunWith(MockitoJUnitRunner.class)
public class ProcessExecutorTest {
	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Mock
	public Runtime runtimeMock;

	@Mock
	public Process processMock;

	@Test
	public void testGetInputStreamWorks() throws InterruptedException, ExecutionException, TimeoutException {
		ProcessExecutor processExecutor = OperatingSystem.IS_WINDOWS ? 
			new ProcessExecutor(Runtime.getRuntime(), new String[] { "ping", "localhost" }) :
			new ProcessExecutor(Runtime.getRuntime(), new String[] { "ping", "-c", "4", "localhost" });
		assertThat(processExecutor.getInputAsString(10, TimeUnit.SECONDS), is(not(nullValue())));
	}

	@Test
	public void testWithMocking() throws InterruptedException, ExecutionException, TimeoutException, IOException {
		String returnValue = "works";

		String[] cmd = new String[] { "ping", "localhost" };
		ProcessExecutor processExecutor = new ProcessExecutor(runtimeMock, cmd);

		when(runtimeMock.exec(cmd)).thenReturn(processMock);
		when(processMock.getInputStream()).thenReturn(IOUtils.toInputStream(returnValue));

		assertThat(processExecutor.getInputAsString(5, TimeUnit.SECONDS), is(returnValue));

		// verify invocation order
		InOrder invocOrder = inOrder(runtimeMock, processMock);
		invocOrder.verify(runtimeMock).exec(any(String[].class));
		invocOrder.verify(processMock).getInputStream();
		invocOrder.verify(processMock).destroy();
	}

	@Test
	public void testThrowsTimeoutExceptionIfGetInputStreamTakesToLong() throws InterruptedException, ExecutionException,
			TimeoutException {
		ProcessExecutor processExecutor = new ProcessExecutor(Runtime.getRuntime(), new String[] { "ping", "localhost" });

		exception.expect(TimeoutException.class);
		processExecutor.getInputAsString(0, TimeUnit.SECONDS);

	}
}
