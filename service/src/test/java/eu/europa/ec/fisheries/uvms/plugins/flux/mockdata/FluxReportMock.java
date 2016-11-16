/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europa.ec.fisheries.uvms.plugins.flux.mockdata;

import java.math.BigDecimal;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.dom.DOMResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import un.unece.uncefact.data.standard.fluxvesselpositionmessage._4.FLUXVesselPositionMessage;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._18.FLUXPartyType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._18.FLUXReportDocumentType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._18.VesselCountryType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._18.VesselGeographicalCoordinateType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._18.VesselPositionEventType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._18.VesselTransportMeansType;
import un.unece.uncefact.data.standard.unqualifieddatatype._18.CodeType;
import un.unece.uncefact.data.standard.unqualifieddatatype._18.DateTimeType;
import un.unece.uncefact.data.standard.unqualifieddatatype._18.IDType;
import un.unece.uncefact.data.standard.unqualifieddatatype._18.MeasureType;
import un.unece.uncefact.data.standard.unqualifieddatatype._18.TextType;
import xeu.bridge_connector.v1.RequestType;

/**
 *
 * @author jojoha
 */
public class FluxReportMock {

    public static RequestType mapToResponseType() throws JAXBException {
        RequestType responseType = new RequestType();
        responseType.setAny(mapToElement());
        return responseType;
    }

    private static Element mapToElement() throws JAXBException {
        FLUXVesselPositionMessage attr = mapToFLUXReportDocumentType();
        JAXBContext context = JAXBContext.newInstance(FLUXVesselPositionMessage.class);
        Marshaller marshaller = context.createMarshaller();
        DOMResult res = new DOMResult();
        marshaller.marshal(attr, res);
        return ((Document) res.getNode()).getDocumentElement();
    }

    private static FLUXVesselPositionMessage mapToFLUXReportDocumentType() {
        FLUXVesselPositionMessage message = new FLUXVesselPositionMessage();
        message.setFLUXReportDocument(mapToFluxDocumentType());
        message.setVesselTransportMeans(mapToVesselTransportMeans());
        return message;
    }

    private static FLUXReportDocumentType mapToFluxDocumentType() {
        FLUXReportDocumentType message = new FLUXReportDocumentType();
        message.getIDS().add(mapToIDType(MockConstants.GUID_ID));
        message.setCreationDateTime(mepToDateTimeTypeNow());
        message.setPurposeCode(mapToCodeType(MockConstants.PURPOSE_CODE));
        message.setOwnerFLUXParty(mapToOwnerFluxParty());
        return message;
    }

    private static VesselTransportMeansType mapToVesselTransportMeans() {
        VesselTransportMeansType movement = new VesselTransportMeansType();
        movement.setRegistrationVesselCountry(mapToVesselCountry());
        movement.getIDS().add(mapToIDType(MockConstants.ASSET_CFR, MockConstants.ASSET_CFR));
        movement.getIDS().add(mapToIDType(MockConstants.ASSET_IRCS, MockConstants.ASSET_IRCS));
        movement.getIDS().add(mapToIDType(MockConstants.ASSET_EXT_MARKING, MockConstants.ASSET_EXT_MARKING));
        movement.getSpecifiedVesselPositionEvents().add(mapToVesselPostionType());
        return movement;
    }

    private static VesselPositionEventType mapToVesselPostionType() {
        VesselPositionEventType position = new VesselPositionEventType();
        position.setObtainedOccurrenceDateTime(mepToDateTimeTypeNow());
        position.setTypeCode(mapToCodeType(MockConstants.POSITION_TYPE));
        position.setSpeedValueMeasure(mapToMeasuerType(MockConstants.REPORTED_SPEED));
        position.setCourseValueMeasure(mapToMeasuerType(MockConstants.REPORTED_COURSE));
        position.setSpecifiedVesselGeographicalCoordinate(mapToCoordinate());
        return position;
    }

    private static VesselGeographicalCoordinateType mapToCoordinate() {
        VesselGeographicalCoordinateType coords = new VesselGeographicalCoordinateType();
        coords.setAltitudeMeasure(mapToMeasuerType(MockConstants.ALTITUDE));
        coords.setLatitudeMeasure(mapToMeasuerType(MockConstants.LATITUDE));
        coords.setLongitudeMeasure(mapToMeasuerType(MockConstants.LONGITUDE));
        return coords;
    }

    private static MeasureType mapToMeasuerType(Double value) {
        MeasureType mt = new MeasureType();
        mt.setValue(BigDecimal.valueOf(value));
        return mt;
    }

    private static IDType mapToIDType(String value) {
        IDType type = new IDType();
        type.setValue(value);
        return type;
    }

    private static DateTimeType mepToDateTimeTypeNow() {
        DateTimeType dateTime = new DateTimeType();
        dateTime.setDateTime(MockConstants.NOW_DATE_GREGORIAN);
        return dateTime;
    }

    private static CodeType mapToCodeType(String value) {
        CodeType code = new CodeType();
        code.setValue(value);
        return code;
    }

    private static FLUXPartyType mapToOwnerFluxParty() {
        FLUXPartyType party = new FLUXPartyType();
        party.getNames().add(mapToTextType());
        return party;
    }

    private static TextType mapToTextType() {
        TextType tt = new TextType();
        tt.setValue(MockConstants.FLUX_OWNER);
        return tt;
    }

    private static VesselCountryType mapToVesselCountry() {
        VesselCountryType ct = new VesselCountryType();
        ct.setID(mapToIDType(MockConstants.ASSET_FLAG_STATE));
        return ct;
    }

    private static IDType mapToIDType(String key, String value) {
        IDType type = new IDType();
        type.setSchemeID(key);
        type.setValue(value);
        return type;
    }

}
