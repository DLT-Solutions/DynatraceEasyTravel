package org.apache.catalina.session;

import static org.easymock.EasyMock.*;

import java.beans.PropertyChangeListener;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Manager;
import org.apache.juli.logging.LogFactory;
import org.easymock.internal.matchers.Any;


public class ManagerTestBase {
	Context container = createMock(Context.class);
	Container parentContainer = createMock(Container.class);

	public void start(Manager manager) {
		expect(parentContainer.getName()).andReturn("context").anyTimes();
		expect(parentContainer.getParent()).andReturn(null).anyTimes();

		expect(container.getSessionTimeout()).andReturn(30).anyTimes();
		expect(container.getName()).andReturn("context").anyTimes();
		container.addPropertyChangeListener(anyPropertyChangeListener());
		expectLastCall().anyTimes();
		expect(container.getApplicationLifecycleListeners()).andReturn(new Object[] {}).atLeastOnce();
		expect(container.getParent()).andReturn(parentContainer).anyTimes();
		expect(container.getDistributable()).andReturn(false).anyTimes();
		expect(container.getLogger()).andReturn(LogFactory.getLog(PersistentManagerBase.class)).anyTimes();
		expect(container.getLoader()).andReturn(null).anyTimes();
		expect(container.getApplicationEventListeners()).andReturn(null).anyTimes();

		replay(container,parentContainer);

		manager.setContainer(container);
	}

	public void stop() {
		verify(container,parentContainer);
	}

	private PropertyChangeListener anyPropertyChangeListener() {
        reportMatcher(Any.ANY);
		return null;
	}
}
