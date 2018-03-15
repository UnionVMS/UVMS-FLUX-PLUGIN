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

import static eu.europa.ec.fisheries.uvms.plugins.flux.service.ExchangeService.DF;
import static eu.europa.ec.fisheries.uvms.plugins.flux.service.ExchangeService.FR;
import static eu.europa.ec.fisheries.uvms.plugins.flux.service.ExchangeService.GUID;
import static eu.europa.ec.fisheries.uvms.plugins.flux.service.ExchangeService.ON;
import static eu.europa.ec.fisheries.uvms.plugins.flux.service.ExchangeService.USER;

import eu.europa.ec.fisheries.schema.exchange.movement.v1.SetReportMovementType;
import eu.europa.ec.fisheries.uvms.plugins.flux.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.flux.exception.PluginException;
import eu.europa.ec.fisheries.uvms.plugins.flux.mapper.FluxMessageResponseMapper;
import eu.europa.ec.fisheries.uvms.plugins.flux.service.ExchangeService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import lombok.extern.slf4j.Slf4j;
import org.jboss.ws.api.annotation.WebContext;
import xeu.bridge_connector.v1.RequestType;

@Stateless
@WebService(serviceName = "MovementService", targetNamespace = "urn:xeu:bridge-connector:wsdl:v1", portName = "BridgeConnectorPortType", endpointInterface = "xeu.bridge_connector.wsdl.v1.BridgeConnectorPortType")
@WebContext(contextRoot = "/flux-service")
@Slf4j
public class FluxMessageReceiverBean extends AbstractFluxReceiver {

    @EJB
    private ExchangeService exchange;

    @EJB
    private StartupBean startupBean;

    @Override
    protected void sendToExchange(RequestType rt) throws PluginException {
        List<SetReportMovementType> movements = FluxMessageResponseMapper.mapToReportMovementTypes(rt, startupBean.getRegisterClassName());
        log.info("[INFO] Going to send [" + movements.size() + "] movements to exchange.");
        final Map<String, String> msgProperties = extractMsgProperties(rt);
        for (SetReportMovementType movement : movements) {
            exchange.sendMovementReportToExchange(movement, msgProperties);
        }
        log.info("[INFO] Finished sending all movements to exchange.");
    }

    private Map<String, String> extractMsgProperties(RequestType rt) throws PluginException {
        Map<QName, String> attributes = rt.getOtherAttributes();
        Map<String, String> props = new HashMap<>();
        props.put(USER, attributes.get(new QName(USER)));
        props.put(ON, rt.getON());
        props.put(FR, attributes.get(new QName(FR)));
        props.put(DF, attributes.get(new QName(DF)));
        props.put(GUID, FluxMessageResponseMapper.extractMessageGUID(rt));
        return props;
    }

    @Override
    protected StartupBean getStartupBean() {
        return startupBean;
    }

    //@Override TODO : for the future we send the whole message to exchange instead of each movement separately...
 /*   protected void sendToExchangeNew(RequestType rt) {
        try {
            SetFLUXMovementReportRequest request = new SetFLUXMovementReportRequest();
            Map<QName, String> otherAttributes = rt.getOtherAttributes();
            final FLUXVesselPositionMessage xmlMessage = FluxMessageResponseMapper.extractVesselPositionMessage(rt.getAny());
            request.setRequest(marshallJaxBObjectToString(xmlMessage));
            String requestStr = createSetFLUXMovementReportRequest(request.getRequest(), otherAttributes.get(new QName(USER)), rt.getDF(),
                    DateUtils.nowUTC().toDate(), FluxMessageResponseMapper.extractMessageGUID(xmlMessage), PluginType.FLUX,
                    otherAttributes.get(new QName(FR)), rt.getON());
            pluginToExchangeProducer.sendModuleMessage(requestStr, null);
        } catch (JAXBException | MessageException | PluginException e) {
            throw new RuntimeException("Couldn't transform Element to Source", e);
        }
    }

    private String createSetFLUXMovementReportRequest(String message, String username, String fluxDFValue, Date date,
                                                      String messageGuid, PluginType pluginType, String senderReceiver, String onValue) throws JAXBException {
        SetFLUXMovementReportRequest request = new SetFLUXMovementReportRequest();
        request.setMethod(ExchangeModuleMethod.SET_MOVEMENT_REPORT);
        request.setRequest(message);
        populateBaseProperties(request, fluxDFValue, date, messageGuid, pluginType, senderReceiver, onValue, username);
        return marshallJaxBObjectToString(request, "Unicode", true);
    }

    private static void populateBaseProperties(ExchangeBaseRequest request, String fluxDFValue, Date date, String messageGuid, PluginType pluginType, String senderReceiver, String onValue, String username) {
        request.setUsername(username);
        request.setFluxDataFlow(fluxDFValue);
        request.setDate(date);
        request.setMessageGuid(messageGuid);
        request.setPluginType(pluginType);
        request.setSenderOrReceiver(senderReceiver);
        request.setOnValue(onValue);
    }
*/
}
