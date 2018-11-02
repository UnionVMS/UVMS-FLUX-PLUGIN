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

import eu.europa.ec.fisheries.schema.exchange.common.v1.AcknowledgeType;
import eu.europa.ec.fisheries.schema.exchange.common.v1.AcknowledgeTypeType;
import eu.europa.ec.fisheries.schema.exchange.plugin.v1.*;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.ExchangePluginResponseMapper;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.constants.MovementPluginConstants;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.producer.PluginToExchangeProducer;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.service.PluginService;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.service.StartupBean;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.*;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@MessageDriven(mappedName = MessageConstants.EVENT_BUS_TOPIC, activationConfig = {
        @ActivationConfigProperty(propertyName = MessageConstants.MESSAGING_TYPE_STR,          propertyValue = MessageConstants.CONNECTION_TYPE),
        @ActivationConfigProperty(propertyName = MessageConstants.SUBSCRIPTION_DURABILITY_STR, propertyValue = MessageConstants.DURABLE_CONNECTION),
        @ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_TYPE_STR,        propertyValue = MessageConstants.DESTINATION_TYPE_TOPIC),
        @ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_STR,            propertyValue = MessageConstants.EVENT_BUS_TOPIC_NAME),
        @ActivationConfigProperty(propertyName = MessageConstants.SUBSCRIPTION_NAME_STR,       propertyValue = MovementPluginConstants.SUBSCRIPTION_NAME_EV),
        @ActivationConfigProperty(propertyName = MessageConstants.CLIENT_ID_STR,               propertyValue = MovementPluginConstants.CLIENT_ID_EV),
        @ActivationConfigProperty(propertyName = MessageConstants.MESSAGE_SELECTOR_STR,        propertyValue = MovementPluginConstants.MESSAGE_SELECTOR_EV)
})
@Slf4j
public class PluginNameEventBusListener implements MessageListener {

    @EJB
    private PluginService service;

    @EJB
    private PluginToExchangeProducer messageProducer;

    @EJB
    private StartupBean startup;

    @Override
    public void onMessage(Message inMessage) {
        log.debug("Eventbus listener for movement (MessageConstants.PLUGIN_SERVICE_CLASS_NAME): {}", startup.getRegisterClassName());
        TextMessage textMessage = (TextMessage) inMessage;
        try {
            PluginBaseRequest request = JAXBMarshaller.unmarshallTextMessage(textMessage, PluginBaseRequest.class);
            String responseMessage = null;
            switch (request.getMethod()) {
                case SET_CONFIG:
                    SetConfigRequest setConfigRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, SetConfigRequest.class);
                    AcknowledgeTypeType setConfig = service.setConfig(setConfigRequest.getConfigurations());
                    AcknowledgeType setConfigAck = ExchangePluginResponseMapper.mapToAcknowlegeType(textMessage.getJMSMessageID(), setConfig);
                    responseMessage = ExchangePluginResponseMapper.mapToSetConfigResponse(startup.getRegisterClassName(), setConfigAck);
                    break;
                case SET_COMMAND:
                    SetCommandRequest setCommandRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, SetCommandRequest.class);
                    AcknowledgeTypeType setCommand = service.setCommand(setCommandRequest.getCommand());
                    AcknowledgeType setCommandAck = ExchangePluginResponseMapper.mapToAcknowlegeType(textMessage.getJMSMessageID(), setCommand);
                    responseMessage = ExchangePluginResponseMapper.mapToSetCommandResponse(startup.getRegisterClassName(), setCommandAck);
                    break;
                case SET_REPORT:
                    SetReportRequest setReportRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, SetReportRequest.class);
                    AcknowledgeTypeType setReport = service.setReport(setReportRequest.getReport());
                    AcknowledgeType setReportAck = ExchangePluginResponseMapper.mapToAcknowlegeType(textMessage.getJMSMessageID(), setReport);
                    responseMessage = ExchangePluginResponseMapper.mapToSetReportResponse(startup.getRegisterClassName(), setReportAck);
                    break;
                case START:
                    StartRequest startRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, StartRequest.class);
                    AcknowledgeTypeType start = service.start();
                    AcknowledgeType startAck = ExchangePluginResponseMapper.mapToAcknowlegeType(textMessage.getJMSMessageID(), start);
                    responseMessage = ExchangePluginResponseMapper.mapToStartResponse(startup.getRegisterClassName(), startAck);
                    break;
                case STOP:
                    StopRequest stopRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, StopRequest.class);
                    AcknowledgeTypeType stop = service.stop();
                    AcknowledgeType stopAck = ExchangePluginResponseMapper.mapToAcknowlegeType(textMessage.getJMSMessageID(), stop);
                    responseMessage = ExchangePluginResponseMapper.mapToStopResponse(startup.getRegisterClassName(), stopAck);
                    break;
                case PING:
                    PingRequest pingRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, PingRequest.class);
                    responseMessage = ExchangePluginResponseMapper.mapToPingResponse(startup.isIsEnabled(), startup.isIsEnabled());
                    break;
                default:
                    log.error("Not supported method");
                    break;
            }
            messageProducer.sendResponseMessageToSender(textMessage, responseMessage);
        } catch (ExchangeModelMarshallException | NullPointerException e) {
            log.error("[ Error when receiving message in movement " + startup.getRegisterClassName() + " ]", e);
        } catch (JMSException | MessageException ex) {
            log.error("[ Error when handling JMS message in movement " + startup.getRegisterClassName() + " ]", ex);
        }
    }
}