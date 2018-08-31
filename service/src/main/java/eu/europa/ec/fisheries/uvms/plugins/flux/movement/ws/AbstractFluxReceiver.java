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
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.plugins.flux.movement.ws;

import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.exception.PluginException;
import eu.europa.ec.fisheries.uvms.plugins.flux.movement.service.StartupBean;
import lombok.extern.slf4j.Slf4j;
import xeu.bridge_connector.v1.RequestType;
import xeu.bridge_connector.v1.ResponseType;
import xeu.bridge_connector.wsdl.v1.BridgeConnectorPortType;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

@Slf4j
public abstract class AbstractFluxReceiver implements BridgeConnectorPortType {

    @Override
    public ResponseType post(RequestType rt) {
        ResponseType type = new ResponseType();
        if (!getStartupBean().isIsEnabled()) {
            type.setStatus("NOK");
            return type;
        }
        try {
            log.debug("Got activity request from FLUX in FLUX plugin");
            sendToExchange(rt);
            type.setStatus("OK");
            return type;
        } catch (Exception e) {
            log.error("[ Error when receiving data from FLUX. ]", e);
            type.setStatus("NOK");
            return type;
        }
    }

    protected abstract void sendToExchange(RequestType rt) throws JAXBException, PluginException, ExchangeModelMarshallException, TransformerException;

    protected abstract StartupBean getStartupBean();

}
