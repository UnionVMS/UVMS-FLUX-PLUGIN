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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import eu.europa.ec.fisheries.uvms.plugins.flux.exception.PluginException;
import eu.europa.ec.fisheries.uvms.plugins.flux.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import un.unece.uncefact.data.standard.fluxvesselpositionmessage._4.FLUXVesselPositionMessage;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._18.VesselGeographicalCoordinateType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._18.VesselPositionEventType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._18.VesselTransportMeansType;
import un.unece.uncefact.data.standard.unqualifieddatatype._18.IDType;
import xeu.bridge_connector.v1.RequestType;

public class FluxMessageResponseMapper {

    private static final Logger LOG = LoggerFactory.getLogger(FluxMessageResponseMapper.class);

    private static final String ASSET_EXT_MARKING_CODE = "EXT_MARKING";
    private static final String ASSET_IRCS_CODE = "IRCS";
    private static final String ASSET_UVI_CODE = "UVI";
    private static final String ASSET_CFR_CODE = "CFR";

    private static final String MOVEMENTTYPE_POS = "POS";
    private static final String MOVEMENTTYPE_EXI = "EXI";
    private static final String MOVEMENTTYPE_ENT = "ENT";
    private static final String MOVEMENTTYPE_MAN = "MAN";

    private static FLUXVesselPositionMessage extractVesselPositionMessage(Element any) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(FLUXVesselPositionMessage.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        FLUXVesselPositionMessage xmlMessage = (FLUXVesselPositionMessage) unmarshaller.unmarshal(any);
        return xmlMessage;
    }

    private static VesselTransportMeansType extractMovement(FLUXVesselPositionMessage attribute) throws PluginException {
        try {
            if (attribute != null) {
                return attribute.getVesselTransportMeans();
            } else {
                throw new PluginException("Error when extracting ExchangeDocumentInfoType ( Movement ): BasicAttribute is null");
            }
        } catch (Exception e) {
            LOG.error("[ Error when extracting movement. ] {}", e.getMessage());
            throw new PluginException("Error when extracting ExchangeDocumentInfoType ( Vessel ) : " + e.getMessage());
        }
    }

    private static String extractCorrelationId(FLUXVesselPositionMessage attribute) throws PluginException {
        try {
            if (attribute != null) {
                IDType messageID = attribute.getFLUXReportDocument().getReferencedID();
                return messageID.getValue();
            } else {
                throw new PluginException("Error when extracting Correlation ID: BasicAttribute is null");
            }
        } catch (Exception e) {
            LOG.error("[ Error when extracting correlation ID. ] {}", e.getMessage());
            throw new PluginException("Error when extracting Correlation ID: " + e.getMessage());
        }
    }

    public static List<SetReportMovementType> mapToMovementType(RequestType rt, String registerClassName) throws JAXBException, PluginException {

        FLUXVesselPositionMessage extractVesselPositionMessage = extractVesselPositionMessage(rt.getAny());
        VesselTransportMeansType positionReport = extractMovement(extractVesselPositionMessage);
        List<SetReportMovementType> movementList = new ArrayList<>();

        for (VesselPositionEventType col : positionReport.getSpecifiedVesselPositionEvents()) {
            SetReportMovementType movementType = new SetReportMovementType();
            movementType.setMovement(mapResponse(col, positionReport));
            movementType.setPluginType(PluginType.FLUX);
            movementType.setPluginName(registerClassName);
            movementType.setTimestamp(DateUtil.createNowDate());
            movementList.add(movementType);
        }

        return movementList;

    }

    private static MovementBaseType mapResponse(VesselPositionEventType response, VesselTransportMeansType report) throws JAXBException, PluginException {
        try {
            MovementBaseType movement = new MovementBaseType();

            HashMap<String, String> extractAssetIds = extractAssetIds(report.getIDS());
            movement.setAssetId(mapToAssetId(extractAssetIds));

            movement.setExternalMarking(extractAssetIds.get(ASSET_EXT_MARKING_CODE));
            movement.setIrcs(extractAssetIds.get(ASSET_IRCS_CODE));

            try {
                switch (response.getTypeCode().getValue()) {
                    case MOVEMENTTYPE_POS:
                        movement.setMovementType(MovementTypeType.POS);
                        break;
                    case MOVEMENTTYPE_EXI:
                        movement.setMovementType(MovementTypeType.EXI);
                        break;
                    case MOVEMENTTYPE_ENT:
                        movement.setMovementType(MovementTypeType.ENT);
                        break;
                    case MOVEMENTTYPE_MAN:
                        movement.setMovementType(MovementTypeType.MAN);
                        break;
                    default:
                        throw new AssertionError();

                }
            } catch (NullPointerException e) {
                LOG.error("Nullpointer when mapping movementType from typecode in report from FLUX, setting movementtype to POS");
                movement.setMovementType(MovementTypeType.POS);
            }

            try {
                movement.setFlagState(report.getRegistrationVesselCountry().getID().getValue());
            } catch (NullPointerException e) {
                LOG.error("Nullpointer when mapping flagstate in position report from FLUX");
            }

            movement.setPosition(mapToMovementPoint(response.getSpecifiedVesselGeographicalCoordinate()));
            movement.setPositionTime(DateUtil.getDate(response.getObtainedOccurrenceDateTime().getDateTime()));

            if (response.getCourseValueMeasure() != null) {
                if (response.getCourseValueMeasure().getValue() != null) {
                    movement.setReportedCourse(response.getCourseValueMeasure().getValue().doubleValue());
                }
            }

            if (response.getSpeedValueMeasure() != null) {
                if (response.getSpeedValueMeasure().getValue() != null) {
                    movement.setReportedSpeed(response.getSpeedValueMeasure().getValue().doubleValue());
                }
            }

            movement.setComChannelType(MovementComChannelType.FLUX);
            movement.setSource(MovementSourceType.OTHER);
            return movement;
        } catch (NullPointerException ex) {
            LOG.error("Nullpointer when mapping to response");
            throw new PluginException("Nullpointer when mapping to response");
        }
    }

