package com.dynatrace.easytravel.tomcat;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.catalina.Context;
import org.apache.catalina.Manager;
import org.easymock.internal.matchers.Any;
import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.ipc.SocketUtils;


public class Tomcat7StarterTest {
    EasyTravelConfig config = EasyTravelConfig.read();

	@Test
	public void testCreate() throws Exception {
		Tomcat7Starter run = new Tomcat7Starter();
		assertNotNull(run);

		// does nothing here
		run.stop();
	}

    @Ignore("Fails in CI because it loads all classes from all available jars here")
	@Test
	public void testRunAndStop() throws Exception {
		Tomcat7Starter run = new Tomcat7Starter();
		Tomcat7Config tomcatConfig = new Tomcat7Config.Tomcat7ConfigBuilder()
				.withPort(SocketUtils.getNextFreePort(7000, 7777, BaseConstants.LOCALHOST))
				.build();
		run.run(tomcatConfig);

		// only set for persistent == true
		assertNull(run.getManager());
		assertNotNull(run.getServerName());
		run.setManager(null);
		assertNull(run.getManager());

		run.stop();
	}

    @Ignore("Fails in CI because it loads all classes from all available jars here")
	@Test
	public void testRunAndStopWithPersistentManager() throws Exception {
		Tomcat7Starter run = new Tomcat7Starter();
		Tomcat7Config tomcatConfig = new  Tomcat7Config.Tomcat7ConfigBuilder()
				.withPort(SocketUtils.getNextFreePort(7100, 7777, BaseConstants.LOCALHOST))
				.withShutdownPort(SocketUtils.getNextFreePort(7200, 7777, BaseConstants.LOCALHOST))
				.build();
		run.run(tomcatConfig);

		// only set for persistent == true
		assertNotNull(run.getManager());

		run.stop();
	}

	@Test
	public void testCreatePersistentManager() throws Exception {
		Tomcat7Starter run = new Tomcat7Starter();
		Context context = createStrictMock(Context.class);

		context.setManager(anyManager());

		replay(context);

		run.createPersistentManager("some", context);

		verify(context);
	}

	private Manager anyManager() {
        reportMatcher(Any.ANY);

		return null;
	}
}
