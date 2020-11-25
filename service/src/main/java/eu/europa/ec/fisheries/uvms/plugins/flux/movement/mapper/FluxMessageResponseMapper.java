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

import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetId;
import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetIdList;
import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetIdType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementBaseType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementComChannelType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.SetReportMovementType;
import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginType;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.constants.Codes;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.constants.Codes.FLUXVesselIDType;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.constants.Codes.FLUXVesselPositionType;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.exception.PluginException;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.util.DateUtil;

import java.util.*;
import java.util.Map.Entry;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import un.unece.uncefact.data.standard.fluxvesselpositionmessage._4.FLUXVesselPositionMessage;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._18.VesselCountryType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._18.VesselGeographicalCoordinateType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._18.VesselPositionEventType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._18.VesselTransportMeansType;
import un.unece.uncefact.data.standard.unqualifieddatatype._18.CodeType;
import un.unece.uncefact.data.standard.unqualifieddatatype._18.IDType;
import un.unece.uncefact.data.standard.unqualifieddatatype._18.MeasureType;
import xeu.bridge_connector.v1.RequestType;

public class FluxMessageResponseMapper {

    private static final Logger LOG = LoggerFactory.getLogger(FluxMessageResponseMapper.class);

    public static List<SetReportMovementType> mapToReportMovementTypes(RequestType rt, String registerClassName) throws PluginException {
        FLUXVesselPositionMessage vessPosMessage;
        String originalMessage = null;
        try {
            vessPosMessage = extractVesselPositionMessage(rt.getAny());
            originalMessage = JAXBMarshaller.marshallJaxBObjectToString(vessPosMessage);
        } catch (JAXBException e) {
            throw new PluginException("[ERROR] Error while trying to FluxMessageResponseMapper.vessPosMessage(rt.getAny())!");
        }
        VesselTransportMeansType positionReport = vessPosMessage.getVesselTransportMeans();
        List<SetReportMovementType> movementList = new ArrayList<>();
        for (VesselPositionEventType col : positionReport.getSpecifiedVesselPositionEvents()) {
            SetReportMovementType movementType = new SetReportMovementType();
            movementType.setMovement(mapResponse(col, positionReport));
            movementType.setPluginType(PluginType.FLUX);
            movementType.setPluginName(registerClassName);
            movementType.setTimestamp(new Date());
            movementType.setOriginalIncomingMessage(originalMessage);
            movementList.add(movementType);
        }
        return movementList;
    }

    private static MovementBaseType mapResponse(VesselPositionEventType response, VesselTransportMeansType report) {
        MovementBaseType movement = new MovementBaseType();
        HashMap<Codes.FLUXVesselIDType, String> extractAssetIds = extractAssetIds(report.getIDS());
        movement.setAssetId(mapToAssetId(extractAssetIds));
        movement.setExternalMarking(extractAssetIds.get(FLUXVesselIDType.EXT_MARK));
        movement.setIrcs(extractAssetIds.get(FLUXVesselIDType.IRCS));
        movement.setMovementType(mapToMovementTypeFromPositionType(movement, response.getTypeCode()));
        setFlagState(movement, report.getRegistrationVesselCountry());
        movement.setPosition(mapToMovementPoint(response.getSpecifiedVesselGeographicalCoordinate()));
        if (response.getObtainedOccurrenceDateTime() != null) {
            movement.setPositionTime(DateUtil.getDate(response.getObtainedOccurrenceDateTime().getDateTime()));
        }
        setCourseAndSpeed(response, movement);
        movement.setComChannelType(MovementComChannelType.FLUX);
        movement.setSource(MovementSourceType.FLUX);
        return movement;
    }

    private static void setFlagState(MovementBaseType movement, VesselCountryType registrationVesselCountry) {
        if (registrationVesselCountry != null && registrationVesselCountry.getID() != null) {
            movement.setFlagState(registrationVesselCountry.getID().getValue());
        } else {
            movement.setFlagState(null);
            LOG.error("[ERROR] Couldn't set FlagState, VesselTransportMeansType.getRegistrationVesselCountry.ID!");
        }
    }

    private static void setCourseAndSpeed(VesselPositionEventType response, MovementBaseType movement) {
        final MeasureType courseValueMeasure = response.getCourseValueMeasure();
        if (courseValueMeasure != null) {
            if (courseValueMeasure.getValue() != null) {
                movement.setReportedCourse(courseValueMeasure.getValue().doubleValue());
            }
        }
        final MeasureType speedValueMeasure = response.getSpeedValueMeasure();
        if (speedValueMeasure != null) {
            if (speedValueMeasure.getValue() != null) {
                movement.setReportedSpeed(speedValueMeasure.getValue().doubleValue());
            }
        }
    }

