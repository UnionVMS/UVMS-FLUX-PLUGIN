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
package eu.europa.ec.fisheries.uvms.plugins.flux.movement.mapper;

import static org.junit.Assert.assertThat;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBException;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetId;
import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetIdList;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementBaseType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementComChannelType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.SetReportMovementType;
import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginType;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.constants.Codes;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.exception.PluginException;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.mockdata.FluxReportMock;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.mockdata.MockConstants;
import un.unece.uncefact.data.standard.fluxvesselpositionmessage._4.FLUXVesselPositionMessage;
import un.unece.uncefact.data.standard.unqualifieddatatype._18.IDType;
import xeu.bridge_connector.v1.RequestType;

public class FluxMessageResponseMapperTest {

    @Test
    public void testFluxMessageResponseMapper() throws JAXBException, PluginException {
        RequestType mapToResponseType = FluxReportMock.mapToResponseType();

        List<SetReportMovementType> mapToMovementType = FluxMessageResponseMapper.mapToReportMovementTypes(mapToResponseType, MockConstants.REGISTER_CLASSNAME);

        Assert.assertEquals("The size of List<SetReportMovementType> is incorrect ", 1, mapToMovementType.size());
        assertSetReportMovementType(mapToMovementType.get(0));
        assertMovement(mapToMovementType.get(0).getMovement());

    }

    @Test
    public void testFluxMessageResponseMapperMMSI() throws JAXBException, PluginException {
        String mmsi = FluxReportMock.randomIntegers(8);
        FLUXVesselPositionMessage message = FluxReportMock.mapToFLUXReportDocumentType();
        IDType mmsiIdType = FluxReportMock.mapToIDType(Codes.FLUXVesselIDType.MMSI.toString(), mmsi);
        message.getVesselTransportMeans().getIDS().add(mmsiIdType);
        RequestType responseType = FluxReportMock.mapToResponseType(message);
        List<SetReportMovementType> mapToMovementType = FluxMessageResponseMapper.mapToReportMovementTypes(responseType, MockConstants.REGISTER_CLASSNAME);

        assertThat(mapToMovementType.size(), CoreMatchers.is(1));
        assertThat(mapToMovementType.get(0).getMovement().getMmsi(), CoreMatchers.is(mmsi));
        AssetId assetIds = mapToMovementType.get(0).getMovement().getAssetId();
        assertThat(extractAssetIdValue(assetIds, Codes.FLUXVesselIDType.MMSI.toString()), CoreMatchers.is(mmsi));
    }

    @Test
    public void testFluxMessageResponseMapperUnknownSchemeId() throws JAXBException, PluginException {
        FLUXVesselPositionMessage message = FluxReportMock.mapToFLUXReportDocumentType();
        IDType unknownIdType = FluxReportMock.mapToIDType("UNKNOWN", FluxReportMock.randomIntegers(5));
        message.getVesselTransportMeans().getIDS().add(unknownIdType);

        RequestType responseType = FluxReportMock.mapToResponseType(message);
        List<SetReportMovementType> mapToMovementType = FluxMessageResponseMapper.mapToReportMovementTypes(responseType, MockConstants.REGISTER_CLASSNAME);

        Assert.assertEquals("The size of List<SetReportMovementType> is incorrect ", 1, mapToMovementType.size());
        assertSetReportMovementType(mapToMovementType.get(0));
        assertMovement(mapToMovementType.get(0).getMovement());
    }

    @Test
    public void testFluxMessageResponseMapperExtMarkSchemeId() throws JAXBException, PluginException {
        String extMark = FluxReportMock.randomIntegers(5);
        FLUXVesselPositionMessage message = FluxReportMock.mapToFLUXReportDocumentType();
        List<IDType> filteredIds = message.getVesselTransportMeans().getIDS()
            .stream()
            .filter(id -> !id.getSchemeID().equals(Codes.FLUXVesselIDType.EXT_MARK.toString()))
            .collect(Collectors.toList());
        message.getVesselTransportMeans().getIDS().clear();
        message.getVesselTransportMeans().getIDS().addAll(filteredIds);
        IDType extMarkIdType = FluxReportMock.mapToIDType(Codes.FLUXVesselIDType.EXT_MARK.toString(), extMark);
        message.getVesselTransportMeans().getIDS().add(extMarkIdType);

        RequestType responseType = FluxReportMock.mapToResponseType(message);
        List<SetReportMovementType> mapToMovementType = FluxMessageResponseMapper.mapToReportMovementTypes(responseType, MockConstants.REGISTER_CLASSNAME);

        assertThat(mapToMovementType.size(), CoreMatchers.is(1));
        assertThat(mapToMovementType.get(0).getMovement().getExternalMarking(), CoreMatchers.is(extMark));
    }

