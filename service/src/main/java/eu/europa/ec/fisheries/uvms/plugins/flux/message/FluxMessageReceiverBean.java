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
package eu.europa.ec.fisheries.uvms.plugins.flux.message;

import eu.europa.ec.fisheries.schema.exchange.movement.v1.SetReportMovementType;
import eu.europa.ec.fisheries.uvms.plugins.flux.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.flux.mapper.FluxMessageResponseMapper;
import eu.europa.ec.fisheries.uvms.plugins.flux.service.ExchangeService;
import eu.europa.ec.fisheries.uvms.service.client.flux.movement.contract.BasicAttribute;
import eu.europa.ec.fisheries.uvms.service.client.flux.movement.contract.ExchangeDocumentInfoType;
import java.util.List;
import javax.ejb.EJB;

import javax.ejb.Stateless;
import javax.jws.WebService;

import eu.europa.ec.fisheries.uvms.service.client.flux.movement.contract.BasicAttribute;
import eu.europa.ec.fisheries.uvms.service.client.flux.movement.contract.ExchangeDocumentInfoType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xeu.bridge_connector.v1.RequestType;
import xeu.bridge_connector.v1.ResponseType;
import xeu.bridge_connector.wsdl.v1.BridgeConnectorPortType;

/**
 **/
@Stateless
@WebService(serviceName = "MovementService", targetNamespace = "urn:xeu:bridge-connector:wsdl:v1", portName = "BridgeConnectorPortType", endpointInterface = "xeu.bridge_connector.wsdl.v1.BridgeConnectorPortType")
public class FluxMessageReceiverBean implements BridgeConnectorPortType {

    private static Logger LOG = LoggerFactory.getLogger(FluxMessageReceiverBean.class);

    @EJB
    ExchangeService exchange;

    @EJB
    StartupBean startupBean;

    @Override
    public ResponseType post(RequestType rt) {
        ResponseType type = new ResponseType();
        if (!startupBean.isIsEnabled()) {
            type.setStatus("NOK");
            return type;
        }
        try {

            LOG.debug("Got positionreport request from FLUX in FLUX plugin");

            BasicAttribute response = FluxMessageResponseMapper.extractBasicAttribute(rt.getAny());
            String correlationId = FluxMessageResponseMapper.extractCorrelationId(response);
            ExchangeDocumentInfoType extractMovement = FluxMessageResponseMapper.extractMovement(response);

            List<SetReportMovementType> movements = FluxMessageResponseMapper.mapToMovementType(extractMovement, startupBean.getRegisterClassName());

            for (SetReportMovementType movement : movements) {
                exchange.sendMovementReportToExchange(movement);
            }

            type.setStatus("OK");
            return type;
        } catch (Exception e) {
            LOG.error("[ Error when getting data. ] {} {}", e.getMessage(), e.getStackTrace());
            type.setStatus("NOK");
            return type;
        }
    }

}