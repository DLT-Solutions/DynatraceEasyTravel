/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: AbstractNetstatTest.java
 * @date: 25.09.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.launcher.procedures;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.dynatrace.easytravel.constants.BaseConstants.SystemProperties;
import com.dynatrace.easytravel.util.NetstatUtil;
/**
 *
 * @author stefan.moschinski
 */
@RunWith(MockitoJUnitRunner.class)
public class NetstatUtilTest {

	@Mock
	Runtime runtime;

	@Mock
	Process process;

	String responseIp4Win =
			"TCP    172.16.103.247:25837   172.16.96.49:2021      ESTABLISHED     7392\n" +
					"[javaw.exe]\n" +
					"TCP    172.16.103.247:25843   172.16.96.49:2021      ESTABLISHED     7392\n" +
					"[javaw.exe]\n" +
					"TCP    172.16.103.247:26107   172.16.96.10:8001      ESTABLISHED     11420\n" +
					"[spotify.exe]\n" +
					"TCP    172.16.103.247:27252   172.16.98.197:445      ESTABLISHED     4\n" +
					"Can not obtain ownership information\n" +
					"TCP    127.0.0.1:9998         127.0.0.1:27931        TIME_WAIT       0\n" +
					"TCP    127.0.0.1:9998         127.0.0.1:27942        TIME_WAIT       0\n" +
					"TCP    127.0.0.1:9998         127.0.0.1:27948        TIME_WAIT       0\n" +
					"TCP    127.0.0.1:9998         127.0.0.1:27955        TIME_WAIT       0\n" +
					"UDP    127.0.0.1:20976        127.0.0.1:20978        ESTABLISHED     6292\n" +
					" [javaw.exe]\n";

	String responseIp6Win =
			"TCPv6    10::10:25837   172.16.96.49:2021      ESTABLISHED     7392\n" +
					"[javaw.exe]\n" +
					"TCPv6    10::10:25843   10::10:2021      ESTABLISHED     7392\n" +
					"[javaw.exe]\n" +
					"TCPv6    10::10:26107   172.16.96.10:8001      ESTABLISHED     11421\n" +
					"[spotify.exe]\n" +
					"TCPv6    10::10:27252   172.16.98.197:445      ESTABLISHED     4\n" +
					"Can not obtain ownership information\n" +
					"TCPv6    10::10.1.2.3:9998         127.0.0.1:27931        TIME_WAIT       0\n" +
					"TCPv6    10::10.1.2.3:9998         127.0.0.1:27942        TIME_WAIT       0\n" +
					"TCPv6    10::10.1.2.3:9998         127.0.0.1:27948        TIME_WAIT       0\n" +
					"TCPv6    10::10:9998         127.0.0.1:27955        TIME_WAIT       0\n" +
					"UDPv6    10::10.1.2.3:20976        127.0.0.1:20978        ESTABLISHED     6292\n" +
					" [javaw.exe]\n";

