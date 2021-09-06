package eu.europa.ec.fisheries.uvms.plugins.flux.movement.ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

@WebService(targetNamespace = "urn:xeu:bridge-connector:wsdl:v1", name = "BridgeConnectorPortType")
@XmlSeeAlso({xeu.bridge_connector.v1.ObjectFactory.class})
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface BridgeConnectorPortType {

    @WebResult(name = "Connector2BridgeResponse", targetNamespace = "urn:xeu:bridge-connector:v1", partName = "output")
    @WebMethod(action = "urn:xeu:bridge-connector:wsdl:v1:post")
    public xeu.bridge_connector.v1.ResponseType post(
            @WebParam(partName = "parameter", name = "Connector2BridgeRequest", targetNamespace = "urn:xeu:bridge-connector:v1")
                    RequestType parameter
    );
}