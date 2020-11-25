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
package eu.europa.ec.fisheries.uvms.plugins.flux.movement.message;

import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.RecipientInfoType;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.exception.PluginException;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.service.StartupBean;
import un.unece.uncefact.data.standard.fluxvesselpositionmessage._4.FLUXVesselPositionMessage;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.PortInitiator;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.constants.MovementPluginConstants;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.mapper.FluxMessageRequestMapper;
import xeu.connector_bridge.v1.PostMsgOutType;
import xeu.connector_bridge.v1.PostMsgType;
import xeu.connector_bridge.wsdl.v1.BridgeConnectorPortType;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Stateless
public class FluxMessageSenderBean {

    private static final Logger LOG = LoggerFactory.getLogger(FluxMessageSenderBean.class);

    @EJB
    private PortInitiator port;

    @EJB
    private StartupBean startupBean;

    public FLUXVesselPositionMessage sendMovement(MovementType movement, String recipient, List<RecipientInfoType> recipientInfo) throws PluginException {
        try {
            String messageId = UUID.randomUUID().toString();
            if (movement.getGuid() != null) {
                messageId = movement.getGuid();
            }

            LOG.info("Sending message to recipient {} with messageID: {} ", recipient, messageId);

            BridgeConnectorPortType portType = port.getPort();

            Map<String, String> headerValues = new HashMap<>();

            String headerKey = startupBean.getSetting(MovementPluginConstants.CLIENT_HEADER);
            String headerValue = startupBean.getSetting(MovementPluginConstants.CLIENT_HEADER_VALUE);

            headerValues.put(headerKey, headerValue + "-" + recipient);
            FluxMessageRequestMapper.addHeaderValueToRequest(portType, headerValues);

            // Find custom attributes for recipient
            String dataflow = getDataflow(recipientInfo);
            XMLGregorianCalendar todt = getRecipentTODT(recipient);

            FLUXVesselPositionMessage vesselPosition = FluxMessageRequestMapper.mapToFluxMovement(movement, startupBean.getSetting(MovementPluginConstants.OWNER_FLUX_PARTY));
            PostMsgType request = FluxMessageRequestMapper.mapToRequest(vesselPosition, messageId, recipient, dataflow, todt, startupBean.getSetting(MovementPluginConstants.FLUX_DEFAULT_AD));
            PostMsgOutType resp = portType.post(request);

            if (resp.getAssignedON() == null) {
                LOG.info("Failed to send report to recipient {}, response assignedOn is empty", recipient);
                throw new PluginException("Failed to send report to recipient");
            } else {
                LOG.info("Success when sending report with messageId {} to recipient {} ", request.getID(), recipient);
            }

            return vesselPosition;
        } catch (Exception e) {
            LOG.error("Error when sending movement to FLUX.", e);
            throw new PluginException(e.getMessage());
        }
    }

    private String getDataflow(List<RecipientInfoType> recipientInfo) {
        for (RecipientInfoType info : recipientInfo) {
            if (info.getKey().contains("FLUXVesselPositionMessage")) {
                return info.getKey();
            }
        }
        return startupBean.getSetting(MovementPluginConstants.FLUX_DATAFLOW);
    }

    private XMLGregorianCalendar getRecipentTODT(String recipient) {
        try {
            Map<String, String> todtMap = startupBean.getSettingsMap(MovementPluginConstants.TODT_MAP);
            if (todtMap.containsKey(recipient)) {
                Instant todt = Instant.now().truncatedTo(ChronoUnit.SECONDS).plus(Long.parseLong(todtMap.get(recipient)), ChronoUnit.MINUTES);
                return DatatypeFactory.newInstance().newXMLGregorianCalendar(todt.toString());
            }
        } catch (Exception e) {
            LOG.error("Could not find custom TODT for recipient {}", recipient, e);
        }
        return null;
    }
}
