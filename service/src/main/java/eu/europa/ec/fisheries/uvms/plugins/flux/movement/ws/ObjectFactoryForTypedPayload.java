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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

import xeu.bridge_connector.v1.ResponseType;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the xeu.bridge_connector.v1 package. 
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
public class ObjectFactoryForTypedPayload {

    private final static QName _Connector2BridgeRequest_QNAME = new QName("urn:xeu:bridge-connector:v1", "Connector2BridgeRequest");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: xeu.bridge_connector.v1
     *
     */
    public ObjectFactoryForTypedPayload() {
    }

    /**
     * Create an instance of {@link RequestTypeWithTypedPayload }
     * 
     */
    public RequestTypeWithTypedPayload createRequestType() {
        return new RequestTypeWithTypedPayload();
    }

    @XmlElementDecl(namespace = "urn:xeu:bridge-connector:v1", name = "Connector2BridgeRequest")
    public JAXBElement<RequestTypeWithTypedPayload> createConnector2BridgeRequest(RequestTypeWithTypedPayload value) {
        return new JAXBElement<>(_Connector2BridgeRequest_QNAME, RequestTypeWithTypedPayload.class, null, value);
    }
}
