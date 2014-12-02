
package com.anz.rer.etl.webservice;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.anz.rer.etl.webservice package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _GetBusinessDateResponse_QNAME = new QName("http://webservice.etl.rer.anz.com/", "getBusinessDateResponse");
    private final static QName _GetRWInfoByPositionIDResponse_QNAME = new QName("http://webservice.etl.rer.anz.com/", "getRWInfoByPositionIDResponse");
    private final static QName _GetRiskWareHouseCache_QNAME = new QName("http://webservice.etl.rer.anz.com/", "getRiskWareHouseCache");
    private final static QName _GetRiskWareHouseCacheResponse_QNAME = new QName("http://webservice.etl.rer.anz.com/", "getRiskWareHouseCacheResponse");
    private final static QName _GetRWHCache_QNAME = new QName("http://webservice.etl.rer.anz.com/", "getRWHCache");
    private final static QName _GetBusinessDate_QNAME = new QName("http://webservice.etl.rer.anz.com/", "getBusinessDate");
    private final static QName _GetRWHCacheResponse_QNAME = new QName("http://webservice.etl.rer.anz.com/", "getRWHCacheResponse");
    private final static QName _GetRWInfoByPositionID_QNAME = new QName("http://webservice.etl.rer.anz.com/", "getRWInfoByPositionID");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.anz.rer.etl.webservice
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link RwhInfo }
     * 
     */
    public RwhInfo createRwhInfo() {
        return new RwhInfo();
    }

    /**
     * Create an instance of {@link RwhInfo.RWHCache }
     * 
     */
    public RwhInfo.RWHCache createRwhInfoRWHCache() {
        return new RwhInfo.RWHCache();
    }

    /**
     * Create an instance of {@link GetRWHCacheResponse }
     * 
     */
    public GetRWHCacheResponse createGetRWHCacheResponse() {
        return new GetRWHCacheResponse();
    }

    /**
     * Create an instance of {@link GetRWHCacheResponse.Return }
     * 
     */
    public GetRWHCacheResponse.Return createGetRWHCacheResponseReturn() {
        return new GetRWHCacheResponse.Return();
    }

    /**
     * Create an instance of {@link GetRWInfoByPositionID }
     * 
     */
    public GetRWInfoByPositionID createGetRWInfoByPositionID() {
        return new GetRWInfoByPositionID();
    }

    /**
     * Create an instance of {@link GetRWInfoByPositionIDResponse }
     * 
     */
    public GetRWInfoByPositionIDResponse createGetRWInfoByPositionIDResponse() {
        return new GetRWInfoByPositionIDResponse();
    }

    /**
     * Create an instance of {@link GetBusinessDateResponse }
     * 
     */
    public GetBusinessDateResponse createGetBusinessDateResponse() {
        return new GetBusinessDateResponse();
    }

    /**
     * Create an instance of {@link GetRiskWareHouseCache }
     * 
     */
    public GetRiskWareHouseCache createGetRiskWareHouseCache() {
        return new GetRiskWareHouseCache();
    }

    /**
     * Create an instance of {@link GetBusinessDate }
     * 
     */
    public GetBusinessDate createGetBusinessDate() {
        return new GetBusinessDate();
    }

    /**
     * Create an instance of {@link GetRiskWareHouseCacheResponse }
     * 
     */
    public GetRiskWareHouseCacheResponse createGetRiskWareHouseCacheResponse() {
        return new GetRiskWareHouseCacheResponse();
    }

    /**
     * Create an instance of {@link GetRWHCache }
     * 
     */
    public GetRWHCache createGetRWHCache() {
        return new GetRWHCache();
    }

    /**
     * Create an instance of {@link ConcurrentHashMap }
     * 
     */
    public ConcurrentHashMap createConcurrentHashMap() {
        return new ConcurrentHashMap();
    }

    /**
     * Create an instance of {@link RwhInfo.RWHCache.Entry }
     * 
     */
    public RwhInfo.RWHCache.Entry createRwhInfoRWHCacheEntry() {
        return new RwhInfo.RWHCache.Entry();
    }

    /**
     * Create an instance of {@link GetRWHCacheResponse.Return.Entry }
     * 
     */
    public GetRWHCacheResponse.Return.Entry createGetRWHCacheResponseReturnEntry() {
        return new GetRWHCacheResponse.Return.Entry();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetBusinessDateResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.etl.rer.anz.com/", name = "getBusinessDateResponse")
    public JAXBElement<GetBusinessDateResponse> createGetBusinessDateResponse(GetBusinessDateResponse value) {
        return new JAXBElement<GetBusinessDateResponse>(_GetBusinessDateResponse_QNAME, GetBusinessDateResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetRWInfoByPositionIDResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.etl.rer.anz.com/", name = "getRWInfoByPositionIDResponse")
    public JAXBElement<GetRWInfoByPositionIDResponse> createGetRWInfoByPositionIDResponse(GetRWInfoByPositionIDResponse value) {
        return new JAXBElement<GetRWInfoByPositionIDResponse>(_GetRWInfoByPositionIDResponse_QNAME, GetRWInfoByPositionIDResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetRiskWareHouseCache }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.etl.rer.anz.com/", name = "getRiskWareHouseCache")
    public JAXBElement<GetRiskWareHouseCache> createGetRiskWareHouseCache(GetRiskWareHouseCache value) {
        return new JAXBElement<GetRiskWareHouseCache>(_GetRiskWareHouseCache_QNAME, GetRiskWareHouseCache.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetRiskWareHouseCacheResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.etl.rer.anz.com/", name = "getRiskWareHouseCacheResponse")
    public JAXBElement<GetRiskWareHouseCacheResponse> createGetRiskWareHouseCacheResponse(GetRiskWareHouseCacheResponse value) {
        return new JAXBElement<GetRiskWareHouseCacheResponse>(_GetRiskWareHouseCacheResponse_QNAME, GetRiskWareHouseCacheResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetRWHCache }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.etl.rer.anz.com/", name = "getRWHCache")
    public JAXBElement<GetRWHCache> createGetRWHCache(GetRWHCache value) {
        return new JAXBElement<GetRWHCache>(_GetRWHCache_QNAME, GetRWHCache.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetBusinessDate }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.etl.rer.anz.com/", name = "getBusinessDate")
    public JAXBElement<GetBusinessDate> createGetBusinessDate(GetBusinessDate value) {
        return new JAXBElement<GetBusinessDate>(_GetBusinessDate_QNAME, GetBusinessDate.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetRWHCacheResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.etl.rer.anz.com/", name = "getRWHCacheResponse")
    public JAXBElement<GetRWHCacheResponse> createGetRWHCacheResponse(GetRWHCacheResponse value) {
        return new JAXBElement<GetRWHCacheResponse>(_GetRWHCacheResponse_QNAME, GetRWHCacheResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetRWInfoByPositionID }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.etl.rer.anz.com/", name = "getRWInfoByPositionID")
    public JAXBElement<GetRWInfoByPositionID> createGetRWInfoByPositionID(GetRWInfoByPositionID value) {
        return new JAXBElement<GetRWInfoByPositionID>(_GetRWInfoByPositionID_QNAME, GetRWInfoByPositionID.class, null, value);
    }

}