	String responseIp4Unix =
			"Aktive Internetverbindungen (Server und stehende Verbindungen)\n" +
					"Proto Recv-Q Send-Q Local Address           Foreign Address         State       PID/Program name\n" +
					"tcp        0      0 0.0.0.0:25              0.0.0.0:*               LISTEN      -               \n" +
					"tcp        0      0 127.0.0.1:32000         0.0.0.0:*               LISTEN      1791240/java    \n" +
					"tcp        0      0 127.0.0.1:5037          0.0.0.0:*               LISTEN      3369808/adb     \n" +
					"tcp        0      0 0.0.0.0:50000           0.0.0.0:*               LISTEN      1790461/dtserver\n" +
					"tcp        0      0 0.0.0.0:6000            0.0.0.0:*               LISTEN      -               \n" +
					"tcp        0      0 0.0.0.0:50001           0.0.0.0:*               LISTEN      1812802/dtanalysiss\n" +
					"tcp        0      0 127.0.0.1:58021         127.0.1.1:9998          VERBUNDEN   583221/eclipse  \n" +
					"tcp        0      0 127.0.0.1:58030         127.0.1.1:9998          VERBUNDEN   583221/eclipse  \n" +
					"tcp        0      0 127.0.0.1:50001         127.0.0.1:38478         VERBUNDEN   1812802/dtanalysiss\n" +
					"tcp        1      0 192.168.1.104:53304     69.171.246.16:80        CLOSE_WAIT  3078336/firefox \n" +
					"tcp        0      0 127.0.0.1:58022         127.0.1.1:9998          VERBUNDEN   583221/eclipse  \n" +
					"tcp        0      0 127.0.0.1:56780         127.0.0.1:9998          VERBUNDEN   1791019/java    \n" +
					"tcp       38      0 192.168.1.104:41040     91.189.89.224:443       CLOSE_WAIT  4910/python     \n" +
					"tcp        0      0 127.0.0.1:50000         127.0.0.1:33332         VERBUNDEN   1790461/dtserver\n" +
					"tcp        1      0 192.168.1.104:56139     212.69.189.196:80       CLOSE_WAIT  5136/gvfsd-http \n" +
					"tcp        1      0 192.168.1.101:54581     91.189.89.144:80        CLOSE_WAIT  5238/ubuntu-geoip-p\n" +
					"tcp        0      0 192.168.1.104:45623     69.171.246.16:80        VERBUNDEN   3078336/firefox \n" +
					"tcp        0      0 127.0.0.1:56778         127.0.0.1:9998          VERBUNDEN   1791019/java    \n" +
					"tcp        1      0 192.168.1.104:53306     69.171.246.16:80        CLOSE_WAIT  3078336/firefox \n" +
					"tcp        0      0 127.0.0.1:32000         127.0.0.1:31000         VERBUNDEN   1791238/wrapper \n" +
					"tcp        0      0 127.0.0.1:56781         127.0.0.1:9998          VERBUNDEN   1791019/java    \n" +
					"tcp        0      0 192.168.1.104:45358     173.252.100.27:80       VERBUNDEN   3078336/firefox \n" +
					"tcp6       0      0 :::8021                 :::*                    LISTEN      1790478/java    \n" +
					"tcp6       0      0 :::22                   :::*                    LISTEN      -               \n" +
					"tcp6       0      0 :::8023                 :::*                    LISTEN      1790478/java    \n" +
					"tcp6       0      0 ::1:631                 :::*                    LISTEN      -               \n" +
					"tcp6       0      0 :::9092                 :::*                    LISTEN      1791240/java    \n" +
					"tcp6       0      0 :::3141                 :::*                    LISTEN      1791019/java    \n" +
					"tcp6       0      0 127.0.0.1:38478         127.0.0.1:50001         VERBUNDEN   1812820/java    \n" +
					"tcp6       0      0 127.0.1.1:9998          127.0.0.1:58022         VERBUNDEN   1790478/java    \n" +
					"tcp6       0      0 127.0.0.1:9092          127.0.0.1:58124         VERBUNDEN   1791240/java    \n" +
					"udp        0      0 0.0.0.0:68              0.0.0.0:*                           -               \n" +
					"udp        0      0 0.0.0.0:514             0.0.0.0:*                           -               \n" +
					"udp6       0      0 :::514                  :::*                                -               \n" +
					"udp6       0      0 :::33848                :::*                                1791019/java    \n" +
					"udp6       0      0 :::5353                 :::*                                1791019/java    \n";

	@Test
	public void testWithNullResponseIOException() throws IOException {
		checkWithException(IOException.class);
	}

	@Test
	public void testWithNullResponseInterrruptedException() throws IOException {
		checkWithException(InterruptedException.class);
	}

	@Test
	public void testWithNullResponseExecutionException() throws IOException {
		checkWithException(ExecutionException.class);
	}

	@Test
	public void testWithNullResponseTimeoutException() throws IOException {
		checkWithException(TimeoutException.class);
	}

