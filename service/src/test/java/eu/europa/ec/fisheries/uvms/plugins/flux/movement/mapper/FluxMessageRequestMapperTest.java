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
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europa.ec.fisheries.uvms.plugins.flux.movement.mapper;

import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetId;
import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetIdList;
import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetIdType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.mockdata.MockConstants;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.mockdata.MovementTypeMock;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.service.StartupBean;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.w3c.dom.Element;

import un.unece.uncefact.data.standard.fluxvesselpositionmessage._4.FLUXVesselPositionMessage;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._18.FLUXReportDocumentType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._18.VesselPositionEventType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._18.VesselTransportMeansType;
import un.unece.uncefact.data.standard.unqualifieddatatype._18.IDType;
import xeu.connector_bridge.v1.PostMsgType;

/**
 *
 * @author jojoha
 */
@RunWith(MockitoJUnitRunner.class)
public class FluxMessageRequestMapperTest {

    FluxMessageResponseMapper responseMapper;

    @Mock
    StartupBean settings;

    @InjectMocks
    FluxMessageRequestMapper requestMapper;

    public FluxMessageRequestMapperTest() {
    }

    @BeforeClass
    public static void setUpClass() {

    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(settings.getSetting("FLUX_DEFAULT_AD")).thenReturn(MockConstants.AD);
        Mockito.when(settings.getSetting("FLUX_DATAFLOW")).thenReturn(MockConstants.FLUX_DATA_FLOW);
        Mockito.when(settings.getSetting("OWNER_FLUX_PARTY")).thenReturn(MockConstants.OWNER_FLUX_PARTY);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of mapToRequest method, of class FluxMessageRequestMapper.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testMapToRequest() throws Exception {
        MovementType movement = MovementTypeMock.maptoMovementType();

        PostMsgType mapToRequest = requestMapper.mapToRequest(movement, MockConstants.GUID, MockConstants.RECIPIENT, Collections.emptyList());

        assertNotNull(mapToRequest);

        assertEquals(MockConstants.RECIPIENT, mapToRequest.getAD());

        FLUXVesselPositionMessage extractVesselPositionMessage = extractVesselPositionMessage(mapToRequest.getAny());
        assertFLUXVesselPositionMessage(extractVesselPositionMessage);
        assertFluxReportDocument(extractVesselPositionMessage.getFLUXReportDocument());
        assertFluxVesselTransportMeans(extractVesselPositionMessage.getVesselTransportMeans());
        assertSpecifiedVesselPositionEvent(extractVesselPositionMessage.getVesselTransportMeans().getSpecifiedVesselPositionEvents());
    }

    @Test
    public void testMapToRequestRecipientNull() throws Exception {
        MovementType movement = MovementTypeMock.maptoMovementType();
        PostMsgType mapToRequest = requestMapper.mapToRequest(movement, MockConstants.GUID, null, Collections.emptyList());
        assertNotNull(mapToRequest);
        assertEquals(MockConstants.AD, mapToRequest.getAD());
    }
    
    @Test
    public void testMapNullReportedCourse() throws Exception {
        MovementType movement = MovementTypeMock.maptoMovementType();
        movement.setReportedCourse(null);
        PostMsgType mapToRequest = requestMapper.mapToRequest(movement, MockConstants.GUID, null, Collections.emptyList());
        assertNotNull(mapToRequest);
        FLUXVesselPositionMessage message = extractVesselPositionMessage(mapToRequest.getAny());
        assertNull(message.getVesselTransportMeans().getSpecifiedVesselPositionEvents().get(0).getCourseValueMeasure());
    }

    @Test
    public void testMapNullReportedSpeed() throws Exception {
        MovementType movement = MovementTypeMock.maptoMovementType();
        movement.setReportedSpeed(null);
        PostMsgType mapToRequest = requestMapper.mapToRequest(movement, MockConstants.GUID, null, Collections.emptyList());
        assertNotNull(mapToRequest);
        FLUXVesselPositionMessage message = extractVesselPositionMessage(mapToRequest.getAny());
        assertNull(message.getVesselTransportMeans().getSpecifiedVesselPositionEvents().get(0).getSpeedValueMeasure());
    }

    @Test
    public void testReportedSpeedNoRounding() throws Exception {
        MovementType movement = MovementTypeMock.maptoMovementType();
        movement.setReportedSpeed(2d);
        PostMsgType mapToRequest = requestMapper.mapToRequest(movement, MockConstants.GUID, null, Collections.emptyList());
        assertNotNull(mapToRequest);
        FLUXVesselPositionMessage message = extractVesselPositionMessage(mapToRequest.getAny());
        assertEquals(BigDecimal.valueOf(2), message.getVesselTransportMeans().getSpecifiedVesselPositionEvents().get(0).getSpeedValueMeasure().getValue());
    }

    @Test
    public void testReportedSpeedRounding() throws Exception {
        MovementType movement = MovementTypeMock.maptoMovementType();
        movement.setReportedSpeed(1.23456);
        PostMsgType mapToRequest = requestMapper.mapToRequest(movement, MockConstants.GUID, null, Collections.emptyList());
        assertNotNull(mapToRequest);
        FLUXVesselPositionMessage message = extractVesselPositionMessage(mapToRequest.getAny());
        assertEquals(BigDecimal.valueOf(1.23), message.getVesselTransportMeans().getSpecifiedVesselPositionEvents().get(0).getSpeedValueMeasure().getValue());
    }

    @Test
    public void testReportedSpeedRoundingUp() throws Exception {
        MovementType movement = MovementTypeMock.maptoMovementType();
        movement.setReportedSpeed(1.23656);
        PostMsgType mapToRequest = requestMapper.mapToRequest(movement, MockConstants.GUID, null, Collections.emptyList());
        assertNotNull(mapToRequest);
        FLUXVesselPositionMessage message = extractVesselPositionMessage(mapToRequest.getAny());
        assertEquals(BigDecimal.valueOf(1.24), message.getVesselTransportMeans().getSpecifiedVesselPositionEvents().get(0).getSpeedValueMeasure().getValue());
    }

    @Test
    public void testReportedCourseNoRounding() throws Exception {
        MovementType movement = MovementTypeMock.maptoMovementType();
        movement.setReportedCourse(2d);
        PostMsgType mapToRequest = requestMapper.mapToRequest(movement, MockConstants.GUID, null, Collections.emptyList());
        assertNotNull(mapToRequest);
        FLUXVesselPositionMessage message = extractVesselPositionMessage(mapToRequest.getAny());
        assertEquals(BigDecimal.valueOf(2), message.getVesselTransportMeans().getSpecifiedVesselPositionEvents().get(0).getCourseValueMeasure().getValue());
    }

    @Test
    public void testReportedCourseRounding() throws Exception {
        MovementType movement = MovementTypeMock.maptoMovementType();
        movement.setReportedCourse(1.23456);
        PostMsgType mapToRequest = requestMapper.mapToRequest(movement, MockConstants.GUID, null, Collections.emptyList());
        assertNotNull(mapToRequest);
        FLUXVesselPositionMessage message = extractVesselPositionMessage(mapToRequest.getAny());
        assertEquals(BigDecimal.valueOf(1.23), message.getVesselTransportMeans().getSpecifiedVesselPositionEvents().get(0).getCourseValueMeasure().getValue());
    }

    @Test
    public void testReportedCourseRoundingUp() throws Exception {
        MovementType movement = MovementTypeMock.maptoMovementType();
        movement.setReportedCourse(1.23656);
        PostMsgType mapToRequest = requestMapper.mapToRequest(movement, MockConstants.GUID, null, Collections.emptyList());
        assertNotNull(mapToRequest);
        FLUXVesselPositionMessage message = extractVesselPositionMessage(mapToRequest.getAny());
        assertEquals(BigDecimal.valueOf(1.24), message.getVesselTransportMeans().getSpecifiedVesselPositionEvents().get(0).getCourseValueMeasure().getValue());
    }

    @Test
    public void testLatitudeNoRounding() throws Exception {
        MovementType movement = MovementTypeMock.maptoMovementType();
        movement.getPosition().setLatitude(1.2345);
        PostMsgType mapToRequest = requestMapper.mapToRequest(movement, MockConstants.GUID, null, Collections.emptyList());
        assertNotNull(mapToRequest);
        FLUXVesselPositionMessage message = extractVesselPositionMessage(mapToRequest.getAny());
        assertEquals(BigDecimal.valueOf(1.2345), message.getVesselTransportMeans().getSpecifiedVesselPositionEvents().get(0).getSpecifiedVesselGeographicalCoordinate().getLatitudeMeasure().getValue());
    }

    @Test
    public void testLatitudeRoundingAddDecimal() throws Exception {
        MovementType movement = MovementTypeMock.maptoMovementType();
        movement.getPosition().setLatitude(1.2);
        PostMsgType mapToRequest = requestMapper.mapToRequest(movement, MockConstants.GUID, null, Collections.emptyList());
        assertNotNull(mapToRequest);
        FLUXVesselPositionMessage message = extractVesselPositionMessage(mapToRequest.getAny());
        assertEquals(new BigDecimal("1.200"), message.getVesselTransportMeans().getSpecifiedVesselPositionEvents().get(0).getSpecifiedVesselGeographicalCoordinate().getLatitudeMeasure().getValue());
    }

    @Test
    public void testLatitudeRoundingTruncateDecimal() throws Exception {
        MovementType movement = MovementTypeMock.maptoMovementType();
        movement.getPosition().setLatitude(1.2345234);
        PostMsgType mapToRequest = requestMapper.mapToRequest(movement, MockConstants.GUID, null, Collections.emptyList());
        assertNotNull(mapToRequest);
        FLUXVesselPositionMessage message = extractVesselPositionMessage(mapToRequest.getAny());
        assertEquals(BigDecimal.valueOf(1.234523), message.getVesselTransportMeans().getSpecifiedVesselPositionEvents().get(0).getSpecifiedVesselGeographicalCoordinate().getLatitudeMeasure().getValue());
    }

    @Test
    public void testLatitudeRoundingDecimalUp() throws Exception {
        MovementType movement = MovementTypeMock.maptoMovementType();
        movement.getPosition().setLatitude(1.2345236);
        PostMsgType mapToRequest = requestMapper.mapToRequest(movement, MockConstants.GUID, null, Collections.emptyList());
        assertNotNull(mapToRequest);
        FLUXVesselPositionMessage message = extractVesselPositionMessage(mapToRequest.getAny());
        assertEquals(BigDecimal.valueOf(1.234524), message.getVesselTransportMeans().getSpecifiedVesselPositionEvents().get(0).getSpecifiedVesselGeographicalCoordinate().getLatitudeMeasure().getValue());
    }

    @Test
    public void testLongitudeNoRounding() throws Exception {
        MovementType movement = MovementTypeMock.maptoMovementType();
        movement.getPosition().setLongitude(1.2345);
        PostMsgType mapToRequest = requestMapper.mapToRequest(movement, MockConstants.GUID, null, Collections.emptyList());
        assertNotNull(mapToRequest);
        FLUXVesselPositionMessage message = extractVesselPositionMessage(mapToRequest.getAny());
        assertEquals(BigDecimal.valueOf(1.2345), message.getVesselTransportMeans().getSpecifiedVesselPositionEvents().get(0).getSpecifiedVesselGeographicalCoordinate().getLongitudeMeasure().getValue());
    }

    @Test
    public void testLongitudeRoundingAddDecimal() throws Exception {
        MovementType movement = MovementTypeMock.maptoMovementType();
        movement.getPosition().setLongitude(1.2);
        PostMsgType mapToRequest = requestMapper.mapToRequest(movement, MockConstants.GUID, null, Collections.emptyList());
        assertNotNull(mapToRequest);
        FLUXVesselPositionMessage message = extractVesselPositionMessage(mapToRequest.getAny());
        assertEquals(new BigDecimal("1.200"), message.getVesselTransportMeans().getSpecifiedVesselPositionEvents().get(0).getSpecifiedVesselGeographicalCoordinate().getLongitudeMeasure().getValue());
    }

    @Test
    public void testLongitudeRoundingTruncateDecimal() throws Exception {
        MovementType movement = MovementTypeMock.maptoMovementType();
        movement.getPosition().setLongitude(1.2345234);
        PostMsgType mapToRequest = requestMapper.mapToRequest(movement, MockConstants.GUID, null, Collections.emptyList());
        assertNotNull(mapToRequest);
        FLUXVesselPositionMessage message = extractVesselPositionMessage(mapToRequest.getAny());
        assertEquals(BigDecimal.valueOf(1.234523), message.getVesselTransportMeans().getSpecifiedVesselPositionEvents().get(0).getSpecifiedVesselGeographicalCoordinate().getLongitudeMeasure().getValue());
    }

    @Test
    public void testLongitudeRoundingDecimalUp() throws Exception {
        MovementType movement = MovementTypeMock.maptoMovementType();
        movement.getPosition().setLongitude(1.2345236);
        PostMsgType mapToRequest = requestMapper.mapToRequest(movement, MockConstants.GUID, null, Collections.emptyList());
        assertNotNull(mapToRequest);
        FLUXVesselPositionMessage message = extractVesselPositionMessage(mapToRequest.getAny());
        assertEquals(BigDecimal.valueOf(1.234524), message.getVesselTransportMeans().getSpecifiedVesselPositionEvents().get(0).getSpecifiedVesselGeographicalCoordinate().getLongitudeMeasure().getValue());
    }

    @Test
    public void testImoIdentifier() throws Exception {
        String imo = "12345";
        MovementType movement = MovementTypeMock.maptoMovementType();
        AssetIdList imoId = new AssetIdList();
        imoId.setIdType(AssetIdType.IMO);
        imoId.setValue(imo);
        movement.getAssetId().getAssetIdList().add(imoId);
        PostMsgType mapToRequest = requestMapper.mapToRequest(movement, MockConstants.GUID, null, Collections.emptyList());
        assertNotNull(mapToRequest);
        FLUXVesselPositionMessage message = extractVesselPositionMessage(mapToRequest.getAny());
        Optional<IDType> uvi = message.getVesselTransportMeans().getIDS().stream().filter(i -> i.getSchemeID().equals("UVI")).findFirst();

        assertEquals(imo, uvi.get().getValue());
    }

    @Test
    public void testIrcsWithDash() throws Exception {
        String ircs = "ABC-1234";
        MovementType movement = MovementTypeMock.maptoMovementType();
        movement.setIrcs(ircs);
        AssetId assetId = new AssetId();
        AssetIdList assetIdList = new AssetIdList();
        assetIdList.setIdType(AssetIdType.IRCS);
        assetIdList.setValue(ircs);
        assetId.getAssetIdList().add(assetIdList);
        movement.setAssetId(assetId);
        PostMsgType mapToRequest = requestMapper.mapToRequest(movement, MockConstants.GUID, null, Collections.emptyList());
        assertNotNull(mapToRequest);
        FLUXVesselPositionMessage message = extractVesselPositionMessage(mapToRequest.getAny());
        Optional<IDType> ircsId = message.getVesselTransportMeans().getIDS().stream().filter(i -> i.getSchemeID().equals("IRCS")).findFirst();

        assertEquals("ABC1234", ircsId.get().getValue());
    }

    private void assertFLUXVesselPositionMessage(FLUXVesselPositionMessage message) {
        assertNotNull("FLUXVesselPositionMessage is NULL", message);
        assertNotNull("FLUXReportDocumentType is NULL", message.getFLUXReportDocument());
        assertNotNull("VesselTransportMeansType is NULL", message.getVesselTransportMeans());
    }

    private void assertFluxReportDocument(FLUXReportDocumentType fluxReportDocument) {
        assertNotNull("FLUXReportDocumentType is NULL", fluxReportDocument);

        List<IDType> ids = fluxReportDocument.getIDS();
        assertEquals(1, ids.size());

        assertTrue("Reference id is not a UUID", fluxReportDocument.getIDS().get(0).getValue().matches(MockConstants.UUID_REGEX));

        assertNotNull("DateTime is null", fluxReportDocument.getCreationDateTime().getDateTime());

        assertNotNull("Owner Flux party list size should be 1", fluxReportDocument.getOwnerFLUXParty().getIDS().size() == 1);
        assertEquals(MockConstants.OWNER_FLUX_PARTY, fluxReportDocument.getOwnerFLUXParty().getIDS().get(0).getValue());
    }

    private void assertFluxVesselTransportMeans(VesselTransportMeansType vesselTransportMeans) {
        assertNotNull("VesselTransportMeansType is NULL", vesselTransportMeans);

        assertEquals("VesselTransportMeansType id list should be of size 3", 3, vesselTransportMeans.getIDS().size());

        Map<String, String> data = new HashMap<>();
        for (IDType col : vesselTransportMeans.getIDS()) {
            data.put(col.getSchemeID(), col.getValue());
        }

        assertTrue("VesselTransportMeansType does not contain id:" + MockConstants.ASSET_IRCS, data.containsKey(MockConstants.ASSET_IRCS));
        assertTrue("VesselTransportMeansType does not contain id:" + MockConstants.ASSET_EXT_MARKING, data.containsKey(MockConstants.ASSET_EXT_MARKING));
        assertTrue("VesselTransportMeansType does not contain id:" + MockConstants.ASSET_CFR, data.containsKey(MockConstants.ASSET_CFR));

        assertEquals(MockConstants.ASSET_CFR, data.get(MockConstants.ASSET_CFR));
        assertEquals(MockConstants.ASSET_EXT_MARKING, data.get(MockConstants.ASSET_EXT_MARKING));
        assertEquals(MockConstants.ASSET_CFR, data.get(MockConstants.ASSET_CFR));

        assertEquals(MockConstants.ASSET_FLAG_STATE, vesselTransportMeans.getRegistrationVesselCountry().getID().getValue());

    }

    /**
     * Test of addHeaderValueToRequest method, of class
     * FluxMessageRequestMapper.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testAddHeaderValueToRequest() throws Exception {

    }

    private void assertSpecifiedVesselPositionEvent(List<VesselPositionEventType> specifiedVesselPositionEvents) {
        assertNotNull(specifiedVesselPositionEvents);
        assertEquals(1, specifiedVesselPositionEvents.size());

        VesselPositionEventType event = specifiedVesselPositionEvents.get(0);
        assertEquals(MockConstants.NOW_DATE_GREGORIAN.toString(), event.getObtainedOccurrenceDateTime().getDateTime().toString());
        assertEquals(MovementTypeType.POS.name(), event.getTypeCode().getValue());

        assertEquals(MockConstants.REPORTED_SPEED.doubleValue(), event.getSpeedValueMeasure().getValue().doubleValue(), 0.01);
        assertEquals(MockConstants.REPORTED_COURSE.doubleValue(), event.getCourseValueMeasure().getValue().doubleValue(), 0.01);

        assertEquals(MockConstants.LATITUDE.doubleValue(), event.getSpecifiedVesselGeographicalCoordinate().getLatitudeMeasure().getValue().doubleValue(), 0.01);
        assertEquals(MockConstants.LONGITUDE.doubleValue(), event.getSpecifiedVesselGeographicalCoordinate().getLongitudeMeasure().getValue().doubleValue(), 0.01);

    }

    private static FLUXVesselPositionMessage extractVesselPositionMessage(Element any) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(FLUXVesselPositionMessage.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        FLUXVesselPositionMessage xmlMessage = (FLUXVesselPositionMessage) unmarshaller.unmarshal(any);
        return xmlMessage;
    }

}
