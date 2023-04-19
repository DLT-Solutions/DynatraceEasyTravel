package com.dynatrace.diagnostics.uemload.openkit.action.definition;

import com.dynatrace.diagnostics.uemload.openkit.event.EventCallback;
import com.dynatrace.diagnostics.uemload.openkit.time.TimeService;
import com.dynatrace.openkit.api.Action;

import java.util.concurrent.Future;
import java.util.function.Supplier;

public interface ActionParent<A extends Action> {
	A getAction();
	void addSubTask(Supplier<Future> task);
	void ended(EventCallback callback);
	void started(EventCallback callback);
	void setTimeService(TimeService timeService);
}
