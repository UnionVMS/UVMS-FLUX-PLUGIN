/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.plugins.flux.message;

import static eu.europa.ec.fisheries.uvms.exchange.model.mapper.JAXBMarshaller.marshallJaxBObjectToString;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

import eu.europa.ec.fisheries.schema.exchange.module.v1.ExchangeModuleMethod;
import eu.europa.ec.fisheries.schema.exchange.module.v1.SetFLUXFAReportMessageRequest;
import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginType;
import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.plugins.flux.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.flux.exception.PluginException;
import eu.europa.ec.fisheries.uvms.plugins.flux.service.ExchangeService;
import un.unece.uncefact.data.standard.fluxfareportmessage._3.FLUXFAReportMessage;
import xeu.bridge_connector.v1.RequestType;

@Stateless
@WebService(serviceName = "FLUXFAReportMessageService", targetNamespace = "urn:xeu:bridge-connector:wsdl:v1", portName = "BridgeConnectorPortType", endpointInterface = "xeu.bridge_connector.wsdl.v1.BridgeConnectorPortType")
public class FLUXFAReportMessageReceiver extends AbstractFluxReceiver {

	private static Logger LOG = LoggerFactory.getLogger(FLUXFAReportMessageReceiver.class);
	
    @EJB
    private StartupBean startupBean;

    @EJB
    private ExchangeService exchange;

    @Override protected void sendToExchange(RequestType rt) throws JAXBException, PluginException {

        SetFLUXFAReportMessageRequest request = new SetFLUXFAReportMessageRequest();
        JAXBContext jc = JAXBContext.newInstance(FLUXFAReportMessage.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        FLUXFAReportMessage xmlMessage = (FLUXFAReportMessage) unmarshaller.unmarshal(rt.getAny());

        try {
            request.setRequest(marshallJaxBObjectToString(xmlMessage));
        } catch (ExchangeModelMarshallException e) {
        	LOG.error("Problem with marshallJaxBObjectToString:" + xmlMessage,e);
        }

        Map<QName, String> otherAttributes = rt.getOtherAttributes();
        String user = otherAttributes.get(new QName("USER"));
        String guid = otherAttributes.get(new QName("GUID"));
        String fr = otherAttributes.get(new QName("FR"));

        request.setMethod(ExchangeModuleMethod.SET_FLUX_FA_REPORT_MESSAGE);
        request.setUsername(user);

        request.setFluxDataFlow(rt.getDF());
        request.setDate(new Date());
        request.setMessageGuid(guid);
        request.setPluginType(PluginType.FLUX);
        request.setSenderOrReceiver(fr);
        request.setOnValue(rt.getON());

        try {
            exchange.sendActivityReportToExchange(marshallJaxBObjectToString(request));
        } catch (ExchangeModelMarshallException e) {
            LOG.error("Problem with sendToExchange:" + request,e);
        }
    }

    @Override protected StartupBean getStartupBean() {
        return startupBean;
    }
}