    private static MovementPoint mapToMovementPoint(VesselGeographicalCoordinateType coordinate) throws PluginException {
        try {
            MovementPoint point = new MovementPoint();

            if (coordinate.getLatitudeMeasure() != null) {
                if (coordinate.getLatitudeMeasure().getValue() != null) {
                    point.setLatitude(coordinate.getLatitudeMeasure().getValue().doubleValue());
                }
            }

            if (coordinate.getLongitudeMeasure() != null) {
                if (coordinate.getLongitudeMeasure().getValue() != null) {
                    point.setLongitude(coordinate.getLongitudeMeasure().getValue().doubleValue());
                }
            }

            if (coordinate.getAltitudeMeasure() != null) {
                if (coordinate.getAltitudeMeasure().getValue() != null) {
                    point.setAltitude(coordinate.getAltitudeMeasure().getValue().doubleValue());
                }
            }

            return point;
        } catch (NullPointerException ex) {
            LOG.error("Nullpointer when mapping to Movement point");
            throw new PluginException("Nullpointer when mapping to Movement point");
        }
    }

    private static HashMap<String, String> extractAssetIds(List<IDType> vesselIds) {
        HashMap<String, String> ids = new HashMap<>();
        for (IDType vesselId : vesselIds) {
            ids.put(vesselId.getSchemeID(), vesselId.getValue());
        }
        return ids;
    }

    private static AssetId mapToAssetId(Map<String, String> vesselIds) throws PluginException {
        try {
            AssetId idType = new AssetId();
            for (Entry<String, String> vesselId : vesselIds.entrySet()) {
                switch (vesselId.getKey()) {
                    case ASSET_IRCS_CODE:
                        idType.getAssetIdList().add(mapToVesselId(AssetIdType.IRCS, vesselId.getValue()));
                        break;
                    case ASSET_CFR_CODE:
                        idType.getAssetIdList().add(mapToVesselId(AssetIdType.CFR, vesselId.getValue()));
                        break;
                    case ASSET_UVI_CODE:
                        idType.getAssetIdList().add(mapToVesselId(AssetIdType.IMO, vesselId.getValue()));
                        break;
                    default:
                        LOG.error("VesselId type not mapped {}", vesselId.getKey());
                        break;
                }
            }
            return idType;
        } catch (NullPointerException ex) {
            LOG.error("Nullpointer when mapping to AssetId");
            throw new PluginException("Nullpointer when mapping to AssetId");
        }
    }

    private static BigDecimal convertStringToBigDecimal(String value) throws PluginException {
        try {
            Double parsedDouble = Double.parseDouble(value);
            BigDecimal parsedBigDecimal = BigDecimal.valueOf(parsedDouble);
            return parsedBigDecimal;
        } catch (Exception e) {
            LOG.error("[ Error when parsing decimal value from string. ] {}", e.getMessage());
            throw new PluginException("Error when converting String to Decimal: " + e.getMessage());
        }
    }

    private static boolean convertYNToBoolean(String data) throws PluginException {
        try {
            if (data != null && !data.isEmpty()) {
                if (data.equalsIgnoreCase("Y")) {
                    return true;
                } else if (data.equalsIgnoreCase("N")) {
                    return false;
                } else {
                    throw new PluginException("Error when converting Y N to boolean, String does not match Y or N");
                }
            } else {
                throw new PluginException("Error when converting Y N to boolean, input string is null or empty");
            }
        } catch (Exception e) {
            LOG.error("[ Error when converting string to boolean. ] {}", e.getMessage());
            throw new PluginException("Error when converting Y N to boolean: " + e.getMessage());
        }
    }

    private static AssetIdList mapToVesselId(AssetIdType assetIdType, String value) {
        AssetIdList assetId = new AssetIdList();
        assetId.setIdType(assetIdType);
        assetId.setValue(value);
        return assetId;
    }

}
