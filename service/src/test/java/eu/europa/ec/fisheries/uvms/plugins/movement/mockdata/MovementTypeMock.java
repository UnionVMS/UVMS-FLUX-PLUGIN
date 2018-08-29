/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europa.ec.fisheries.uvms.plugins.movement.mockdata;

import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetId;
import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetIdList;
import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetIdType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementActivityType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementActivityTypeType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementTypeType;

/**
 *
 * @author jojoha
 */
public class MovementTypeMock {

    public static MovementType maptoMovementType() {

        MovementType type = new MovementType();
        type.setActivity(getMovementActivty());
        type.setAssetId(getAssetId());
        type.setAssetName(MockConstants.ASSET_NAME);
        type.setCalculatedCourse(MockConstants.CALC_COURSE);
        type.setCalculatedSpeed(MockConstants.CALC_SPEED);
        type.setConnectId(MockConstants.CONNECT_ID);
        type.setFlagState(MockConstants.ASSET_FLAG_STATE);
        type.setGuid(MockConstants.GUID_ID);
        type.setMeasuredSpeed(MockConstants.MEASURED_SPEED);
        type.setMovementType(MovementTypeType.POS);
        type.setReportedCourse(MockConstants.REPORTED_COURSE);
        type.setReportedSpeed(MockConstants.REPORTED_SPEED);
        type.setPosition(getPosition());
        type.setSource(MovementSourceType.INMARSAT_C);
        type.setExternalMarking(MockConstants.ASSET_EXT_MARKING);
        type.setIrcs(MockConstants.ASSET_IRCS);
        type.setPositionTime(MockConstants.NOW_DATE);
        return type;

    }

    private static MovementActivityType getMovementActivty() {
        MovementActivityType act = new MovementActivityType();
        act.setMessageType(MovementActivityTypeType.AUT);
        act.setCallback(MockConstants.ACTIVITY_CALLBACK);
        act.setMessageId(MockConstants.GUID);
        return act;
    }

    private static AssetId getAssetId() {
        AssetId id = new AssetId();
        id.getAssetIdList().add(getAssetIdList(AssetIdType.IRCS, MockConstants.ASSET_IRCS));
        id.getAssetIdList().add(getAssetIdList(AssetIdType.CFR, MockConstants.ASSET_CFR));
        return id;
    }

    private static AssetIdList getAssetIdList(AssetIdType type, String value) {
        AssetIdList id = new AssetIdList();
        id.setIdType(type);
        id.setValue(value);
        return id;
    }

    private static MovementPoint getPosition() {
        MovementPoint point = new MovementPoint();
        point.setAltitude(MockConstants.ALTITUDE);
        point.setLatitude(MockConstants.LATITUDE);
        point.setLongitude(MockConstants.LONGITUDE);
        return point;
    }

}
