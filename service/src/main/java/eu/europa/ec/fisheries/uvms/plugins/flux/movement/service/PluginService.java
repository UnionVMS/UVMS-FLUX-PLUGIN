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
package eu.europa.ec.fisheries.uvms.plugins.flux.movement.service;

import eu.europa.ec.fisheries.schema.exchange.common.v1.AcknowledgeTypeType;
import eu.europa.ec.fisheries.schema.exchange.common.v1.CommandType;
import eu.europa.ec.fisheries.schema.exchange.common.v1.CommandTypeType;
import eu.europa.ec.fisheries.schema.exchange.common.v1.KeyValueType;
import eu.europa.ec.fisheries.schema.exchange.common.v1.ReportType;
import eu.europa.ec.fisheries.schema.exchange.common.v1.ReportTypeType;
import eu.europa.ec.fisheries.schema.exchange.plugin.v1.PluginBaseRequest;
import eu.europa.ec.fisheries.schema.exchange.plugin.v1.SendFLUXMovementReportRequest;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.EmailType;
import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PollType;
import eu.europa.ec.fisheries.schema.exchange.service.v1.SettingListType;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.commons.message.impl.JAXBUtils;
import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.ExchangeModuleRequestMapper;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.constants.MovementPluginType;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.exception.MappingException;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.exception.PluginException;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.PortInitiator;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.message.FluxMessageSenderBean;

import java.util.List;
import java.util.UUID;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

import eu.europa.ec.fisheries.uvms.plugins.flux.movement.producer.FluxMessageProducerBean;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.producer.PluginToExchangeProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import un.unece.uncefact.data.standard.fluxvesselpositionmessage._4.FLUXVesselPositionMessage;
import xeu.connector_bridge.v1.AssignedONType;
import xeu.connector_bridge.v1.PostMsgOutType;
import xeu.connector_bridge.v1.PostMsgType;
import xeu.connector_bridge.wsdl.v1.BridgeConnectorPortType;

/**
 *
 */
@LocalBean
@Stateless
@Slf4j
public class PluginService {

    private static final String CLIENT_ID = "CLIENT_ID";
    private static final String CONNECTOR_ID = "connectorID";

    @EJB
    private StartupBean startupBean;

    @EJB
    private FluxMessageSenderBean sender;

    @EJB
    private FluxMessageProducerBean producer;

    @EJB
    private PluginToExchangeProducer exchangeProducer;

    @EJB
    private PortInitiator portInintiator;

    /**
     * TODO implement
     *
     * @param report
     * @return
     */
    public AcknowledgeTypeType setReport(ReportType report) {
        log.info(startupBean.getRegisterClassName() + ".report(" + report.getType().name() + ")");
        log.debug("timestamp: " + report.getTimestamp());
        MovementType movement = report.getMovement();
        if (movement != null && ReportTypeType.MOVEMENT.equals(report.getType())) {
            try {
                MovementPoint pos = movement.getPosition();
                if (pos != null) {
                    log.info("lon: " + pos.getLongitude());
                    log.info("lat: " + pos.getLatitude());
                }
                String editorType = startupBean.getSetting("EDITOR_TYPE");
                String actionReason = startupBean.getSetting("ACTION_REASON");

                String messageId = UUID.randomUUID().toString();
                if (movement.getGuid() != null) {
                    messageId = movement.getGuid();
                }
                sender.sendMovement(movement, messageId, report.getRecipient());
            } catch (PluginException ex) {
                log.debug("Error when setting report",ex);
                return AcknowledgeTypeType.NOK;
            }
        }
        return AcknowledgeTypeType.OK;
    }

    /**
     * TODO implement
     *
     * @param report
     * @return
     */
    public AcknowledgeTypeType sendFluxMovementReport(SendFLUXMovementReportRequest report) throws MappingException, JAXBException, DatatypeConfigurationException {
        log.info(startupBean.getRegisterClassName() + ".report(" + "forward position report" + ")");
        if (report != null) {
            try {
                if (portInintiator.isWsSetup()) {
                    sendMessageThroughWs(report, MovementPluginType.SEND_MOVEMENT_REPORT);
                } else {
                    sendMessageThroughJms(report, MovementPluginType.SEND_MOVEMENT_REPORT);
                }
            } catch (MessageException ex) {
                log.debug("Error when sending flux movement report");
                return AcknowledgeTypeType.NOK;
            }
        }
        return AcknowledgeTypeType.OK;
    }

    private void sendMessageThroughJms(SendFLUXMovementReportRequest report, MovementPluginType type) throws MessageException {
        producer.sendMessageToBridgeQueue(report, type);
    }

