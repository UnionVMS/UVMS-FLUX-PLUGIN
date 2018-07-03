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
import static eu.europa.ec.fisheries.uvms.plugins.flux.constants.ActivityType.UNKNOWN;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Date;
import java.util.List;
import java.util.Map;

import eu.europa.ec.fisheries.schema.exchange.module.v1.ExchangeModuleMethod;
import eu.europa.ec.fisheries.schema.exchange.module.v1.SetFLUXFAReportMessageRequest;
import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginType;
import eu.europa.ec.fisheries.uvms.commons.message.impl.JAXBUtils;
import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.plugins.flux.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.flux.constants.ActivityType;
import eu.europa.ec.fisheries.uvms.plugins.flux.exception.PluginException;
import eu.europa.ec.fisheries.uvms.plugins.flux.service.ExchangeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.jboss.ws.api.annotation.WebContext;
import un.unece.uncefact.data.standard.fluxfareportmessage._3.FLUXFAReportMessage;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._20.FLUXReportDocument;
import un.unece.uncefact.data.standard.unqualifieddatatype._20.IDType;
import xeu.bridge_connector.v1.RequestType;

@Stateless
@WebService(serviceName = "FLUXFAReportMessageService", targetNamespace = "urn:xeu:bridge-connector:wsdl:v1", portName = "BridgeConnectorPortType", endpointInterface = "xeu.bridge_connector.wsdl.v1.BridgeConnectorPortType")
@WebContext(contextRoot = "/unionvms/flux-service")
@Slf4j
public class FLUXFAReportMessageReceiverBean extends AbstractFluxReceiver {

    private static final String FLUXFAREPORT_MESSAGE_START_XSD_ELEMENT = "FLUXFAReportMessage";
    private static final String FLUXFAQUERY_MESSAGE_START_XSD_ELEMENT = "FLUXFAQueryMessage";
    private static final String FLUXRESPONSE_MESSAGE_START_XSD_ELEMENT = "FLUXResponseMessage";

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

            FLUXReportDocument fluxReportDocument = xmlMessage.getFLUXReportDocument();

            String guid = null;
            if (fluxReportDocument != null){
                List<IDType> fluxReportDocumentIDS = fluxReportDocument.getIDS();
                if (CollectionUtils.isNotEmpty(fluxReportDocumentIDS)){
                    guid = fluxReportDocumentIDS.get(0).getValue();
                }
            }
            String messageAsString = marshallJaxBObjectToString(xmlMessage);
            ActivityType activityType = extractActivityTypeFromMessage(messageAsString);

            Map<QName, String> otherAttributes = rt.getOtherAttributes();
            String user = otherAttributes.get(new QName("USER"));
            String fr = otherAttributes.get(new QName("FR"));

            switch (activityType){
                case FA_REPORT:
                    request.setMethod(ExchangeModuleMethod.SET_FLUX_FA_REPORT_MESSAGE);
                    break;
                case FA_QUERY:
                    request.setMethod(ExchangeModuleMethod.SET_FA_QUERY_MESSAGE);
                    break;
                case FLUX_RESPONSE:
                    request.setMethod(ExchangeModuleMethod.RCV_FLUX_FA_RESPONSE_MESSAGE);
                    break;
                    default:
                case UNKNOWN:
                    request.setMethod(ExchangeModuleMethod.UNKNOWN);
            }
            request.setUsername(user);

            request.setFluxDataFlow(rt.getDF());
            request.setDate(new Date());
            request.setMessageGuid(guid);
            request.setPluginType(PluginType.FLUX);
            request.setSenderOrReceiver(fr);
            request.setOnValue(rt.getON());

            request.setRequest(JAXBUtils.marshallJaxBObjectToString(xmlMessage,"ISO-8859-1",true));
            exchange.sendActivityReportToExchange(JAXBUtils.marshallJaxBObjectToString(request,"ISO-8859-1",true));

        } catch ( ExchangeModelMarshallException | XMLStreamException e) {
            e.printStackTrace();
        }
    }

    @Override protected StartupBean getStartupBean() {
        return startupBean;
    }

    ActivityType extractActivityTypeFromMessage(String document) throws XMLStreamException {
        Reader reader = new StringReader(document);
        XMLStreamReader xml = XMLInputFactory.newFactory().createXMLStreamReader(reader);
        ActivityType type = null;
        while (xml.hasNext()) {
            int nextNodeType = xml.next();
            if (nextNodeType == XMLStreamConstants.START_ELEMENT) {
                if (FLUXFAREPORT_MESSAGE_START_XSD_ELEMENT.equals(xml.getLocalName())) {
                    type = ActivityType.FA_REPORT;
                    break;
                } else if (FLUXFAQUERY_MESSAGE_START_XSD_ELEMENT.equals(xml.getLocalName())) {
                    type = ActivityType.FA_QUERY;
                    break;
                } else if (FLUXRESPONSE_MESSAGE_START_XSD_ELEMENT.equals(xml.getLocalName())) {
                    type = ActivityType.FLUX_RESPONSE;
                }
            }
        }
        xml.close();
        return type != null ? type : UNKNOWN;
    }
}