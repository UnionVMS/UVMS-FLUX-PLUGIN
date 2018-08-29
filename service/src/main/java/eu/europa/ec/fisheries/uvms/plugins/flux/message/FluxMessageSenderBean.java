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

import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.plugins.flux.PortInitiator;
import eu.europa.ec.fisheries.uvms.plugins.flux.exception.PluginException;
import eu.europa.ec.fisheries.uvms.plugins.flux.mapper.FluxMessageRequestMapper;
import eu.europa.ec.fisheries.uvms.plugins.flux.service.StartupBean;
import lombok.extern.slf4j.Slf4j;
import xeu.connector_bridge.v1.PostMsgOutType;
import xeu.connector_bridge.v1.PostMsgType;
import xeu.connector_bridge.wsdl.v1.BridgeConnectorPortType;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@LocalBean
@Stateless
@Slf4j
public class FluxMessageSenderBean {

    @EJB
    private PortInitiator port;

    @EJB
    private FluxMessageRequestMapper mapper;

    @EJB
    private StartupBean startupBean;

    public String sendMovement(MovementType movement, String messageId, String recipient) throws PluginException {
        try {

            log.info("Sending message to EU [ {} ] with messageID: {} ", messageId);

            BridgeConnectorPortType portType = port.getPort();

            //TODO Addd these in properties table
            Map<String, String> headerValues = new HashMap<>();

            String headerKey = startupBean.getSetting("CLIENT_CERT_HEADER");
            String headerValue = startupBean.getSetting("CLIENT_CERT_USER");

            headerValues.put(headerKey, headerValue);
            mapper.addHeaderValueToRequest(portType, headerValues);

            PostMsgType request = mapper.mapToRequest(movement, messageId, recipient);
            PostMsgOutType resp = portType.post(request);

            if (resp.getAssignedON() == null) {
                log.info("Failed to send to flux Recipient {}, Mesageid {} Should corralate with Movement GUID", recipient, messageId);
            } else {
                log.info("Success when sending to flux MessageId {} ( Should corralate with Movement GUID ) Recipient {} ", messageId, recipient);
            }

            if (request.getID() != null && !request.getID().isEmpty()) {
                return request.getID();
            } else {
                throw new PluginException("No MessageID in request, MessageID must be set!");
            }

        } catch (Exception e) {
            log.error("[ Error when sending movement to FLUX. ] {}", e.getMessage());
            throw new PluginException(e.getMessage());
        }
    }

}
