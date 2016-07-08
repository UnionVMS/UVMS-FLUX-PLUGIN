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

import eu.europa.ec.fisheries.schema.exchange.movement.v1.SetReportMovementType;
import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginType;
import eu.europa.ec.fisheries.uvms.plugins.flux.exception.PluginException;
import eu.europa.ec.fisheries.uvms.plugins.flux.util.DateUtil;
import eu.europa.ec.fisheries.uvms.service.client.flux.movement.contract.ExchangeDocumentInfoType;
import eu.europa.ec.fisheries.uvms.service.client.flux.movement.contract.VesselPositionType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.bind.JAXBException;

/**
 **/
public class MovementMapper {
    
    public static List<SetReportMovementType> mapToMovementType(ExchangeDocumentInfoType movement) throws JAXBException, PluginException {
        
        List<SetReportMovementType> movementList = new ArrayList<>();
        
        for (VesselPositionType col : movement.getVesselPositions()) {
            SetReportMovementType movementType = new SetReportMovementType();
            movementType.setMovement(FluxMessageResponseMapper.mapResponse(col, movement.getVesselID()));
            movementType.setPluginType(PluginType.FLUX);
            movementType.setPluginName(null);
            movementType.setTimestamp(DateUtil.createXMLGregorianCalendar(new Date()));
            
            movementList.add(movementType);
        }
        
        return movementList;
    }
    
}