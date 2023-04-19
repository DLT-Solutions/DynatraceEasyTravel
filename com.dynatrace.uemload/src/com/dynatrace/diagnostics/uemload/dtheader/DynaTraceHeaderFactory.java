package com.dynatrace.diagnostics.uemload.dtheader;


public class DynaTraceHeaderFactory {

	private static final NullDynaTraceHeader NULL_DT_HEADER = new NullDynaTraceHeader();

	/**
	 * Creates a new instance of a class implementing the {@link DynaTraceHeader} interface.
	 * If tagged web requests are disabled, a non-doing null object is returned.
	 * 
	 * @param virtualUserId Id of the virtual user
	 * @param taggedWebRequest defines whether tagged web requests are enabled
	 * @return
	 * @author stefan.moschinski
	 */
	public static DynaTraceHeader newInstance(int virtualUserId, boolean taggedWebRequest) {
		return taggedWebRequest ? new DynaTraceHeaderImpl(virtualUserId) : NULL_DT_HEADER;
	}
}
