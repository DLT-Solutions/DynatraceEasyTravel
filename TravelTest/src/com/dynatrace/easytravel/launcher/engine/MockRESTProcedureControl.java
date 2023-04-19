package com.dynatrace.easytravel.launcher.engine;

import java.util.List;
import java.util.UUID;

import javax.ws.rs.*;

import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureSetting;

/**
 * Test-Handler for simulating REST for testing class Batch
 *
 * @author dominik.stadler
 */
@Path("/")
public class MockRESTProcedureControl {
	private State state = State.UNKNOWN;


	@GET
    @Produces("text/plain")
    @Path(Constants.REST.PREPARE + "/{name}")
    public synchronized String prepare(
            @PathParam("name") final String name,
            @QueryParam("property") final List<String> properties,
            @QueryParam("setting") final List<DefaultProcedureSetting> settings,
            @QueryParam("environment") final List<DefaultProcedureSetting> environment
            )
    {
        return UUID.randomUUID().toString() + "|1234";
    }

	@GET
	@Produces("text/plain")
	@Path(Constants.REST.START + "/{uuid}")
    public synchronized String start(
			@PathParam("uuid") final String uuid)
	{
		state = State.OPERATING;
		return state.toString();
	}

	@GET
	@Produces("text/plain")
	@Path(Constants.REST.STOP + "/{uuid}")
    public synchronized String stop(
			@PathParam("uuid") final String uuid) {
		state = State.UNKNOWN;
		return "OK";
	}

	@GET
	@Produces("text/plain")
	@Path(Constants.REST.STATUS + "/{uuid}")
    public String status(
			@PathParam("uuid") final String uuid) {
		return state.toString();
	}
}
