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

package eu.europa.ec.fisheries.uvms.plugins.flux.service;

import static eu.europa.ec.fisheries.uvms.plugins.flux.constants.ActivityType.UNKNOWN;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.Date;
import java.util.UUID;

import eu.europa.ec.fisheries.schema.exchange.movement.v1.SetReportMovementType;
import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginType;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.ExchangeModuleRequestMapper;
import eu.europa.ec.fisheries.uvms.plugins.flux.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.flux.constants.ActivityType;
import eu.europa.ec.fisheries.uvms.plugins.flux.producer.PluginToExchangeProducer;
import eu.europa.ec.fisheries.uvms.plugins.flux.util.SaxParserUUIDExtractor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

@LocalBean
@Stateless
@Slf4j
public class ExchangeService {


    private static final String exchangeUsername = "flux";
    private static final String DF = "DF";
    private static final String FR = "FR";
    private static final String ON = "ON";

    @EJB
    private StartupBean startupBean;

    @EJB
    private PluginToExchangeProducer producer;

    public void sendMovementReportToExchange(SetReportMovementType reportType) {
        try {
            String text = ExchangeModuleRequestMapper.createSetMovementReportRequest(reportType, "FLUX");
            String messageId = producer.sendModuleMessage(text, null);
            startupBean.getCachedMovement().put(messageId, reportType);
        } catch (ExchangeModelMarshallException e) {
            log.error("Couldn't map movement to setreportmovementtype");
        } catch (MessageException e) {
            log.error("Couldn't send movement");
            startupBean.getCachedMovement().put(UUID.randomUUID().toString(), reportType);
        }
    }

    public void sendActivityReportToExchange(String fluxFAReportRequest) {
        String messageId = null;
        try {
            messageId = producer.sendModuleMessage(fluxFAReportRequest, null);
        } catch (MessageException e) {
            e.printStackTrace();
        }
        log.info("[END] Request object created and Message sent to [EXCHANGE] module : " + messageId);

    }

    /**
     * Create object with all necessary properties required to communicate with exchange
     *
     * @param textMessage
     * @return
     * @throws JMSException
     */
    private ExchangeMessageProperties createExchangeMessagePropertiesForFluxFAReportRequest(TextMessage textMessage, ActivityType type) throws JMSException {
        ExchangeMessageProperties exchangeMessageProperties = new ExchangeMessageProperties();
        exchangeMessageProperties.setUsername(exchangeUsername);
        exchangeMessageProperties.setDate(new Date());
        exchangeMessageProperties.setPluginType(PluginType.FLUX);
        exchangeMessageProperties.setDFValue(extractStringPropertyFromJMSTextMessage(textMessage, DF));
        exchangeMessageProperties.setSenderReceiver(extractStringPropertyFromJMSTextMessage(textMessage, FR));
        exchangeMessageProperties.setOnValue(extractStringPropertyFromJMSTextMessage(textMessage, ON));
        exchangeMessageProperties.setMessageGuid(type != UNKNOWN ? extractMessageGuidFromInputXML(textMessage.getText(), type) : StringUtils.EMPTY);
        log.info("Properties read from the message:" + exchangeMessageProperties);
        return exchangeMessageProperties;
    }


    @Data
    public class ExchangeMessageProperties {
        private String username;
        private String reportType;
        private String DFValue;
        private String onValue;
        private Date date;
        private PluginType pluginType;
        private String senderReceiver;
        private String messageGuid;
    }

    //Extract UUID value from FLUXReportDocument as messageGuid
    private String extractMessageGuidFromInputXML(String message, ActivityType type) {
        String messageGuid = null;
        SaxParserUUIDExtractor saxParserForFaFLUXMessge = new SaxParserUUIDExtractor(type);
        try {
            saxParserForFaFLUXMessge.parseDocument(message);
        } catch (SAXException | NullPointerException e) {
            messageGuid = saxParserForFaFLUXMessge.getUuidValue();
        }
        return messageGuid;
    }

    private String extractStringPropertyFromJMSTextMessage(TextMessage textMessage, String property) {
        String value = null;
        try {
            value = textMessage.getStringProperty(property);
        } catch (JMSException e) {
            log.error("Couldn't find the property [ " + property + " ] in JMS Text Message:" + property, e);
        }
        return value;
    }
}