/*

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.measures.IMeasureDimension;
import com.quartetfs.biz.pivot.dto.CellDTO;
import com.quartetfs.biz.pivot.dto.CellSetDTO;
import com.quartetfs.biz.pivot.dto.DrillthroughResultDTO;
import com.quartetfs.biz.pivot.impl.Location;
import com.quartetfs.biz.pivot.query.IMDXQuery;
import com.quartetfs.biz.pivot.query.impl.DrillthroughQuery;
import com.quartetfs.biz.pivot.query.impl.GetAggregatesQuery;
import com.quartetfs.biz.pivot.query.impl.MDXQuery;
import com.quartetfs.biz.pivot.webservices.IQueriesService;
import com.quartetfs.biz.pivot.webservices.impl.JAXBDataBindingFactory;
import com.quartetfs.tech.streaming.IIdGenerator;
import com.quartetfs.tech.streaming.ILongPollingService;
import com.quartetfs.tech.streaming.IStreamProperties;
import com.quartetfs.tech.streaming.IStreamProperties.InitialState;
import com.quartetfs.tech.streaming.IStreamingService;
import com.quartetfs.tech.streaming.impl.StreamProperties;

*//**
 * 
 * A simple web service client to illustrate web service consumption from a Java
 * client.
 * 
 * @author Quartet Financial Systems
 * 
 *//*
public class ExecMdx { 

	*//** the logger **//*
	private static Logger logger = Logger.getLogger( ExecMdx.class.getName() );

	*//** Base url for web services *//*
//	public static final String BASE_URL = "http://localhost:8080/activepivot-sandbox-4.2.0/";
	 public static final String BASE_URL = "http://localhost:9090/";

	*//** Publication domain *//*
	public static final String PUBLICATION_DOMAIN = "test-domain";

	*//** User name *//*
	public static final String USER = "admin";

	*//** User password *//*
	public static final String PASSWORD = "admin";

	*//** User password *//*
	public static final String PIVOT_ID = "EquityDerivativesCube";

	*//**
	 * You can run this sample client once the Sandbox server has been started.
	 * 
	 * @param args
	 *//*
	public static void main(String[] args) throws Exception {
		final boolean allowCompression = false;

		RemoteServiceBean rsb = new RemoteServiceBean();
		fillWebServiceProxy(rsb, allowCompression, BASE_URL + "webservices/", USER, PASSWORD);
//		fillSpringRemotingProxy(rsb, allowCompression, BASE_URL + "remoting/", USER, PASSWORD);

		rsb.check();

		// List dimensions 
		final List<String> dimensions;
		{
			logger.log(Level.INFO, "Discovering dimensions on " + PIVOT_ID + ".");
			dimensions = rsb.queriesService.retrieveDimensions(PIVOT_ID);
			logger.log(Level.INFO, "Discovered dimensions on " + PIVOT_ID + ": " + dimensions);
		}

		// Execute a query through a GetAggregatesQuery
		{
			List<String> defaultMembers = rsb.queriesService.retrieveDefaultMembers(PIVOT_ID);
			
			Object[][] arrayLocation = new Object[defaultMembers.size()][];
			for (int i = 0 ; i < defaultMembers.size() ; i++) {
				if (i == 0) {
					arrayLocation[i] = new Object[] { defaultMembers.get(i), null };
				} else if (i == defaultMembers.size() - 1) {
					arrayLocation[i] = new Object[] { null };
				} else {
					arrayLocation[i] = new Object[] { defaultMembers.get(i) };
				}
			}

			GetAggregatesQuery query = new GetAggregatesQuery(Collections.<ILocation> singleton(new Location(arrayLocation)),
					Collections.singleton(IMeasureDimension.COUNT_ID));
			query.setPivotId(PIVOT_ID);

			logger.log(Level.INFO, "Executing GetAggregates query: " + query);

			long start = System.currentTimeMillis();
			rsb.queriesService.execute(query);
			logger.log(Level.INFO, "Time of GetAggregatesQuery = " + (System.currentTimeMillis() - start));
		}
		
		// Execute a drillthrough through a GetAggregatesQuery
		{
			List<String> defaultMembers = rsb.queriesService.retrieveDefaultMembers(PIVOT_ID);
			
			Object[][] arrayLocation = new Object[defaultMembers.size()][];
			for (int i = 0 ; i < defaultMembers.size() ; i++) {
				arrayLocation[i] = new Object[] { defaultMembers.get(i) };
			}

			DrillthroughQuery query = new DrillthroughQuery(Collections.<ILocation> singleton(new Location(arrayLocation)));
			query.setPivotId(PIVOT_ID);

			logger.log(Level.INFO, "Executing Drillthrough query: " + query);

			long start = System.currentTimeMillis();
			DrillthroughResultDTO drillthroughDTO = rsb.queriesService.execute(query);
			logger.log(Level.INFO, "Time of Drillthrough (" + drillthroughDTO.getRows().size() + " rows) = " + (System.currentTimeMillis() - start));
		}

		// Register a realtime query through an ad-hoc MdxQuery
		{
			String mdx = "SELECT NON EMPTY {DrilldownLevel({[" + dimensions.get(0) + "].[ALL].[AllMember]})} ON ROWS,  NON EMPTY {DrilldownLevel({[" + dimensions.get(dimensions.size() - 1) + "].Members})} ON COLUMNS FROM [" + PIVOT_ID + "] WHERE ([Measures].[" + IMeasureDimension.COUNT_ID + "])";

			IMDXQuery query = new MDXQuery(mdx);
			logger.log(Level.INFO, "Executing MDX query: " + mdx);

			// We execute the query for the sake of the example (it is not
			// necessary for the following realtime registration)
			{
				long start = System.currentTimeMillis();
				CellSetDTO cellSet = rsb.queriesService.execute(query);
				logger.log(Level.INFO, "Time of MdxQuery = " + (System.currentTimeMillis() - start));

				List<CellDTO> cells = cellSet.getCells();
				for (CellDTO cell : cells) {
					logger.log(Level.INFO, "Cell: " + cell);
				}
			}

			// First, we register the listener
			String listenerId = rsb.idGenerator.generateListenerIds(1)[0];
			rsb.longPollingService.addListener(PUBLICATION_DOMAIN, listenerId);
			
			// This thread will long-poll the results
			Executors.newSingleThreadExecutor().execute(new LongPollingListener(rsb.longPollingService, listenerId));

			// Then, we subscribe a continuous query, events will be received
			// through the communication channel.
			String streamId = rsb.idGenerator.generateStreamIds(1)[0];
			IStreamProperties streamProperties = new StreamProperties(streamId, PUBLICATION_DOMAIN, InitialState.STARTED, true);
			rsb.streamingService.createStream(query, streamProperties);
		}
	}

	// Build remote service proxies using WebServices (SOAP-based)
	public static void fillWebServiceProxy(RemoteServiceBean rsb, boolean allowCompression, String baseURL, String userName, String password) throws Exception {

		// HTTP Client Policy
		HTTPClientPolicy policy = new HTTPClientPolicy();
		policy.setConnectionTimeout(5000);
		policy.setReceiveTimeout(30000);
		policy.setAllowChunking(false);

		// Create a JAXB data binding
		JAXBDataBindingFactory bindingFactory = new JAXBDataBindingFactory();

		// Create a JAX-WS service factories for the target services.
		JaxWsProxyFactoryBean queriesServiceFactory = new JaxWsProxyFactoryBean();
		queriesServiceFactory.setAddress(baseURL + "Queries");
		queriesServiceFactory.setServiceClass(IQueriesService.class);
		queriesServiceFactory.setDataBinding(bindingFactory.create());
		queriesServiceFactory.setUsername(userName);
		queriesServiceFactory.setPassword(password);
		if (allowCompression) {
			AcceptGZipUtil.applyOnFactory(queriesServiceFactory);
		}

		JaxWsProxyFactoryBean streamingServiceFactory = new JaxWsProxyFactoryBean();
		streamingServiceFactory.setAddress(baseURL + "Streaming");
		streamingServiceFactory.setServiceClass(IStreamingService.class);
		streamingServiceFactory.setDataBinding(bindingFactory.create());
		streamingServiceFactory.setUsername(userName);
		streamingServiceFactory.setPassword(password);
		if (allowCompression) {
			AcceptGZipUtil.applyOnFactory(streamingServiceFactory);
		}

		JaxWsProxyFactoryBean longPollingServiceFactory = new JaxWsProxyFactoryBean();
		longPollingServiceFactory.setAddress(baseURL + "LongPolling");
		longPollingServiceFactory.setServiceClass(ILongPollingService.class);
		longPollingServiceFactory.setDataBinding(bindingFactory.create());
		longPollingServiceFactory.setUsername(userName);
		longPollingServiceFactory.setPassword(password);
		if (allowCompression) {
			AcceptGZipUtil.applyOnFactory(longPollingServiceFactory);
		}

		JaxWsProxyFactoryBean idGeneratorServiceFactory = new JaxWsProxyFactoryBean();
		idGeneratorServiceFactory.setAddress(baseURL + "IdGenerator");
		idGeneratorServiceFactory.setServiceClass(IIdGenerator.class);
		idGeneratorServiceFactory.setDataBinding(bindingFactory.create());
		idGeneratorServiceFactory.setUsername(userName);
		idGeneratorServiceFactory.setPassword(password);

		// Instantiate services using the factories
		rsb.queriesService = queriesServiceFactory.create(IQueriesService.class);
		Client queriesClient = ClientProxy.getClient(rsb.queriesService);
		if (queriesClient != null) {
			HTTPConduit conduit = (HTTPConduit) queriesClient.getConduit();
			conduit.setClient(policy);
		}
		if (allowCompression) {
			AcceptGZipUtil.applyOnInstance(rsb.queriesService);
		}

		rsb.streamingService = streamingServiceFactory.create(IStreamingService.class);
		Client streamingClient = ClientProxy.getClient(rsb.streamingService);
		if (streamingClient != null) {
			HTTPConduit conduit = (HTTPConduit) streamingClient.getConduit();
			conduit.setClient(policy);
		}
		if (allowCompression) {
			AcceptGZipUtil.applyOnInstance(rsb.streamingService);
		}

		rsb.longPollingService = longPollingServiceFactory.create(ILongPollingService.class);
		Client longPollingClient = ClientProxy.getClient(rsb.longPollingService);
		if (longPollingClient != null) {
			HTTPConduit conduit = (HTTPConduit) longPollingClient.getConduit();
			conduit.setClient(policy);
		}
		if (allowCompression) {
			AcceptGZipUtil.applyOnInstance(rsb.longPollingService);
		}

		rsb.idGenerator = idGeneratorServiceFactory.create(IIdGenerator.class);
		Client idGeneratorClient = ClientProxy.getClient(rsb.idGenerator);
		if (idGeneratorClient != null) {
			HTTPConduit conduit = (HTTPConduit) idGeneratorClient.getConduit();
			conduit.setClient(policy);
		}
	}

	// Build remote service proxies using Spring HTTP Invokers (java binary based)
	public static void fillSpringRemotingProxy(RemoteServiceBean rsb, boolean allowCompression, String baseURL, String userName, String password) {
		AuthenticatedHTTPRequestExecutor authenticatedHTTPRequestExecutor = new AuthenticatedHTTPRequestExecutor();
		authenticatedHTTPRequestExecutor.setUsername(userName);
		authenticatedHTTPRequestExecutor.setPassword(password);
		authenticatedHTTPRequestExecutor.setAcceptGzipEncoding(allowCompression);

		HttpInvokerProxyFactoryBean queriesServiceFactory = new org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean();
		queriesServiceFactory.setServiceUrl(baseURL + "QueriesService");
		queriesServiceFactory.setServiceInterface(IQueriesService.class);
		queriesServiceFactory.setHttpInvokerRequestExecutor(authenticatedHTTPRequestExecutor);
		queriesServiceFactory.afterPropertiesSet();

		HttpInvokerProxyFactoryBean streamingServiceFactory = new org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean();
		streamingServiceFactory.setServiceUrl(baseURL + "StreamingService");
		streamingServiceFactory.setServiceInterface(IStreamingService.class);
		streamingServiceFactory.setHttpInvokerRequestExecutor(authenticatedHTTPRequestExecutor);
		streamingServiceFactory.afterPropertiesSet();

		HttpInvokerProxyFactoryBean longPollingServiceFactory = new org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean();
		longPollingServiceFactory.setServiceUrl(baseURL + "LongPollingService");
		longPollingServiceFactory.setServiceInterface(ILongPollingService.class);
		longPollingServiceFactory.setHttpInvokerRequestExecutor(authenticatedHTTPRequestExecutor);
		longPollingServiceFactory.afterPropertiesSet();

		HttpInvokerProxyFactoryBean idGeneratorServiceFactory = new org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean();
		idGeneratorServiceFactory.setServiceUrl(baseURL + "IdGenerator");
		idGeneratorServiceFactory.setServiceInterface(IIdGenerator.class);
		idGeneratorServiceFactory.setHttpInvokerRequestExecutor(authenticatedHTTPRequestExecutor);
		idGeneratorServiceFactory.afterPropertiesSet();

		rsb.queriesService = (IQueriesService) queriesServiceFactory.getObject();
		rsb.streamingService = (IStreamingService) streamingServiceFactory.getObject();
		rsb.longPollingService = (ILongPollingService) longPollingServiceFactory.getObject();
		rsb.idGenerator = (IIdGenerator) idGeneratorServiceFactory.getObject();
	}

}*/