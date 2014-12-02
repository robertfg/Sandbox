
package com.anz.rer.etl.webservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for rwhInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="rwhInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="geographyHierarchyPath" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="instrument" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="instrumentCCY" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="legalEntityHierarchyPath" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="mxFamily" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="mxGroup" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="mxType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="portfolio" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="portfolioHierarchyPath" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="positionID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="RWHCache">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="entry" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="key" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *                             &lt;element name="value" type="{http://webservice.etl.rer.anz.com/}rwhInfo" minOccurs="0"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "rwhInfo", propOrder = {
    "geographyHierarchyPath",
    "instrument",
    "instrumentCCY",
    "legalEntityHierarchyPath",
    "mxFamily",
    "mxGroup",
    "mxType",
    "portfolio",
    "portfolioHierarchyPath",
    "positionID",
    "rwhCache"
})
public class RwhInfo {

    protected String geographyHierarchyPath;
    protected String instrument;
    protected String instrumentCCY;
    protected String legalEntityHierarchyPath;
    protected String mxFamily;
    protected String mxGroup;
    protected String mxType;
    protected String portfolio;
    protected String portfolioHierarchyPath;
    protected int positionID;
    @XmlElement(name = "RWHCache", required = true)
    protected RwhInfo.RWHCache rwhCache;

    /**
     * Gets the value of the geographyHierarchyPath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGeographyHierarchyPath() {
        return geographyHierarchyPath;
    }

    /**
     * Sets the value of the geographyHierarchyPath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGeographyHierarchyPath(String value) {
        this.geographyHierarchyPath = value;
    }

    /**
     * Gets the value of the instrument property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInstrument() {
        return instrument;
    }

    /**
     * Sets the value of the instrument property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInstrument(String value) {
        this.instrument = value;
    }

    /**
     * Gets the value of the instrumentCCY property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInstrumentCCY() {
        return instrumentCCY;
    }

    /**
     * Sets the value of the instrumentCCY property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInstrumentCCY(String value) {
        this.instrumentCCY = value;
    }

    /**
     * Gets the value of the legalEntityHierarchyPath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLegalEntityHierarchyPath() {
        return legalEntityHierarchyPath;
    }

    /**
     * Sets the value of the legalEntityHierarchyPath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLegalEntityHierarchyPath(String value) {
        this.legalEntityHierarchyPath = value;
    }

    /**
     * Gets the value of the mxFamily property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMxFamily() {
        return mxFamily;
    }

    /**
     * Sets the value of the mxFamily property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMxFamily(String value) {
        this.mxFamily = value;
    }

    /**
     * Gets the value of the mxGroup property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMxGroup() {
        return mxGroup;
    }

    /**
     * Sets the value of the mxGroup property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMxGroup(String value) {
        this.mxGroup = value;
    }

    /**
     * Gets the value of the mxType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMxType() {
        return mxType;
    }

    /**
     * Sets the value of the mxType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMxType(String value) {
        this.mxType = value;
    }

    /**
     * Gets the value of the portfolio property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPortfolio() {
        return portfolio;
    }

    /**
     * Sets the value of the portfolio property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPortfolio(String value) {
        this.portfolio = value;
    }

    /**
     * Gets the value of the portfolioHierarchyPath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPortfolioHierarchyPath() {
        return portfolioHierarchyPath;
    }

    /**
     * Sets the value of the portfolioHierarchyPath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPortfolioHierarchyPath(String value) {
        this.portfolioHierarchyPath = value;
    }

    /**
     * Gets the value of the positionID property.
     * 
     */
    public int getPositionID() {
        return positionID;
    }

    /**
     * Sets the value of the positionID property.
     * 
     */
    public void setPositionID(int value) {
        this.positionID = value;
    }

    /**
     * Gets the value of the rwhCache property.
     * 
     * @return
     *     possible object is
     *     {@link RwhInfo.RWHCache }
     *     
     */
    public RwhInfo.RWHCache getRWHCache() {
        return rwhCache;
    }

    /**
     * Sets the value of the rwhCache property.
     * 
     * @param value
     *     allowed object is
     *     {@link RwhInfo.RWHCache }
     *     
     */
    public void setRWHCache(RwhInfo.RWHCache value) {
        this.rwhCache = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="entry" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="key" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
     *                   &lt;element name="value" type="{http://webservice.etl.rer.anz.com/}rwhInfo" minOccurs="0"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "entry"
    })
    public static class RWHCache {

        protected List<RwhInfo.RWHCache.Entry> entry;

        /**
         * Gets the value of the entry property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the entry property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getEntry().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link RwhInfo.RWHCache.Entry }
         * 
         * 
         */
        public List<RwhInfo.RWHCache.Entry> getEntry() {
            if (entry == null) {
                entry = new ArrayList<RwhInfo.RWHCache.Entry>();
            }
            return this.entry;
        }


        /**
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;sequence>
         *         &lt;element name="key" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
         *         &lt;element name="value" type="{http://webservice.etl.rer.anz.com/}rwhInfo" minOccurs="0"/>
         *       &lt;/sequence>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "key",
            "value"
        })
        public static class Entry {

            protected Integer key;
            protected RwhInfo value;

            /**
             * Gets the value of the key property.
             * 
             * @return
             *     possible object is
             *     {@link Integer }
             *     
             */
            public Integer getKey() {
                return key;
            }

            /**
             * Sets the value of the key property.
             * 
             * @param value
             *     allowed object is
             *     {@link Integer }
             *     
             */
            public void setKey(Integer value) {
                this.key = value;
            }

            /**
             * Gets the value of the value property.
             * 
             * @return
             *     possible object is
             *     {@link RwhInfo }
             *     
             */
            public RwhInfo getValue() {
                return value;
            }

            /**
             * Sets the value of the value property.
             * 
             * @param value
             *     allowed object is
             *     {@link RwhInfo }
             *     
             */
            public void setValue(RwhInfo value) {
                this.value = value;
            }

        }

    }

}
