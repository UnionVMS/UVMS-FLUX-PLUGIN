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
package eu.europa.ec.fisheries.uvms.plugins.flux.mapper;

import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetId;
import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetIdList;
import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetIdType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementBaseType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.plugins.flux.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.flux.exception.PluginException;
import eu.europa.ec.fisheries.uvms.plugins.flux.util.DateUtil;
import eu.europa.ec.fisheries.uvms.service.client.flux.movement.contract.BasicAttribute;
import eu.europa.ec.fisheries.uvms.service.client.flux.movement.contract.CodeType;
import eu.europa.ec.fisheries.uvms.service.client.flux.movement.contract.ExchangeDocumentInfoType;
import eu.europa.ec.fisheries.uvms.service.client.flux.movement.contract.FLUXGeographicalCoordinateType;
import eu.europa.ec.fisheries.uvms.service.client.flux.movement.contract.IDType;
import eu.europa.ec.fisheries.uvms.service.client.flux.movement.contract.MeasureType;
import eu.europa.ec.fisheries.uvms.service.client.flux.movement.contract.TextType;
import eu.europa.ec.fisheries.uvms.service.client.flux.movement.contract.VesselIdentificationType;
import eu.europa.ec.fisheries.uvms.service.client.flux.movement.contract.VesselPositionType;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.dom.DOMResult;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;

import eu.europa.ec.fisheries.uvms.service.client.flux.movement.contract.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.xml.datatype.XMLGregorianCalendar;
import xeu.connector_bridge.v1.PostMsgType;

/**
 **/
@LocalBean
@Stateless
public class FluxMessageRequestMapper {

    private static FLUXGeographicalCoordinateType mapToFLUXGeographicalCoordinateType(MovementPoint position) throws PluginException {
        FLUXGeographicalCoordinateType coordinate = new FLUXGeographicalCoordinateType();
        if (position != null) {
            coordinate.setLatitudeMeasure(mapToMeasurType(position.getLatitude()));
            coordinate.setLongitudeMeasure(mapToMeasurType(position.getLongitude()));
        }
        return coordinate;
    }

    @EJB
    StartupBean settings;

    public PostMsgType mapToRequest(ExchangeDocumentInfoType queryType, String messageId) throws JAXBException {

        PostMsgType message = new PostMsgType();
        message.setAD(settings.getSetting("FLUX_AD"));
        message.setDF(settings.getSetting("FLUX_DATAFLOW"));
        message.setID(messageId);

        //Below does not need to be set because the bridge takes care of it
        //Date timeInFuture = DateUtil.getTimeInFuture(1);
        //message.setTODT(DateUtil.createXMLGregorianCalendar(timeInFuture, TimeZone.getTimeZone("UTC")));
        //message.setTO(2);
        //message.setDT(DateUtil.createXMLGregorianCalendar(new Date(), TimeZone.getTimeZone("UTC")));
        //If below is set you override the per-dataflow default set in Bridge
        //message.setVB(VerbosityType.ERROR);
        //message.setAR(false);
        //message.setTS(true);
        BasicAttribute attr = mapToBasicAttribute(queryType, messageId);

        JAXBContext context = JAXBContext.newInstance(BasicAttribute.class);
        Marshaller marshaller = context.createMarshaller();
        DOMResult res = new DOMResult();
        marshaller.marshal(attr, res);

        Element elt = ((Document) res.getNode()).getDocumentElement();

        message.setAny(elt);
        return message;
    }

    public static ExchangeDocumentInfoType mapToExchangeDocumentInfoType(MovementType movement, String editorType, String actionReason) throws PluginException {
        ExchangeDocumentInfoType type = new ExchangeDocumentInfoType();
        type.setVesselID(mapToFluxVesselId(movement));
        type.getVesselPositions().add(mapToVesselpositionType(movement));
        type.setEditorType(mapToTextType(editorType));
        type.setActionReason(mapToTextType(actionReason));
        return type;
    }

    public static TextType mapToTextType(String value) {
        TextType textType = new TextType();
        textType.setValue(value);
        return textType;
    }

    public static VesselPositionType mapToVesselpositionType(MovementType movement) throws PluginException {
        VesselPositionType type = new VesselPositionType();
        if (movement != null) {
            type.setCourse(mapToMeasurType(movement.getReportedCourse()));
            type.setSpeed(mapToMeasurType(movement.getReportedSpeed()));
            type.setObtained(DateUtil.createXMLGregorianCalendar(movement.getPositionTime()).normalize());
            type.setPosition(mapToFLUXGeographicalCoordinateType(movement.getPosition()));
            if (movement.getMovementType() != null) {
                type.setType(mapToCodeType(movement.getMovementType().name()));
            }
        }
        return type;
    }

    public static MeasureType mapToMeasurType(Double value) throws PluginException {
        MeasureType measureType = new MeasureType();
        try {
            measureType.setValue(BigDecimal.valueOf(value));
        } catch (NumberFormatException e) {
            throw new PluginException("Failure when parsing double, Input not a double [ Map To MeasureType ] ");
        } catch (NullPointerException e) {
            throw new PluginException("Failure when parsing double, Nullpointer [ Map To MeasureType ] ");
        }
        return measureType;

    }

    public static VesselIdentificationType mapToFluxVesselId(MovementBaseType movementBaseType) {
        VesselIdentificationType type = new VesselIdentificationType();
        type.setUVI(mapToIDType(movementBaseType.getMmsi()));
        type.setRadioCallSign(mapToIDType(movementBaseType.getIrcs()));
        type.setFlagState(mapToCodeType(movementBaseType.getFlagState()));
        type.setExternalMarking(mapToIDType(movementBaseType.getExternalMarking()));
        return type;
    }

    public static CodeType mapToCodeType(String value) {
        CodeType codeType = new CodeType();
        codeType.setValue(value);
        return codeType;
    }

    public static IDType getVesselIdValue(List<AssetIdList> value, AssetIdType idType) {
        for (AssetIdList id : value) {
            if (id.getIdType().equals(idType)) {
                IDType type = new IDType();
                type.setValue(id.getValue());
                return type;
            }
        }
        return null;
    }

    public static BasicAttribute mapToBasicAttribute(ExchangeDocumentInfoType movement, String messageId) {
        BasicAttribute attr = new BasicAttribute();
        attr.setMessageID(mapToIdType(messageId));
        attr.setExchangeDocumentInfo(movement);
        XMLGregorianCalendar gregCalendar = DateUtil.createXMLGregorianCalendar(new Date());
        attr.setCreation(gregCalendar.normalize());
        return attr;
    }

    public static IDType mapToIdType() {
        IDType idType = new IDType();
        idType.setValue(UUID.randomUUID().toString());
        return idType;
    }

    public static IDType mapToIdType(String value) {
        IDType idType = new IDType();
        idType.setValue(value);
        return idType;
    }

    public static void addHeaderValueToRequest(Object port, final Map<String, String> values) {
        BindingProvider bp = (BindingProvider) port;
        Map<String, Object> context = bp.getRequestContext();

        Map<String, List<String>> headers = new HashMap<>();
        for (Entry entry : values.entrySet()) {
            headers.put(entry.getKey().toString(), Collections.singletonList(entry.getValue().toString()));
        }
        context.put(MessageContext.HTTP_REQUEST_HEADERS, headers);
    }

    public static IDType mapToIDType(String externalMarking){
        IDType idType = new IDType();
        idType.setValue(externalMarking);
        return idType;
    }

}