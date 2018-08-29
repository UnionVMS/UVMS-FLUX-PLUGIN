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
package eu.europa.ec.fisheries.uvms.plugins.movement.service;

import eu.europa.ec.fisheries.schema.exchange.common.v1.AcknowledgeTypeType;
import eu.europa.ec.fisheries.schema.exchange.common.v1.CommandType;
import eu.europa.ec.fisheries.schema.exchange.common.v1.CommandTypeType;
import eu.europa.ec.fisheries.schema.exchange.common.v1.KeyValueType;
import eu.europa.ec.fisheries.schema.exchange.common.v1.ReportType;
import eu.europa.ec.fisheries.schema.exchange.common.v1.ReportTypeType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.EmailType;
import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PollType;
import eu.europa.ec.fisheries.schema.exchange.service.v1.SettingListType;
import eu.europa.ec.fisheries.uvms.plugins.movement.PortInitiator;
import eu.europa.ec.fisheries.uvms.plugins.movement.exception.PluginException;
import eu.europa.ec.fisheries.uvms.plugins.movement.message.FluxMessageSenderBean;
import java.util.UUID;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import lombok.extern.slf4j.Slf4j;

/**
 *
 */
@LocalBean
@Stateless
@Slf4j
public class PluginService {

    @EJB
    private StartupBean startupBean;

    @EJB
    private FluxMessageSenderBean sender;

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
                log.debug("Error when setting report");
                return AcknowledgeTypeType.NOK;
            }
        }
        return AcknowledgeTypeType.OK;
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
            for (KeyValueType values : settings.getSetting()) {
                log.debug("Setting [ " + values.getKey() + " : " + values.getValue() + " ]");
                startupBean.getSettings().put(values.getKey(), values.getValue());
            }
            portInintiator.updatePort();
            return AcknowledgeTypeType.OK;
        } catch (Exception e) {
            log.error("Failed to set config in {}", startupBean.getRegisterClassName());
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
            log.error("Failed to start {}", startupBean.getRegisterClassName());
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
            log.error("Failed to stop {}", startupBean.getRegisterClassName());
            return AcknowledgeTypeType.NOK;
        }
    }

}
