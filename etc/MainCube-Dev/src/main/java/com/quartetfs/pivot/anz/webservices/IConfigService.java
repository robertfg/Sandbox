package com.quartetfs.pivot.anz.webservices;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Produces("text/plain")
public interface IConfigService {
	@GET
	@Path("/config/")
	public String config();
	@GET
	@Path("/version/")
	public String version();
	@GET
	@Path("/allevents/")
	public String printAllEvents();
	@GET
	@Path("/parsevents/")
	public String printParseEvents();
	@GET
	@Path("commitevents")
	public String printCommitEvents();
	@GET
	@Path("deletevents")
	public String printDeleteEvents();
	
	@GET
	@Path("cleanevents")
	public void cleanAllEvents();
	
	@GET
	@Path("vectorLabels")
	public String vectorLabels();
	
}