    private void sendMessageThroughWs(PluginBaseRequest request, MovementPluginType type) throws JAXBException, DatatypeConfigurationException, MappingException {
        log.info("[WEBSERVICE] Sending message through :::-->>> WS\n\n");
        PostMsgType postMsgType = getPostMsgType(request, type);
        int waitingTimes = 120;
        while (portInintiator.isWaitingForUrlConfigProperty() && waitingTimes > 0) {
            try {
                log.warn("Webservice needs to wait for the URL to be set up. Waiting for the {} time (MAX 60 Times)", waitingTimes);
                Thread.sleep(1000);
                waitingTimes--;
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new MappingException("Thread interrupted...",ie);
            }
        }
        BridgeConnectorPortType port = portInintiator.getPort();
        BindingProvider bp = (BindingProvider) port;
        // todo check why there is no setting for fluxmovement plugin client id! from front end config tab
        // so we hardwire it
        startupBean.getSettings().put(startupBean.getRegisterClassName() + "." + CLIENT_ID, "flux-movement-plugin");
        bp.getRequestContext().put(CONNECTOR_ID, startupBean.getSetting(CLIENT_ID));
        String endPoint = ((BindingProvider) port).getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY).toString();
        try {
            PostMsgOutType post = port.post(postMsgType);
            List<AssignedONType> assignedON = post.getAssignedON();
            upgradeResponseWithOnMessage(assignedON, request.getResponseLogGuid());
            log.info("[INFO] Outgoing message ({}) with ON :[{}] send to [{}]", type, request.getOnValue(), endPoint);
        } catch (WebServiceException | NullPointerException ex) {
            log.error("[ERROR] Couldn't send message to {}", endPoint, ex.getCause());
        }
    }

    private void upgradeResponseWithOnMessage(List<AssignedONType> assignedOn, String responseGuid) {
        String onValue = assignedOn.isEmpty() ? null : assignedOn.get(0).getON();
        try {
            String stringMessage = ExchangeModuleRequestMapper.createUpdateOnMessageRequest(onValue, responseGuid);
            exchangeProducer.sendModuleMessage(stringMessage, null);
        } catch (ExchangeModelMarshallException | MessageException e) {
            e.printStackTrace();
        }
    }

    private PostMsgType getPostMsgType(PluginBaseRequest request, MovementPluginType msgType) throws DatatypeConfigurationException, MappingException, JAXBException {
        PostMsgType postMsgType = new PostMsgType();
        postMsgType.setAD(request.getDestination());
        postMsgType.setDF(request.getFluxDataFlow());
        String response;
        if (MovementPluginType.SEND_MOVEMENT_REPORT.equals(msgType)) {
            response = ((SendFLUXMovementReportRequest) request).getReport();
            postMsgType.setAny(marshalToDOM(JAXBUtils.unMarshallMessage(response, FLUXVesselPositionMessage.class)));
        }
        return postMsgType;
    }

    private Element marshalToDOM(Object toBeWrapped) throws MappingException {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(toBeWrapped.getClass());
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.newDocument();
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal(toBeWrapped, document);
            return document.getDocumentElement();
        } catch (ParserConfigurationException | JAXBException e) {
            throw new MappingException("Could not wrap object " + toBeWrapped + " in post msg", e);
        }
    }

    /**
     * TODO implement
     *
     * @param command
     * @return
     */
    public AcknowledgeTypeType setCommand(CommandType command) {
        log.info(startupBean.getRegisterClassName() + ".setCommand(" + command.getCommand().name() + ")");
        log.debug("timestamp: " + command.getTimestamp());
        PollType poll = command.getPoll();
        EmailType email = command.getEmail();
        if (poll != null && CommandTypeType.POLL.equals(command.getCommand())) {
            log.info("POLL: " + poll.getPollId());
        }
        if (email != null && CommandTypeType.EMAIL.equals(command.getCommand())) {
            log.info("EMAIL: subject=" + email.getSubject());
        }
        return AcknowledgeTypeType.OK;
    }

    /**
     * Set the config values for the movement
     *
     * @param settings
     * @return
     */
    public AcknowledgeTypeType setConfig(SettingListType settings) {
        log.info(startupBean.getRegisterClassName() + ".setConfig()");
        try {
            for (KeyValueType setting : settings.getSetting()) {
                log.debug("Setting [ " + setting.getKey() + " : " + setting.getValue() + " ]");
                startupBean.getSettings().put(setting.getKey(), setting.getValue());

                if (setting.getKey().endsWith("FLUX_ENDPOINT")) {
                    if (StringUtils.isNotEmpty(setting.getValue())) {
                        portInintiator.setWsSetup(true);
                        log.info("Mark as activated the WS OUT service with endpoint : [{}]", setting.getValue());
                    } else {
                        log.warn("WS OUT endpoint has been marked as deactivated since the endpont value is NULL!");
                        portInintiator.setWsSetup(false);
                    }
                }

            }
            portInintiator.updatePort();
            if (portInintiator.isWsSetup()) {
                portInintiator.setWaitingForUrlConfigProperty(false);
            } else {
                portInintiator.setWaitingForUrlConfigProperty(true);
            }
            return AcknowledgeTypeType.OK;
        } catch (Exception e) {
            log.error("Failed to set config in " + startupBean.getRegisterClassName(),e);
            return AcknowledgeTypeType.NOK;
        }

    }

    /**
     * Start the movement. Use this to enable functionality in the movement
     *
     * @return
     */
    public AcknowledgeTypeType start() {
        log.info(startupBean.getRegisterClassName() + ".start()");
        try {
            startupBean.setIsEnabled(Boolean.TRUE);
            return AcknowledgeTypeType.OK;
        } catch (Exception e) {
            startupBean.setIsEnabled(Boolean.FALSE);
            log.error("Failed to start " + startupBean.getRegisterClassName(),e);
            return AcknowledgeTypeType.NOK;
        }

    }

    /**
     * Start the movement. Use this to disable functionality in the movement
     *
     * @return
     */
    public AcknowledgeTypeType stop() {
        log.info(startupBean.getRegisterClassName() + ".stop()");
        try {
            startupBean.setIsEnabled(Boolean.FALSE);
            return AcknowledgeTypeType.OK;
        } catch (Exception e) {
            startupBean.setIsEnabled(Boolean.TRUE);
            log.error("Failed to stop " + startupBean.getRegisterClassName(),e);
            return AcknowledgeTypeType.NOK;
        }
    }

}
