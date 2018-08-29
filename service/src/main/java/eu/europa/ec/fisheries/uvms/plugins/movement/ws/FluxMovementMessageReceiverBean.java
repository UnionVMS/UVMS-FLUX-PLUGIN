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

package eu.europa.ec.fisheries.uvms.plugins.movement.ws;

import eu.europa.ec.fisheries.schema.exchange.module.v1.SetFLUXMovementReportRequest;
import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginType;
import eu.europa.ec.fisheries.uvms.commons.date.DateUtils;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.commons.message.impl.JAXBUtils;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.ExchangeModuleRequestMapper;
import eu.europa.ec.fisheries.uvms.plugins.movement.exception.PluginException;
import eu.europa.ec.fisheries.uvms.plugins.movement.mapper.FluxMessageResponseMapper;
import eu.europa.ec.fisheries.uvms.plugins.movement.producer.PluginToExchangeProducer;
import eu.europa.ec.fisheries.uvms.plugins.movement.service.ExchangeService;
import eu.europa.ec.fisheries.uvms.plugins.movement.service.StartupBean;
import lombok.extern.slf4j.Slf4j;
import org.jboss.ws.api.annotation.WebContext;
import un.unece.uncefact.data.standard.fluxvesselpositionmessage._4.FLUXVesselPositionMessage;
import xeu.bridge_connector.v1.RequestType;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import java.util.Map;

@Stateless
@WebService(serviceName = "MovementService", targetNamespace = "urn:xeu:bridge-connector:wsdl:v1", portName = "BridgeConnectorPortType", endpointInterface = "xeu.bridge_connector.wsdl.v1.BridgeConnectorPortType")
@WebContext(contextRoot = "/unionvms/movement-service")
@Slf4j
public class FluxMovementMessageReceiverBean extends AbstractFluxReceiver {

    private static final String FR = "FR";
    private static final String USER = "USER";
    private static final String AD = "AD";
    private static final String TO = "TO";
    private static final String TODT = "TODT";

    @EJB
    private ExchangeService exchange;

    @EJB
    private StartupBean startupBean;

    @EJB
    private PluginToExchangeProducer pluginToExchangeProducer;

    @Override
    protected StartupBean getStartupBean() {
        return startupBean;
    }

    @Override
    protected void sendToExchange(RequestType rt) {
        try {
            log.info("Received a Movement message in Movement Plugin..");
            SetFLUXMovementReportRequest request = new SetFLUXMovementReportRequest();
            Map<QName, String> attributes = rt.getOtherAttributes();
            FLUXVesselPositionMessage xmlMessage = FluxMessageResponseMapper.extractVesselPositionMessage(rt.getAny());
            request.setRequest(JAXBUtils.marshallJaxBObjectToString(xmlMessage));
            String requestStr = ExchangeModuleRequestMapper.createSetFLUXMovementReportRequest(request.getRequest(), attributes.get(new QName(USER)), rt.getDF(),
                    DateUtils.nowUTC().toDate(), FluxMessageResponseMapper.extractMessageGUID(xmlMessage), PluginType.FLUX,
                    attributes.get(new QName(FR)), rt.getON(), FluxMessageResponseMapper.extractMessageGUID(rt), startupBean.getRegisterClassName(),
                    attributes.get(new QName(AD)), attributes.get(new QName(TO)), attributes.get(new QName(TODT)));
            pluginToExchangeProducer.sendModuleMessage(requestStr, null);
            log.info("Movement message succesfully sent to Exchange..");
        } catch (JAXBException | MessageException | PluginException e) {
            throw new RuntimeException("Couldn't transform Element to Source", e);
        }
    }

}