    private static MovementTypeType mapToMovementTypeFromPositionType(MovementBaseType movement, CodeType vessPosTypeCode) {
        MovementTypeType movType;
        if (vessPosTypeCode != null) {
            movType = FLUXVesselPositionType.fromExternal(vessPosTypeCode.getValue());
        } else {
            movType = MovementTypeType.POS;
            LOG.error("[ERROR] Couldn't map to movementType, vessPosTypeCode was null!");
        }
        return movType;
    }

    private static MovementPoint mapToMovementPoint(VesselGeographicalCoordinateType coordinate) {
        MovementPoint point = new MovementPoint();
        if (coordinate != null) {
            final MeasureType latitudeMeasure = coordinate.getLatitudeMeasure();
            if (latitudeMeasure != null) {
                if (latitudeMeasure.getValue() != null) {
                    point.setLatitude(latitudeMeasure.getValue().doubleValue());
                }
            }
            final MeasureType longitudeMeasure = coordinate.getLongitudeMeasure();
            if (longitudeMeasure != null) {
                if (longitudeMeasure.getValue() != null) {
                    point.setLongitude(longitudeMeasure.getValue().doubleValue());
                }
            }
            final MeasureType altitudeMeasure = coordinate.getAltitudeMeasure();
            if (altitudeMeasure != null) {
                if (altitudeMeasure.getValue() != null) {
                    point.setAltitude(altitudeMeasure.getValue().doubleValue());
                }
            }
        }
        return point;
    }

    private static HashMap<FLUXVesselIDType, String> extractAssetIds(List<IDType> vesselIds) {
        HashMap<FLUXVesselIDType, String> ids = new HashMap<>();
        if (CollectionUtils.isNotEmpty(vesselIds)) {
            for (IDType vesselId : vesselIds) {
                ids.put(FLUXVesselIDType.valueOf(vesselId.getSchemeID()), vesselId.getValue());
            }
        }
        return ids;
    }

    private static AssetId mapToAssetId(Map<FLUXVesselIDType, String> vesselIds) {
        AssetId idType = new AssetId();
        List<AssetIdList> assetIdList = idType.getAssetIdList();
        if (MapUtils.isNotEmpty(vesselIds)) {
            for (Entry<FLUXVesselIDType, String> vesselId : vesselIds.entrySet()) {
                switch (vesselId.getKey()) {
                    case IRCS:
                        assetIdList.add(mapToVesselId(AssetIdType.IRCS, vesselId.getValue()));
                        break;
                    case CFR:
                        assetIdList.add(mapToVesselId(AssetIdType.CFR, vesselId.getValue()));
                        break;
                    case UVI:
                        assetIdList.add(mapToVesselId(AssetIdType.IMO, vesselId.getValue()));
                        break;
                    case EXT_MARK:
                        break;
                    default:
                        LOG.error("VesselId type not mapped {}", vesselId.getKey());
                }
            }
        }
        return idType;
    }

    public static String extractMessageGUID(FLUXVesselPositionMessage attribute) throws PluginException {
        try {
            if (attribute != null) {
                IDType messageID = attribute.getFLUXReportDocument().getIDS().get(0);
                return messageID.getValue();
            } else {
                throw new PluginException("Error when extracting Correlation ID: BasicAttribute is null");
            }
        } catch (Exception e) {
            LOG.error("[ Error when extracting correlation ID. ] {}", e.getMessage());
            throw new PluginException("Error when extracting Correlation ID: " + e.getMessage());
        }
    }


    public static String extractMessageGUID(RequestType rt) throws PluginException {
        try {
            return extractMessageGUID(extractVesselPositionMessage(rt.getAny()));
        } catch (JAXBException e) {
            throw new PluginException(e.getMessage());
        }
    }

    private static AssetIdList mapToVesselId(AssetIdType assetIdType, String value) {
        AssetIdList assetId = new AssetIdList();
        assetId.setIdType(assetIdType);
        assetId.setValue(value);
        return assetId;
    }

    public static FLUXVesselPositionMessage extractVesselPositionMessage(Element any) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(FLUXVesselPositionMessage.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        FLUXVesselPositionMessage xmlMessage = (FLUXVesselPositionMessage) unmarshaller.unmarshal(any);
        return xmlMessage;
    }

}
