/*
 * ﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
 * © European Union, 2015-2016.
 *
 * This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
 * the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

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
package eu.europa.ec.fisheries.uvms.plugins.flux.movement.mapper;

import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetIdList;
import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetIdType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.commons.date.DateUtils;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.constants.MovementPluginConstants;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.exception.MappingException;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.exception.PluginException;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.service.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import un.unece.uncefact.data.standard.fluxvesselpositionmessage._4.FLUXVesselPositionMessage;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._18.*;
import un.unece.uncefact.data.standard.unqualifieddatatype._18.CodeType;
import un.unece.uncefact.data.standard.unqualifieddatatype._18.DateTimeType;
import un.unece.uncefact.data.standard.unqualifieddatatype._18.IDType;
import un.unece.uncefact.data.standard.unqualifieddatatype._18.MeasureType;
import xeu.connector_bridge.v1.PostMsgType;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.dom.DOMResult;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

/**
 *
 */
@LocalBean
@Stateless
public class FluxMessageRequestMapper {

    private static final String MOVEMENT_TYPE = "POS";
    private static final String PURPOSE_CODE = "9";

    private static final Logger LOG = LoggerFactory.getLogger(FluxMessageRequestMapper.class);

    @EJB
    private StartupBean startupBean;

    public PostMsgType mapToRequest(MovementType movement, String messageId, String recipient) throws JAXBException, MappingException {
        PostMsgType message = new PostMsgType();
        if (recipient == null || recipient.isEmpty()) {
            message.setAD(startupBean.getSetting(MovementPluginConstants.FLUX_DEFAULT_AD));
        } else {
            message.setAD(recipient);
        }
        message.setDF(startupBean.getSetting(MovementPluginConstants.FLUX_DATAFLOW));
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
        FLUXVesselPositionMessage attr = mapToFluxMovement(movement, startupBean.getSetting(MovementPluginConstants.OWNER_FLUX_PARTY));
        JAXBContext context = JAXBContext.newInstance(FLUXVesselPositionMessage.class);
        Marshaller marshaller = context.createMarshaller();
        DOMResult res = new DOMResult();
        marshaller.marshal(attr, res);
        Element elt = ((Document) res.getNode()).getDocumentElement();
        message.setAny(elt);
        return message;
    }

    private FLUXVesselPositionMessage mapToFluxMovement(MovementType movement, String fluxOwner) throws MappingException {
        FLUXVesselPositionMessage msg = new FLUXVesselPositionMessage();
        msg.setFLUXReportDocument(mapToReportDocument(fluxOwner, movement.getInternalReferenceNumber()));
        msg.setVesselTransportMeans(mapToVesselTransportMeans(movement));
        return msg;
    }

    private FLUXGeographicalCoordinateType mapToFLUXGeographicalCoordinateType(MovementPoint position) throws PluginException {
        FLUXGeographicalCoordinateType coordinate = new FLUXGeographicalCoordinateType();
        if (position != null) {
            coordinate.setLatitudeMeasure(mapToMeasureType(position.getLatitude()));
            coordinate.setLongitudeMeasure(mapToMeasureType(position.getLongitude()));
        }
        return coordinate;
    }

    private FLUXPartyType mapToFluxPartyType(String ad) {
        FLUXPartyType partyType = new FLUXPartyType();
        partyType.getIDS().add(mapToIdType(ad));
        return partyType;
    }

    private FLUXReportDocumentType mapToReportDocument(String fluxOwner, String referenceNumber) {
        FLUXReportDocumentType doc = new FLUXReportDocumentType();
        if (referenceNumber == null) {
            doc.getIDS().add(mapToGUIDIDType());
        } else {
            doc.getIDS().add(mapToIdType(referenceNumber));
        }
        doc.setCreationDateTime(mapToNowDateTime());
        doc.setPurposeCode(mapToCodeType(PURPOSE_CODE));
        doc.setOwnerFLUXParty(mapToFluxPartyType(fluxOwner));
        return doc;
    }

