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
package eu.europa.ec.fisheries.uvms.plugins.flux.movement.consumer;

import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginFault;
import eu.europa.ec.fisheries.schema.exchange.registry.v1.ExchangeRegistryBaseRequest;
import eu.europa.ec.fisheries.schema.exchange.registry.v1.RegisterServiceResponse;
import eu.europa.ec.fisheries.schema.exchange.registry.v1.UnregisterServiceResponse;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.constants.MovementPluginConstants;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.service.PluginService;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.service.StartupBean;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.*;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;


@MessageDriven(mappedName = MessageConstants.EVENT_BUS_TOPIC, activationConfig = {
        @ActivationConfigProperty(propertyName = MessageConstants.MESSAGING_TYPE_STR, propertyValue = MessageConstants.CONNECTION_TYPE),
        @ActivationConfigProperty(propertyName = MessageConstants.SUBSCRIPTION_DURABILITY_STR, propertyValue = MessageConstants.DURABLE_CONNECTION),
        @ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_TYPE_STR, propertyValue = MessageConstants.DESTINATION_TYPE_TOPIC),
        @ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_STR, propertyValue = MessageConstants.EVENT_BUS_TOPIC_NAME),
        @ActivationConfigProperty(propertyName = MessageConstants.SUBSCRIPTION_NAME_STR, propertyValue = MovementPluginConstants.SUBSCRIPTION_NAME_AC),
        @ActivationConfigProperty(propertyName = MessageConstants.CLIENT_ID_STR, propertyValue = MovementPluginConstants.CLIENT_ID_AC),
        @ActivationConfigProperty(propertyName = MessageConstants.MESSAGE_SELECTOR_STR, propertyValue = MovementPluginConstants.MESSAGE_SELECTOR_AC)
})
@Slf4j
public class PluginAckEventBusListener implements MessageListener {

    @EJB
    private StartupBean startupService;

    @EJB
    private PluginService fluxService;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void onMessage(Message inMessage) {
        log.info("Eventbus listener for movement at selector: {} got a message", startupService.getPluginResponseSubscriptionName());
        TextMessage textMessage = (TextMessage) inMessage;
        try {
            ExchangeRegistryBaseRequest request = tryConsumeRegistryBaseRequest(textMessage);
            if (request == null) {
                PluginFault fault = JAXBMarshaller.unmarshallTextMessage(textMessage, PluginFault.class);
                handlePluginFault(fault);
            } else {
                switch (request.getMethod()) {
                    case REGISTER_SERVICE:
                        RegisterServiceResponse registerResponse = JAXBMarshaller.unmarshallTextMessage(textMessage, RegisterServiceResponse.class);
                        startupService.setWaitingForResponse(Boolean.FALSE);
                        switch (registerResponse.getAck().getType()) {
                            case OK:
                                log.info("Register OK");
                                startupService.setIsRegistered(Boolean.TRUE);
                                break;
                            case NOK:
                                log.info("Register NOK: " + registerResponse.getAck().getMessage());
                                startupService.setIsRegistered(Boolean.FALSE);
                                break;
                            default:
                                log.error("[ Type not supperted: ]" + request.getMethod());
                        }
                        break;
                    case UNREGISTER_SERVICE:
                        UnregisterServiceResponse unregisterResponse = JAXBMarshaller.unmarshallTextMessage(textMessage, UnregisterServiceResponse.class);
                        switch (unregisterResponse.getAck().getType()) {
                            case OK:
                                log.info("Unregister OK");
                                break;
                            case NOK:
                                log.info("Unregister NOK");
                                break;
                            default:
                                log.error("[ Ack type not supported ] ");
                                break;
                        }
                        break;
                    default:
                        log.error("Not supported method");
                        break;
                }
            }
        } catch (ExchangeModelMarshallException | NullPointerException e) {
            log.error("[ Error when receiving message in movement ]", e);
        }
    }

    private void handlePluginFault(PluginFault fault) {
        log.error(startupService.getPluginResponseSubscriptionName() + " received fault " + fault.getCode() + " : " + fault.getMessage());
    }

    private ExchangeRegistryBaseRequest tryConsumeRegistryBaseRequest(TextMessage textMessage) {
        try {
            return JAXBMarshaller.unmarshallTextMessage(textMessage, ExchangeRegistryBaseRequest.class);
        } catch (ExchangeModelMarshallException e) {
            return null;
        }
    }
}