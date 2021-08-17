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
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.constants.Codes;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.constants.Codes.FLUXVesselPositionType;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.exception.MappingException;
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
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.*;
import java.util.Map.Entry;

public class FluxMessageRequestMapper {

    private static final String PURPOSE_CODE = "9";
    private static final String DOCUMENT_ID_SCHEME_ID = "UUID";
    private static final String PURPOSE_CODE_LIST_ID = "FLUX_GP_PURPOSE";
    private static final String FLUX_PARTY_SCHEME_ID = "FLUX_GP_PARTY";
    private static final String VESSEL_COUNTRY_SCHEME_ID = "TERRITORY";
    private static final String POSITION_TYPE_CODE_LIST_ID = "FLUX_VESSEL_POSITION_TYPE";

    private static final Logger LOG = LoggerFactory.getLogger(FluxMessageRequestMapper.class);

    private static final NumberFormat decimalFormatter;
    private static final NumberFormat coordFormatter;

    static {
        decimalFormatter = NumberFormat.getInstance(Locale.ENGLISH);
        decimalFormatter.setRoundingMode(RoundingMode.HALF_UP);
        decimalFormatter.setMaximumFractionDigits(2);
        coordFormatter = NumberFormat.getInstance(Locale.ENGLISH);
        coordFormatter.setRoundingMode(RoundingMode.HALF_UP);
        coordFormatter.setMinimumFractionDigits(3);
        coordFormatter.setMaximumFractionDigits(6);
    }

    private FluxMessageRequestMapper() {}

    public static PostMsgType mapToRequest(FLUXVesselPositionMessage vesselPositionMessage, String messageId, String recipient, String dataflow, XMLGregorianCalendar todt, String defaultAd) throws JAXBException {
        PostMsgType message = new PostMsgType();
        if (recipient == null || recipient.isEmpty()) {
            message.setAD(defaultAd);
        } else {
            message.setAD(recipient);
        }
        message.setDF(dataflow);
        message.setID(messageId);
        message.setTODT(todt);
        //Below does not need to be set because the bridge takes care of it
        //message.setTO(2);
        //message.setDT(DateUtil.createXMLGregorianCalendar(new Date(), TimeZone.getTimeZone("UTC")));
        //If below is set you override the per-dataflow default set in Bridge
        //message.setVB(VerbosityType.ERROR);
        //message.setAR(false);
        //message.setTS(true);
        JAXBContext context = JAXBContext.newInstance(FLUXVesselPositionMessage.class);
        Marshaller marshaller = context.createMarshaller();
        DOMResult res = new DOMResult();
        marshaller.marshal(vesselPositionMessage, res);
        Element elt = ((Document) res.getNode()).getDocumentElement();
        message.setAny(elt);
        return message;
    }

    public static FLUXVesselPositionMessage mapToFluxMovement(MovementType movement, String ownerFluxParty) throws MappingException {
        FLUXVesselPositionMessage msg = new FLUXVesselPositionMessage();
        msg.setFLUXReportDocument(mapToReportDocument(ownerFluxParty));
        msg.setVesselTransportMeans(mapToVesselTransportMeans(movement));
        return msg;
    }

    private static FLUXPartyType mapToFluxPartyType(String ad) {
        FLUXPartyType partyType = new FLUXPartyType();
        partyType.getIDS().add(mapToIdType(ad, FLUX_PARTY_SCHEME_ID));
        return partyType;
    }

    private static FLUXReportDocumentType mapToReportDocument(String fluxOwner) {
        FLUXReportDocumentType doc = new FLUXReportDocumentType();
        doc.getIDS().add(mapToIdType(UUID.randomUUID().toString(), DOCUMENT_ID_SCHEME_ID));
        doc.setCreationDateTime(mapToNowDateTime());
        doc.setPurposeCode(mapToCodeType(PURPOSE_CODE, PURPOSE_CODE_LIST_ID));
        doc.setOwnerFLUXParty(mapToFluxPartyType(fluxOwner));
        return doc;
    }