    public void addHeaderValueToRequest(Object port, final Map<String, String> values) {
        BindingProvider bp = (BindingProvider) port;
        Map<String, List<String>> headers = new HashMap<>();
        for (Entry entry : values.entrySet()) {
            headers.put(entry.getKey().toString(), Collections.singletonList(entry.getValue().toString()));
        }
        bp.getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS, headers);
    }

    private VesselTransportMeansType mapToVesselTransportMeans(MovementType movement) throws MappingException {
        VesselTransportMeansType retVal = new VesselTransportMeansType();
        //Handle Asset ID
        Map<String, String> ids = new HashMap<>();
        for (AssetIdList col : movement.getAssetId().getAssetIdList()) {
            ids.put(col.getIdType().name(), col.getValue());
        }
        if (ids.containsKey(AssetIdType.IRCS.name()) && movement.getIrcs() != null) {
            if (!movement.getIrcs().equals(ids.get(AssetIdType.IRCS.name()))) {
                throw new MappingException("Asset IRCS does not match when mapping AssetID ( There are 2 ways of getting Ircs in this object! :( and they do not match ) " + movement.getIrcs() + ":" +ids.get(AssetIdType.IRCS.name()));
            }
        }
        if (movement.getIrcs() != null) {
            retVal.getIDS().add(mapToVesselIDType(VESSEL_ID_TYPE.IRCS, movement.getIrcs()));
        }
        if (movement.getExternalMarking() != null) {
            retVal.getIDS().add(mapToVesselIDType(VESSEL_ID_TYPE.EXT_MARKING, movement.getExternalMarking()));
        }
        if (ids.containsKey(AssetIdType.CFR.name())) {
            retVal.getIDS().add(mapToVesselIDType(VESSEL_ID_TYPE.CFR, ids.get(AssetIdType.CFR.name())));
        }
        //End handle Asset Id
        retVal.setRegistrationVesselCountry(mapToVesselCountry(movement.getFlagState()));
        retVal.getSpecifiedVesselPositionEvents().add(mapToVesselPosition(movement));
        return retVal;
    }

    private CodeType mapToCodeType(String value) {
        CodeType codeType = new CodeType();
        codeType.setValue(value);
        return codeType;
    }

    private IDType mapToVesselIDType(VESSEL_ID_TYPE vesselIdType, String value) {
        IDType idType = new IDType();
        idType.setSchemeID(vesselIdType.name());
        idType.setValue(value);
        return idType;
    }

    private VesselCountryType mapToVesselCountry(String countryCode) {
        VesselCountryType vesselCountry = new VesselCountryType();
        vesselCountry.setID(mapToIdType(countryCode));
        return vesselCountry;
    }

    private IDType mapToIdType(String value) {
        IDType id = new IDType();
        id.setValue(value);
        return id;
    }

    private IDType mapToGUIDIDType() {
        IDType idType = new IDType();
        idType.setValue(UUID.randomUUID().toString());
        return idType;
    }

    private VesselPositionEventType mapToVesselPosition(MovementType movement) {
        VesselPositionEventType position = new VesselPositionEventType();
        position.setObtainedOccurrenceDateTime(getXmlGregorianTime(movement.getPositionTime()));
        position.setCourseValueMeasure(mapToMeasureType(movement.getReportedCourse()));
        position.setSpeedValueMeasure(mapToMeasureType(movement.getReportedSpeed()));
        position.setTypeCode(mapToCodeType(movement.getMovementType().name()));
        position.setSpecifiedVesselGeographicalCoordinate(mapToGeoPos(movement.getPosition()));
        return position;
    }

    private DateTimeType getXmlGregorianTime(Date date) {
        DateTimeType dateTimeType = new DateTimeType();
        try {
            GregorianCalendar c = new GregorianCalendar();
            c.setTime(date);
            dateTimeType.setDateTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
        } catch (DatatypeConfigurationException ex) {
            LOG.error("Error while parsing date {} to xml gregorian calendar in FLUX. Error: {}", date, ex);
        }
        return dateTimeType;
    }

    private MeasureType mapToMeasureType(Double measuredSpeed) {
        MeasureType measureType = new MeasureType();
        measureType.setValue(BigDecimal.valueOf(measuredSpeed));
        return measureType;
    }

    private DateTimeType mapToNowDateTime() {
        try {
            DateTimeType date = new DateTimeType();
            GregorianCalendar c = new GregorianCalendar();
            c.setTime(new Date());
            date.setDateTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
            return date;
        } catch (DatatypeConfigurationException ex) {
            return new DateTimeType();
        }
    }

    private VesselGeographicalCoordinateType mapToGeoPos(MovementPoint point) {
        VesselGeographicalCoordinateType geoType = new VesselGeographicalCoordinateType();
        geoType.setLatitudeMeasure(mapToMeasureType(point.getLatitude()));
        geoType.setLongitudeMeasure(mapToMeasureType(point.getLongitude()));
        if (point.getAltitude() != null) {
            geoType.setAltitudeMeasure(mapToMeasureType(point.getAltitude()));
        }
        return geoType;
    }

    private enum VESSEL_ID_TYPE {

        CFR, EXT_MARKING, IRCS;
    }
}