    @Test
    public void testFluxMessageResponseMapperExtMarkingSchemeId() throws JAXBException, PluginException {
        String extMarking = FluxReportMock.randomIntegers(5);
        FLUXVesselPositionMessage message = FluxReportMock.mapToFLUXReportDocumentType();
        List<IDType> filteredIds = message.getVesselTransportMeans().getIDS()
                .stream()
                .filter(id -> !id.getSchemeID().equals(Codes.FLUXVesselIDType.EXT_MARK.toString()))
                .collect(Collectors.toList());
        message.getVesselTransportMeans().getIDS().clear();
        message.getVesselTransportMeans().getIDS().addAll(filteredIds);
        IDType extMarkingIdType = FluxReportMock.mapToIDType(Codes.FLUXVesselIDType.EXT_MARKING.toString(), extMarking);
        message.getVesselTransportMeans().getIDS().add(extMarkingIdType);

        RequestType responseType = FluxReportMock.mapToResponseType(message);
        List<SetReportMovementType> mapToMovementType = FluxMessageResponseMapper.mapToReportMovementTypes(responseType, MockConstants.REGISTER_CLASSNAME);

        assertThat(mapToMovementType.size(), CoreMatchers.is(1));
        assertThat(mapToMovementType.get(0).getMovement().getExternalMarking(), CoreMatchers.is(extMarking));
    }

    private void assertSetReportMovementType(SetReportMovementType movement) {
        Assert.assertEquals(MockConstants.REGISTER_CLASSNAME, movement.getPluginName());
        Assert.assertEquals(PluginType.FLUX, movement.getPluginType());
    }

    private void assertMovement(MovementBaseType movement) {

        Assert.assertEquals(MovementComChannelType.FLUX, movement.getComChannelType());

        assertAssetId(movement.getAssetId());
        assertMovementPoint(movement.getPosition());

        Assert.assertEquals(MockConstants.ASSET_EXT_MARKING, movement.getExternalMarking());
        Assert.assertEquals(MockConstants.ASSET_IRCS, movement.getIrcs());
        Assert.assertEquals(MockConstants.ASSET_FLAG_STATE, movement.getFlagState());
        Assert.assertEquals(MovementTypeType.POS, movement.getMovementType());
        Assert.assertEquals(MovementSourceType.FLUX, movement.getSource());
        
        Assert.assertEquals(MockConstants.REPORTED_SPEED, movement.getReportedSpeed());
        Assert.assertEquals(MockConstants.REPORTED_COURSE, movement.getReportedCourse());
        
        

    }

    private void assertAssetId(AssetId assetId) {

        Assert.assertNotNull(assetId);
        Assert.assertNotNull(assetId.getAssetIdList());

        Assert.assertEquals(2, assetId.getAssetIdList().size());

        HashMap<String, String> ids = new HashMap<>();
        for (AssetIdList col : assetId.getAssetIdList()) {
            ids.put(col.getIdType().name(), col.getValue());
        }

        Assert.assertEquals(2, ids.size());

        Assert.assertTrue(ids.containsKey(MockConstants.ASSET_IRCS));
        Assert.assertTrue(ids.containsKey(MockConstants.ASSET_CFR));

        Assert.assertEquals(MockConstants.ASSET_IRCS, ids.get(MockConstants.ASSET_IRCS));
        Assert.assertEquals(MockConstants.ASSET_CFR, ids.get(MockConstants.ASSET_CFR));

    }

    private void assertMovementPoint(MovementPoint position) {
        Assert.assertNotNull(position);
        Assert.assertEquals("Altitude assertion failed", MockConstants.ALTITUDE, position.getAltitude());
        Assert.assertEquals("Latitude assertion failed", MockConstants.LATITUDE, position.getLatitude());
        Assert.assertEquals("Longitude assertion failed", MockConstants.LONGITUDE, position.getLongitude());
    }

    private String extractAssetIdValue(AssetId ids, String idType) {
        return ids.getAssetIdList()
                .stream()
                .filter(id -> id.getIdType().toString().equals(idType))
                .findFirst()
                .get()
                .getValue();
    }
}