	private void checkWithException(final Class<?> exception) throws IOException {
		when(runtime.exec(any(String[].class))).thenReturn(process);
		when(process.getInputStream()).thenAnswer(new Answer<InputStream>() {

			@Override
			public InputStream answer(InvocationOnMock invocation) throws Throwable {
				Throwable throwable = (Throwable) exception.newInstance();
				throw throwable;
			}
		});

		NetstatUtil netstatUtil = new NetstatUtil(runtime);
		assertNull(netstatUtil.findProcessForPort(20976));
		assertNull(netstatUtil.findProcessIdForPort(20976));

		assertNull(netstatUtil.findProcessForPort(9998));
		assertNull(netstatUtil.findProcessIdForPort(9998));

		assertNull(netstatUtil.findProcessForPort(26107));
		assertNull(netstatUtil.findProcessIdForPort(26107));

		assertNull(netstatUtil.findProcessForPort(27252));
		assertNull(netstatUtil.findProcessIdForPort(27252));

		assertNull(netstatUtil.findProcessForPort(8080));
		assertNull(netstatUtil.findProcessIdForPort(8080));
	}

	@Test
	public void testWithIpV4Windows() throws IOException {
		when(runtime.exec(any(String[].class))).thenReturn(process);
		when(process.getInputStream()).thenAnswer(new Answer<InputStream>() {

			@Override
			public InputStream answer(InvocationOnMock invocation) throws Throwable {
				return IOUtils.toInputStream(responseIp4Win);
			}
		});

		NetstatUtil netstatUtil = new NetstatUtil(runtime);
		assertThat(netstatUtil.findProcessForPort(20976), is("javaw.exe"));
		assertThat(netstatUtil.findProcessIdForPort(20976), is("6292"));

		assertThat(netstatUtil.findProcessForPort(9998), is("javaw.exe"));
		assertThat(netstatUtil.findProcessIdForPort(9998), is("0"));

		assertThat(netstatUtil.findProcessForPort(26107), is("spotify.exe"));
		assertThat(netstatUtil.findProcessIdForPort(26107), is("11420"));

		assertThat(netstatUtil.findProcessForPort(27252), is("Can not obtain ownership information"));
		assertThat(netstatUtil.findProcessIdForPort(27252), is("4"));

		assertThat(netstatUtil.findProcessForPort(8080), is((String) null));
		assertThat(netstatUtil.findProcessIdForPort(8080), is((String) null));

		assertNull(netstatUtil.findProcessForPort(2837473));
		assertNull(netstatUtil.findProcessIdForPort(2837473));
	}

	@Test
	public void testWithIpV4Unix() throws IOException {
		when(runtime.exec(any(String[].class))).thenReturn(process);
		when(process.getInputStream()).thenAnswer(new Answer<InputStream>() {

			@Override
			public InputStream answer(InvocationOnMock invocation) throws Throwable {
				return IOUtils.toInputStream(responseIp4Unix);
			}
		});

		NetstatUtil netstatUtil = new NetstatUtil(runtime);
		assertThat(netstatUtil.findProcessForPort(8023), is("java"));
		assertThat(netstatUtil.findProcessIdForPort(8023), is("1790478"));

		assertThat(netstatUtil.findProcessForPort(38478), is("java"));
		assertThat(netstatUtil.findProcessIdForPort(38478), is("1812820"));

		assertThat(netstatUtil.findProcessForPort(53306), is("firefox"));
		assertThat(netstatUtil.findProcessIdForPort(53306), is("3078336"));

		assertThat(netstatUtil.findProcessForPort(33848), is("java"));
		assertThat(netstatUtil.findProcessIdForPort(33848), is("1791019"));

		assertThat(netstatUtil.findProcessForPort(8080), is((String) null));
		assertThat(netstatUtil.findProcessIdForPort(8080), is((String) null));

		assertThat(netstatUtil.findProcessForPort(9998), is("java"));
		assertThat(netstatUtil.findProcessIdForPort(9998), is("1790478"));

		assertNull(netstatUtil.findProcessForPort(2837473));
		assertNull(netstatUtil.findProcessIdForPort(2837473));
	}

