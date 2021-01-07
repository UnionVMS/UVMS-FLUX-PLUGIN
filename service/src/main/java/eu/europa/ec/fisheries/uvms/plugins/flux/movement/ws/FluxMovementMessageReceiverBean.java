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

package eu.europa.ec.fisheries.uvms.plugins.flux.movement.ws;

import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginType;
import eu.europa.ec.fisheries.uvms.commons.date.DateUtils;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.commons.message.impl.JAXBUtils;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.ExchangeModuleRequestMapper;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.exception.PluginException;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.producer.PluginToExchangeProducer;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.service.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.mapper.FluxMessageResponseMapper;
import lombok.extern.slf4j.Slf4j;
import org.jboss.ws.api.annotation.WebContext;
import un.unece.uncefact.data.standard.fluxvesselpositionmessage._4.FLUXVesselPositionMessage;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import java.util.Map;

@Stateless
@WebService(serviceName = "MovementService", targetNamespace = "urn:xeu:bridge-connector:wsdl:v1", portName = "BridgeConnectorPortType", endpointInterface = "eu.europa.ec.fisheries.uvms.plugins.flux.movement.ws.BridgeConnectorPortTypeForTypedPayload")
@WebContext(contextRoot = "/unionvms/movement-service")
@Slf4j
public class FluxMovementMessageReceiverBean extends AbstractFluxReceiver {

    private static final QName FR = new QName("FR");
    private static final QName USER = new QName("USER");
    private static final QName AD = new QName("AD");
    private static final QName TO = new QName("TO");
    private static final QName TODT = new QName("TODT");

    private static final String PLUGIN_ID = "FLUX_VP_PLUGIN_WS";

    @EJB
    private StartupBean startupBean;

    @EJB
    private PluginToExchangeProducer pluginToExchangeProducer;

    @Override
    protected StartupBean getStartupBean() {
        return startupBean;
    }

    @Override
    protected void sendToExchange(RequestTypeWithTypedPayload rt) {
        try {
            log.info("Received a Movement message in Movement Plugin..");
            Map<QName, String> attributes = rt.getOtherAttributes();
            FLUXVesselPositionMessage message = (FLUXVesselPositionMessage) rt.getAny();
            String guid = FluxMessageResponseMapper.extractMessageGUID(message);
            String requestStr = ExchangeModuleRequestMapper.createSetFLUXMovementReportRequest(JAXBUtils.marshallJaxBObjectToString(message), PLUGIN_ID, rt.getDF(),
                    DateUtils.nowUTC().toDate(), guid, PluginType.FLUX,
                    attributes.get(FR), rt.getON(), guid, startupBean.getRegisterClassName(),
                    attributes.get(AD), attributes.get(TO), attributes.get(TODT));
            pluginToExchangeProducer.sendModuleMessage(requestStr, null);
            log.info("Movement message successfully sent to Exchange..");
        } catch (JAXBException | MessageException | PluginException e) {
            throw new RuntimeException("Couldn't transform Element to Source", e);
        }
    }
}
