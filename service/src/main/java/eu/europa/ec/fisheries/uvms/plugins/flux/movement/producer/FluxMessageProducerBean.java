/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

*/
package eu.europa.ec.fisheries.uvms.plugins.flux.movement.producer;

import eu.europa.ec.fisheries.schema.exchange.plugin.v1.PluginBaseRequest;
import eu.europa.ec.fisheries.schema.exchange.plugin.v1.SendFLUXMovementReportRequest;
import eu.europa.ec.fisheries.schema.exchange.plugin.v1.SendFLUXMovementRequest;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.commons.message.impl.AbstractProducer;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.constants.MovementPluginType;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.util.*;

@Stateless
@LocalBean
@Slf4j
public class FluxMessageProducerBean extends AbstractProducer {

    private static final String FLUX_ENV_AD = "AD";
    private static final String FLUX_ENV_DF = "DF";
    private static final String BUSINESS_UUID = "BUSINESS_UUID";
    private static final String FLUX_ENV_FR = "FR";
    private static final String ON = "ON";

    @Override
    public String getDestinationName() {
        return MessageConstants.QUEUE_PLUGIN_BRIDGE;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void sendMessageToBridgeQueue(PluginBaseRequest request, MovementPluginType msgType) throws MessageException {
        log.info("[JMS] Sending message outside of FLUX-FMC through :::-->>> JMS");
        String xmlMessage;
        if (MovementPluginType.SEND_MOVEMENT_REPORT.equals(msgType)){
            xmlMessage = ((SendFLUXMovementReportRequest) request).getReport();
        } else
        if (MovementPluginType.SEND_MOVEMENT.equals(msgType)){
            xmlMessage = ((SendFLUXMovementRequest) request).getReport();
        } else {
            throw new IllegalArgumentException("The message forwarded from Exchange cannot be handeled by th system");
        }
        sendModuleMessageWithProps(xmlMessage, getDestination(), getFLUXMessageProperties(request));
        log.info("[INFO] Outgoing message ({}) with ON :[{}] send to [{}] \n\n", msgType, request.getOnValue(), request.getDestination());
    }

    public Map<String, String> getFLUXMessageProperties(PluginBaseRequest pluginReq) {
        Map<String, String> messageProperties = new HashMap<>();
        if (pluginReq != null) {
            messageProperties.put(FLUX_ENV_AD, pluginReq.getDestination());
            messageProperties.put(FLUX_ENV_FR, pluginReq.getSenderOrReceiver());
            messageProperties.put(FLUX_ENV_DF, pluginReq.getFluxDataFlow());
            messageProperties.put(BUSINESS_UUID, pluginReq.getOnValue());
            messageProperties.put(ON, createBusinessUUID());
        } else {
            log.error("PluginBaseRequest is null so, could not set AD/FR/DF values to the FLUXMessage");
        }
        return messageProperties;
    }

    /**
     * BUSINESS_UUID has a prefix, a date-time combination and a serial - thus it is semi unique
     *
     * @return randomUUID
     */
    private String createBusinessUUID() {
        return UUID.randomUUID().toString();
    }
}

