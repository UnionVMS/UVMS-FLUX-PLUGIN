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

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.bind.JAXBException;
import java.util.List;

import eu.europa.ec.fisheries.schema.exchange.movement.v1.SetReportMovementType;
import eu.europa.ec.fisheries.uvms.plugins.flux.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.flux.exception.PluginException;
import eu.europa.ec.fisheries.uvms.plugins.flux.mapper.FluxMessageResponseMapper;
import eu.europa.ec.fisheries.uvms.plugins.flux.service.ExchangeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xeu.bridge_connector.v1.RequestType;

/**
 *
 */
@Stateless
@WebService(serviceName = "MovementService", targetNamespace = "urn:xeu:bridge-connector:wsdl:v1", portName = "BridgeConnectorPortType", endpointInterface = "xeu.bridge_connector.wsdl.v1.BridgeConnectorPortType")
public class FluxMessageReceiverBean extends AbstractFluxReceiver {

    private static Logger LOG = LoggerFactory.getLogger(FluxMessageReceiverBean.class);

    @EJB
    ExchangeService exchange;

    @EJB
    StartupBean startupBean;


    @Override protected void sendToExchange(RequestType rt) throws JAXBException, PluginException {

        List<SetReportMovementType> movements = FluxMessageResponseMapper.mapToMovementType(rt, startupBean.getRegisterClassName());

        for (SetReportMovementType movement : movements) {
            exchange.sendMovementReportToExchange(movement);
        }
    }


    @Override protected StartupBean getStartupBean() {
        return startupBean;
    }
}
