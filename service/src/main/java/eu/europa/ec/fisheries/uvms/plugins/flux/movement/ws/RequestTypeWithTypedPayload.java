/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.plugins.flux.movement.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import xeu.bridge_connector.v1.VerbosityType;


/**
 * The top level Connector Bridge Envelope.
 * 
 * <p>Java class for RequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;any processContents='skip' namespace='##other'/>
 *       &lt;/sequence>
 *       &lt;attribute name="ON" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="AD" use="required" type="{urn:xeu:bridge-connector:v1}AddressType" />
 *       &lt;attribute name="TODT" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="DF" use="required" type="{urn:xeu:bridge-connector:v1}DataflowType" />
 *       &lt;attribute name="AR" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="TO" type="{urn:xeu:bridge-connector:v1}SyncTimeoutType" />
 *       &lt;attribute name="CT" type="{urn:xeu:bridge-connector:v1}EmailListType" />
 *       &lt;attribute name="VB" type="{urn:xeu:bridge-connector:v1}VerbosityType" />
 *       &lt;anyAttribute processContents='lax'/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RequestType", propOrder = {
        "any"
})
public class RequestTypeWithTypedPayload {

    @XmlAnyElement(lax = true)
    protected Object any;
    @XmlAttribute(name = "ON", required = true)
    protected String on;
    @XmlAttribute(name = "AD", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String ad;
    @XmlAttribute(name = "TODT")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar todt;
    @XmlAttribute(name = "DF", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String df;
    @XmlAttribute(name = "AR")
    protected Boolean ar;
    @XmlAttribute(name = "TO")
    protected Integer to;
    @XmlAttribute(name = "CT")
    protected List<String> ct;
    @XmlAttribute(name = "VB")
    protected VerbosityType vb;
    @XmlAnyAttribute
    private final Map<QName, String> otherAttributes = new HashMap<>();

    /**
     * Gets the value of the any property.
     * 
     * @return
     *     possible object is
     *     {@link Element }
     *     
     */
    public Object getAny() {
        return any;
    }

    /**
     * Sets the value of the any property.
     * 
     * @param value
     *     allowed object is
     *     {@link Element }
     *     
     */
    public void setAny(Object value) {
        this.any = value;
    }

    /**
     * Gets the value of the on property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getON() {
        return on;
    }

    /**
     * Sets the value of the on property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setON(String value) {
        this.on = value;
    }

    /**
     * Gets the value of the ad property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAD() {
        return ad;
    }

    /**
     * Sets the value of the ad property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAD(String value) {
        this.ad = value;
    }

    /**
     * Gets the value of the todt property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTODT() {
        return todt;
    }

    /**
     * Sets the value of the todt property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTODT(XMLGregorianCalendar value) {
        this.todt = value;
    }

    /**
     * Gets the value of the df property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDF() {
        return df;
    }

    /**
     * Sets the value of the df property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDF(String value) {
        this.df = value;
    }

    /**
     * Gets the value of the ar property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAR() {
        return ar;
    }

    /**
     * Sets the value of the ar property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAR(Boolean value) {
        this.ar = value;
    }

    /**
     * Gets the value of the to property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getTO() {
        return to;
    }

    /**
     * Sets the value of the to property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setTO(Integer value) {
        this.to = value;
    }

    /**
     * Gets the value of the ct property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ct property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCT().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getCT() {
        if (ct == null) {
            ct = new ArrayList<String>();
        }
        return this.ct;
    }

    /**
     * Gets the value of the vb property.
     * 
     * @return
     *     possible object is
     *     {@link VerbosityType }
     *     
     */
    public VerbosityType getVB() {
        return vb;
    }

    /**
     * Sets the value of the vb property.
     * 
     * @param value
     *     allowed object is
     *     {@link VerbosityType }
     *     
     */
    public void setVB(VerbosityType value) {
        this.vb = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

}