/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europa.ec.fisheries.uvms.plugins.movement.mapper;

import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetId;
import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetIdList;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementBaseType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementComChannelType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.SetReportMovementType;
import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginType;
import eu.europa.ec.fisheries.uvms.plugins.movement.exception.PluginException;
import eu.europa.ec.fisheries.uvms.plugins.movement.mockdata.FluxReportMock;
import eu.europa.ec.fisheries.uvms.plugins.movement.mockdata.MockConstants;
import java.util.HashMap;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import xeu.bridge_connector.v1.RequestType;

/**
 *
 * @author jojoha
 */
@RunWith(MockitoJUnitRunner.class)
public class FluxMessageResponseMapperTest {

    @BeforeClass
    public static void setUpClass() {

    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

    }

    @After
    public void tearDown() {
    }

    @Test
    public void testFluxMessageResponseMapper() throws JAXBException, PluginException {
        RequestType mapToResponseType = FluxReportMock.mapToResponseType();

        List<SetReportMovementType> mapToMovementType = FluxMessageResponseMapper.mapToReportMovementTypes(mapToResponseType, MockConstants.REGISTER_CLASSNAME);

        Assert.assertEquals("The size of List<SetReportMovementType> is incorrect ", 1, mapToMovementType.size());
        assertSetReportMovementType(mapToMovementType.get(0));
        assertMovement(mapToMovementType.get(0).getMovement());

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
        Assert.assertEquals(MovementSourceType.OTHER, movement.getSource());
        
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

}
