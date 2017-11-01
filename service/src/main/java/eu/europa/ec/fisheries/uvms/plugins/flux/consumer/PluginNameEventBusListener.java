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
package eu.europa.ec.fisheries.uvms.plugins.flux.consumer;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import eu.europa.ec.fisheries.schema.exchange.common.v1.AcknowledgeType;
import eu.europa.ec.fisheries.schema.exchange.common.v1.AcknowledgeTypeType;
import eu.europa.ec.fisheries.schema.exchange.plugin.v1.PingRequest;
import eu.europa.ec.fisheries.schema.exchange.plugin.v1.PluginBaseRequest;
import eu.europa.ec.fisheries.schema.exchange.plugin.v1.SetCommandRequest;
import eu.europa.ec.fisheries.schema.exchange.plugin.v1.SetConfigRequest;
import eu.europa.ec.fisheries.schema.exchange.plugin.v1.SetReportRequest;
import eu.europa.ec.fisheries.schema.exchange.plugin.v1.StartRequest;
import eu.europa.ec.fisheries.schema.exchange.plugin.v1.StopRequest;
import eu.europa.ec.fisheries.uvms.exchange.model.constant.ExchangeModelConstants;
import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.ExchangePluginResponseMapper;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.plugins.flux.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.flux.producer.PluginMessageProducer;
import eu.europa.ec.fisheries.uvms.plugins.flux.service.PluginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MessageDriven(mappedName = ExchangeModelConstants.PLUGIN_EVENTBUS, activationConfig = {
    @ActivationConfigProperty(propertyName = "messagingType", propertyValue = ExchangeModelConstants.CONNECTION_TYPE),
    @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = ExchangeModelConstants.DESTINATION_TYPE_TOPIC),
    @ActivationConfigProperty(propertyName = "destination", propertyValue = ExchangeModelConstants.EVENTBUS_NAME)
})
public class PluginNameEventBusListener implements MessageListener {

    final static Logger LOG = LoggerFactory.getLogger(PluginNameEventBusListener.class);

    @EJB
    PluginService service;

    @EJB
    PluginMessageProducer messageProducer;

    @EJB
    StartupBean startup;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void onMessage(Message inMessage) {

        LOG.debug("Eventbus listener for flux (MessageConstants.PLUGIN_SERVICE_CLASS_NAME): {}", startup.getRegisterClassName());

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
                    LOG.error("Not supported method");
                    break;
            }

            messageProducer.sendResponseMessage(responseMessage, textMessage);

        } catch (ExchangeModelMarshallException | NullPointerException e) {
            LOG.error("[ Error when receiving message in flux " + startup.getRegisterClassName() + " ]", e);
        } catch (JMSException ex) {
            LOG.error("[ Error when handling JMS message in flux " + startup.getRegisterClassName() + " ]", ex);
        }
    }
}