	@Test
	public void testWithIpV6() throws IOException {
		when(runtime.exec(any(String[].class))).thenReturn(process);
		when(process.getInputStream()).thenAnswer(new Answer<InputStream>() {

			@Override
			public InputStream answer(InvocationOnMock invocation) throws Throwable {
				return IOUtils.toInputStream(responseIp6Win);
			}
		});

		NetstatUtil netstatUtil = new NetstatUtil(runtime);
		assertThat(netstatUtil.findProcessForPort(20976), is("javaw.exe"));
		assertThat(netstatUtil.findProcessIdForPort(20976), is("6292"));

		assertThat(netstatUtil.findProcessForPort(9998), is("javaw.exe"));
		assertThat(netstatUtil.findProcessIdForPort(9998), is("0"));

		assertThat(netstatUtil.findProcessForPort(26107), is("spotify.exe"));
		assertThat(netstatUtil.findProcessIdForPort(26107), is("11421"));

		assertThat(netstatUtil.findProcessForPort(27252), is("Can not obtain ownership information"));
		assertThat(netstatUtil.findProcessIdForPort(27252), is("4"));

		assertThat(netstatUtil.findProcessForPort(8080), is((String) null));
		assertThat(netstatUtil.findProcessIdForPort(8080), is((String) null));

		assertNull(netstatUtil.findProcessForPort(2837473));
		assertNull(netstatUtil.findProcessIdForPort(2837473));
	}

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	@Test
	public void testInitializationWithNullFails() {
		expectedEx.expect(NullPointerException.class);
		expectedEx.expectMessage("The runtime must not be null");
		new NetstatUtil(null);
	}

	@Test
	public void testDisablingViaSystemPropertyWorks() throws IOException {
		try {
			System.setProperty(SystemProperties.DISABLE_PORT_CHECK_VIA_NETSTAT, "true");
			when(runtime.exec(any(String[].class))).thenReturn(process);
			when(process.getInputStream()).thenReturn(IOUtils.toInputStream(responseIp4Win));
			NetstatUtil netstatUtil = new NetstatUtil(runtime);

			assertThat(netstatUtil.findProcessForPort(20976), is(nullValue()));
			assertThat(netstatUtil.findProcessIdForPort(20976), is(nullValue()));
		} finally {
			System.setProperty(SystemProperties.DISABLE_PORT_CHECK_VIA_NETSTAT, "false");
		}
	}

	@Test
	public void testWithTimeout() throws IOException {
		when(runtime.exec(any(String[].class))).thenReturn(process);
		when(process.getInputStream()).thenAnswer(new Answer<InputStream>() {

			@Override
			public InputStream answer(InvocationOnMock invocation) throws Throwable {
				// default timeout is 5 sec
				Thread.sleep(6000);

				return IOUtils.toInputStream(responseIp6Win);
			}
		});

		// null because timeout is reached
		NetstatUtil netstatUtil = new NetstatUtil(runtime);
		assertNull(netstatUtil.findProcessForPort(20976));
		assertNull(netstatUtil.findProcessIdForPort(20976));
	}


	@Test
	public void testWithInterruptedException() throws IOException, InterruptedException {
		when(runtime.exec(any(String[].class))).thenReturn(process);
		when(process.getInputStream()).thenAnswer(new Answer<InputStream>() {

			@Override
			public InputStream answer(InvocationOnMock invocation) throws Throwable {
				// default timeout is 5 sec
				Thread.sleep(6000);

				return IOUtils.toInputStream(responseIp6Win);
			}
		});

		final Thread thread = Thread.currentThread();
		Thread t = new Thread("Interrupter") {

			@Override
			public void run() {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				thread.interrupt();
			}
		};

		t.start();

		// null because thread is interrupted
		NetstatUtil netstatUtil = new NetstatUtil(runtime);
		assertNull(netstatUtil.findProcessForPort(20976));

		t.join();
	}
}
