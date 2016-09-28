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
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementComChannelType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.SetReportMovementType;
import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginType;
import eu.europa.ec.fisheries.uvms.plugins.flux.exception.PluginException;
import eu.europa.ec.fisheries.uvms.plugins.flux.util.DateUtil;
import eu.europa.ec.fisheries.uvms.service.client.flux.movement.contract.BasicAttribute;
import eu.europa.ec.fisheries.uvms.service.client.flux.movement.contract.ExchangeDocumentInfoType;
import eu.europa.ec.fisheries.uvms.service.client.flux.movement.contract.FLUXGeographicalCoordinateType;
import eu.europa.ec.fisheries.uvms.service.client.flux.movement.contract.IDType;
import eu.europa.ec.fisheries.uvms.service.client.flux.movement.contract.VesselIdentificationType;
import eu.europa.ec.fisheries.uvms.service.client.flux.movement.contract.VesselPositionType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import eu.europa.ec.fisheries.uvms.service.client.flux.movement.contract.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 **/
public class FluxMessageResponseMapper {

    private static Logger LOG = LoggerFactory.getLogger(FluxMessageResponseMapper.class);

    public static BasicAttribute extractBasicAttribute(Element elmnt) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(BasicAttribute.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        BasicAttribute xmlMessage = (BasicAttribute) unmarshaller.unmarshal(elmnt);
        return xmlMessage;
    }

    public static ExchangeDocumentInfoType extractMovement(BasicAttribute attribute) throws PluginException {
        try {
            if (attribute != null) {
                return attribute.getExchangeDocumentInfo();
            } else {
                throw new PluginException("Error when extracting ExchangeDocumentInfoType ( Movement ): BasicAttribute is null");
            }
        } catch (Exception e) {
            LOG.error("[ Error when extracting movement. ] {}", e.getMessage());
            throw new PluginException("Error when extracting ExchangeDocumentInfoType ( Vessel ) : " + e.getMessage());
        }
    }

    public static String extractCorrelationId(BasicAttribute attribute) throws PluginException {
        try {
            if (attribute != null) {
                IDType messageID = attribute.getMessageID();
                return messageID.getValue();
            } else {
                throw new PluginException("Error when extracting Correlation ID: BasicAttribute is null");
            }
        } catch (Exception e) {
            LOG.error("[ Error when extracting correlation ID. ] {}", e.getMessage());
            throw new PluginException("Error when extracting Correlation ID: " + e.getMessage());
        }
    }

    public static List<SetReportMovementType> mapToMovementType(ExchangeDocumentInfoType movement, String pluginName) throws JAXBException, PluginException {

        List<SetReportMovementType> movementList = new ArrayList<>();

        for (VesselPositionType col : movement.getVesselPositions()) {
            SetReportMovementType movementType = new SetReportMovementType();
            movementType.setMovement(mapResponse(col, movement.getVesselID()));
            movementType.setPluginType(PluginType.FLUX);
            movementType.setPluginName(pluginName);
            movementType.setTimestamp(new Date());
            movementList.add(movementType);
        }

        return movementList;
    }

    public static MovementBaseType mapResponse(VesselPositionType response, VesselIdentificationType vesselId) throws JAXBException, PluginException {
        try {
            MovementBaseType movement = new MovementBaseType();
            movement.setAssetId(mapToAssetId(vesselId));
            movement.setPosition(mapToMovementPoint(response.getPosition()));
            movement.setPositionTime(DateUtil.getDate(response.getObtained()));

            if (response.getCourse() != null) {
                if (response.getCourse().getValue() != null) {
                    movement.setReportedCourse(response.getCourse().getValue().doubleValue());
                }
            }

            if (response.getSpeed() != null) {
                if (response.getSpeed().getValue() != null) {
                    movement.setReportedSpeed(response.getSpeed().getValue().doubleValue());
                }
            }

            movement.setComChannelType(MovementComChannelType.FLUX);
            return movement;
        } catch (NullPointerException ex) {
            LOG.error("Nullpointer when mapping to response");
            throw new PluginException("Nullpointer when mapping to response");
        }
    }

    public static MovementPoint mapToMovementPoint(FLUXGeographicalCoordinateType coordinate) throws PluginException {
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

            return point;
        } catch (NullPointerException ex) {
            LOG.error("Nullpointer when mapping to Movement point");
            throw new PluginException("Nullpointer when mapping to Movement point");
        }
    }

    public static AssetId mapToAssetId(VesselIdentificationType vesselId) throws PluginException {
        try {
            AssetId idType = new AssetId();

            if (vesselId.getRadioCallSign() != null) {
                if (vesselId.getRadioCallSign().getValue() != null) {
                    AssetIdList ircs = new AssetIdList();
                    ircs.setIdType(AssetIdType.IRCS);
                    ircs.setValue(vesselId.getRadioCallSign().getValue());
                    idType.getAssetIdList().add(ircs);
                }
            }

            if (vesselId.getUVI() != null) {
                if (vesselId.getUVI().getValue() != null) {
                    AssetIdList mmsi = new AssetIdList();
                    mmsi.setIdType(AssetIdType.MMSI);
                    mmsi.setValue(vesselId.getUVI().getValue());
                    idType.getAssetIdList().add(mmsi);
                }
            }

            return idType;
        } catch (NullPointerException ex) {
            LOG.error("Nullpointer when mapping to AssetId");
            throw new PluginException("Nullpointer when mapping to AssetId");
        }
    }

    public static BigDecimal convertStringToBigDecimal(String value) throws PluginException {
        try {
            Double parsedDouble = Double.parseDouble(value);
            BigDecimal parsedBigDecimal = BigDecimal.valueOf(parsedDouble);
            return parsedBigDecimal;
        } catch (Exception e) {
            LOG.error("[ Error when parsing decimal value from string. ] {}", e.getMessage());
            throw new PluginException("Error when converting String to Decimal: " + e.getMessage());
        }
    }

    public static boolean convertYNToBoolean(String data) throws PluginException {
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

}