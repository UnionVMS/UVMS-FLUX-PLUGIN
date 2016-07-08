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

import java.util.UUID;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.schema.exchange.movement.v1.SetReportMovementType;
import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.ExchangeModuleRequestMapper;
import eu.europa.ec.fisheries.uvms.plugins.flux.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.flux.constants.ModuleQueue;
import eu.europa.ec.fisheries.uvms.plugins.flux.producer.PluginMessageProducer;

/**
 **/
@LocalBean
@Stateless
public class ExchangeService {

    final static Logger LOG = LoggerFactory.getLogger(ExchangeService.class);

    @EJB
    StartupBean startupBean;

    @EJB
    PluginMessageProducer producer;

    public void sendMovementReportToExchange(SetReportMovementType reportType) {
        try {
            String text = ExchangeModuleRequestMapper.createSetMovementReportRequest(reportType, "FLUX");
            String messageId = producer.sendModuleMessage(text, ModuleQueue.EXCHANGE);
            startupBean.getCachedMovement().put(messageId, reportType);
        } catch (ExchangeModelMarshallException e) {
            LOG.error("Couldn't map movement to setreportmovementtype");
        } catch (JMSException e) {
            LOG.error("couldn't send movement");
            startupBean.getCachedMovement().put(UUID.randomUUID().toString(), reportType);
        }
    }
}