package com.anz.rer.etl.webservice;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 2.7.0
 * 2013-04-16T10:53:59.615+08:00
 * Generated source version: 2.7.0
 * 
 */
@WebServiceClient(name = "RWHServiceImplService", 
                  wsdlLocation = "http://10.52.16.1:10811/ANZEtlApp-2.0.0-SNAPSHOT/RWHService?wsdl",
                  targetNamespace = "http://webservice.etl.rer.anz.com/") 
public class RWHServiceImplService extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://webservice.etl.rer.anz.com/", "RWHServiceImplService");
    public final static QName RWHServiceImplPort = new QName("http://webservice.etl.rer.anz.com/", "RWHServiceImplPort");
    static {
        URL url = null;
        try {
            url = new URL("http://10.52.16.1:10811/ANZEtlApp-2.0.0-SNAPSHOT/RWHService?wsdl");
        } catch (MalformedURLException e) {
            java.util.logging.Logger.getLogger(RWHServiceImplService.class.getName())
                .log(java.util.logging.Level.INFO, 
                     "Can not initialize the default wsdl from {0}", "http://10.52.16.1:10811/ANZEtlApp-2.0.0-SNAPSHOT/RWHService?wsdl");
        }
        WSDL_LOCATION = url;
    }

    public RWHServiceImplService(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public RWHServiceImplService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public RWHServiceImplService() {
        super(WSDL_LOCATION, SERVICE);
    }
    

    /**
     *
     * @return
     *     returns RWHService
     */
    @WebEndpoint(name = "RWHServiceImplPort")
    public RWHService getRWHServiceImplPort() {
        return super.getPort(RWHServiceImplPort, RWHService.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns RWHService
     */
    @WebEndpoint(name = "RWHServiceImplPort")
    public RWHService getRWHServiceImplPort(WebServiceFeature... features) {
        return super.getPort(RWHServiceImplPort, RWHService.class, features);
    }

}