    public static void addHeaderValueToRequest(Object port, final Map<String, String> values) {
        BindingProvider bp = (BindingProvider) port;
        Map<String, List<String>> headers = new HashMap<>();
        for (Entry<String, String> entry : values.entrySet()) {
            headers.put(entry.getKey(), Collections.singletonList(entry.getValue()));
        }
        bp.getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS, headers);
    }

    private static VesselTransportMeansType mapToVesselTransportMeans(MovementType movement) throws MappingException {
        VesselTransportMeansType retVal = new VesselTransportMeansType();
        //Handle Asset ID
        Map<AssetIdType, String> ids = new HashMap<>();
        for (AssetIdList col : movement.getAssetId().getAssetIdList()) {
            ids.put(col.getIdType(), col.getValue());
        }
        if (ids.containsKey(AssetIdType.IRCS) && movement.getIrcs() != null && !movement.getIrcs().equals(ids.get(AssetIdType.IRCS))) {
            throw new MappingException("Asset IRCS does not match when mapping AssetID ( There are 2 ways of getting Ircs in this object! :( and they do not match ) " + movement.getIrcs() + ":" +ids.get(AssetIdType.IRCS));
        }
        if (movement.getIrcs() != null) {
            retVal.getIDS().add(mapToVesselIDType(Codes.FLUXVesselIDType.IRCS, movement.getIrcs().replace("-", "")));
        }
        if (movement.getExternalMarking() != null) {
            retVal.getIDS().add(mapToVesselIDType(Codes.FLUXVesselIDType.EXT_MARK, movement.getExternalMarking().replace("-", "")));
        }
        if (ids.containsKey(AssetIdType.CFR)) {
            retVal.getIDS().add(mapToVesselIDType(Codes.FLUXVesselIDType.CFR, ids.get(AssetIdType.CFR)));
        }
        if (ids.containsKey(AssetIdType.IMO)) {
            retVal.getIDS().add(mapToVesselIDType(Codes.FLUXVesselIDType.UVI, ids.get(AssetIdType.IMO)));
        }
        //End handle Asset Id
        retVal.setRegistrationVesselCountry(mapToVesselCountry(movement.getFlagState()));
        retVal.getSpecifiedVesselPositionEvents().add(mapToVesselPosition(movement));
        return retVal;
    }

    private static CodeType mapToCodeType(String value, String listId) {
        CodeType codeType = new CodeType();
        codeType.setListID(listId);
        codeType.setValue(value);
        return codeType;
    }

    private static IDType mapToVesselIDType(Codes.FLUXVesselIDType vesselIdType, String value) {
        IDType idType = new IDType();
        idType.setSchemeID(vesselIdType.name());
        idType.setValue(value);
        return idType;
    }

    private static VesselCountryType mapToVesselCountry(String countryCode) {
        VesselCountryType vesselCountry = new VesselCountryType();
        vesselCountry.setID(mapToIdType(countryCode, VESSEL_COUNTRY_SCHEME_ID));
        return vesselCountry;
    }

    private static IDType mapToIdType(String value, String schemeId) {
        IDType id = new IDType();
        id.setSchemeID(schemeId);
        id.setValue(value);
        return id;
    }

    private static VesselPositionEventType mapToVesselPosition(MovementType movement) {
        VesselPositionEventType position = new VesselPositionEventType();
        position.setObtainedOccurrenceDateTime(getXmlGregorianTime(movement.getPositionTime()));
        if (movement.getReportedCourse() != null) {
            position.setCourseValueMeasure(mapToMeasureType(movement.getReportedCourse(), decimalFormatter));
        }
        if (movement.getReportedSpeed() != null) {
            position.setSpeedValueMeasure(mapToMeasureType(movement.getReportedSpeed(), decimalFormatter));
        }
        position.setTypeCode(mapToCodeType(FLUXVesselPositionType.fromInternal(movement.getMovementType()), POSITION_TYPE_CODE_LIST_ID));
        position.setSpecifiedVesselGeographicalCoordinate(mapToGeoPos(movement.getPosition()));
        return position;
    }

    private static DateTimeType getXmlGregorianTime(Date date) {
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

    private static MeasureType mapToMeasureType(Double value, NumberFormat numberFormat) {
        MeasureType measureType = new MeasureType();
        measureType.setValue(new BigDecimal(numberFormat.format(value)));
        return measureType;
    }

    private static DateTimeType mapToNowDateTime() {
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

    private static VesselGeographicalCoordinateType mapToGeoPos(MovementPoint point) {
        VesselGeographicalCoordinateType geoType = new VesselGeographicalCoordinateType();
        geoType.setLatitudeMeasure(mapToMeasureType(point.getLatitude(), coordFormatter));
        geoType.setLongitudeMeasure(mapToMeasureType(point.getLongitude(), coordFormatter));
        return geoType;
    }
}
