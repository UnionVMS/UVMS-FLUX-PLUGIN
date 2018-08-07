/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.
 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.plugins.flux.message;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;

import eu.europa.ec.fisheries.schema.exchange.module.v1.ExchangeBaseRequest;
import eu.europa.ec.fisheries.schema.exchange.module.v1.ExchangeModuleMethod;
import eu.europa.ec.fisheries.schema.exchange.module.v1.SetFAQueryMessageRequest;
import eu.europa.ec.fisheries.schema.exchange.module.v1.SetFLUXFAReportMessageRequest;
import eu.europa.ec.fisheries.schema.exchange.module.v1.SetFLUXFAResponseMessageRequest;
import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginType;
import eu.europa.ec.fisheries.uvms.commons.message.impl.JAXBUtils;
import eu.europa.ec.fisheries.uvms.plugins.flux.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.flux.exception.PluginException;
import eu.europa.ec.fisheries.uvms.plugins.flux.service.ExchangeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.jboss.ws.api.annotation.WebContext;
import un.unece.uncefact.data.standard.fluxfaquerymessage._3.FLUXFAQueryMessage;
import un.unece.uncefact.data.standard.fluxfareportmessage._3.FLUXFAReportMessage;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._20.FAQuery;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._20.FLUXReportDocument;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._20.FLUXResponseDocument;
import un.unece.uncefact.data.standard.unqualifieddatatype._20.IDType;
import xeu.bridge_connector.v1.RequestType;
import xeu.bridge_connector.v1.VerbosityType;

@Stateless
@WebService(serviceName = "FLUXFAReportMessageService", targetNamespace = "urn:xeu:bridge-connector:wsdl:v1", portName = "BridgeConnectorPortType", endpointInterface = "xeu.bridge_connector.wsdl.v1.BridgeConnectorPortType")
@WebContext(contextRoot = "/unionvms/flux-service")
@Slf4j
public class FLUXFAReportMessageReceiverBean extends AbstractFluxReceiver {

    private static final String ISO_8859_1 = "ISO-8859-1";
    @EJB
    private StartupBean startupBean;

    @EJB
    private ExchangeService exchange;

    @Override
    protected void sendToExchange(RequestType rt) throws JAXBException, PluginException {

        String localName = rt.getAny().getLocalName();
        Map<QName, String> otherAttributes = rt.getOtherAttributes();
        String user = otherAttributes.get(new QName("USER"));
        String fr = otherAttributes.get(new QName("FR"));
        String on = rt.getON();
        String ad = rt.getAD();
        String df = rt.getDF();
        Integer to = rt.getTO();
        XMLGregorianCalendar todt = rt.getTODT();
        VerbosityType vb = rt.getVB();

        ExchangeBaseRequest exchangeBaseRequest;

        if ("FLUXFAQueryMessage".equals(localName)){
            exchangeBaseRequest = new SetFAQueryMessageRequest();
            exchangeBaseRequest.setMethod(ExchangeModuleMethod.SET_FA_QUERY_MESSAGE);

            JAXBContext jc = JAXBContext.newInstance(FLUXFAQueryMessage.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            FLUXFAQueryMessage xmlMessage = (FLUXFAQueryMessage) unmarshaller.unmarshal(rt.getAny());
            FAQuery faQuery = xmlMessage.getFAQuery();
            String guid = null;
            if (faQuery != null){
                IDType id = faQuery.getID();
                guid = id.getValue();
            }
            exchangeBaseRequest.setMessageGuid(guid);

            ((SetFAQueryMessageRequest) exchangeBaseRequest).setRequest(JAXBUtils.marshallJaxBObjectToString(xmlMessage, ISO_8859_1,true));

        }
        else if ("FLUXFAReportMessage".equals(localName)){
            exchangeBaseRequest = new SetFLUXFAReportMessageRequest();
            JAXBContext jc = JAXBContext.newInstance(FLUXFAReportMessage.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            FLUXFAReportMessage xmlMessage = (FLUXFAReportMessage) unmarshaller.unmarshal(rt.getAny());
            FLUXReportDocument fluxReportDocument = xmlMessage.getFLUXReportDocument();
            String guid = null;
            if (fluxReportDocument != null){
                List<IDType> fluxReportDocumentIDS = fluxReportDocument.getIDS();
                if (CollectionUtils.isNotEmpty(fluxReportDocumentIDS)){
                    guid = fluxReportDocumentIDS.get(0).getValue();
                }
            }
            exchangeBaseRequest.setMethod(ExchangeModuleMethod.SET_FLUX_FA_REPORT_MESSAGE);
            exchangeBaseRequest.setMessageGuid(guid);
            ((SetFLUXFAReportMessageRequest)exchangeBaseRequest).setRequest(JAXBUtils.marshallJaxBObjectToString(xmlMessage, ISO_8859_1,true));


        }
        else if ("FLUXResponseMessage".equals(localName)){
            exchangeBaseRequest = new SetFLUXFAResponseMessageRequest();
            exchangeBaseRequest.setMethod(ExchangeModuleMethod.RCV_FLUX_FA_RESPONSE_MESSAGE);
            JAXBContext jc = JAXBContext.newInstance(FLUXFAReportMessage.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            FLUXResponseDocument xmlMessage = (FLUXResponseDocument) unmarshaller.unmarshal(rt.getAny());
            ((SetFLUXFAResponseMessageRequest)exchangeBaseRequest).setRequest(JAXBUtils.marshallJaxBObjectToString(xmlMessage, ISO_8859_1,true));

        }
        else {
            exchangeBaseRequest = new SetFLUXFAReportMessageRequest();
            exchangeBaseRequest.setMethod(ExchangeModuleMethod.UNKNOWN);
        }

        exchangeBaseRequest.setUsername(user);
        exchangeBaseRequest.setFluxDataFlow(rt.getDF());
        exchangeBaseRequest.setDate(new Date());
        exchangeBaseRequest.setPluginType(PluginType.FLUX);
        exchangeBaseRequest.setSenderOrReceiver(fr);
        exchangeBaseRequest.setOnValue(rt.getON());
        Integer to1 = rt.getTO();
        if (to1 != null){
            exchangeBaseRequest.setTo(BigInteger.valueOf(to1));
        }

        XMLGregorianCalendar todt1 = rt.getTODT();
        if (todt1 != null){
            exchangeBaseRequest.setTodt(todt1.toString());
        }

        exchange.sendActivityReportToExchange(JAXBUtils.marshallJaxBObjectToString(exchangeBaseRequest, ISO_8859_1,true));

    }

    @Override protected StartupBean getStartupBean() {
        return startupBean;
    }

}