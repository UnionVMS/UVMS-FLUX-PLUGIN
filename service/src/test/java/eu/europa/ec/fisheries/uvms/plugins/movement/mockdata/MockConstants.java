/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europa.ec.fisheries.uvms.plugins.movement.mockdata;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 *
 * @author jojoha
 */
public class MockConstants {

    public static final String GUID = UUID.randomUUID().toString();
    public static final String GUID_ID = UUID.randomUUID().toString();
    public static final String CONNECT_ID = UUID.randomUUID().toString();

    public static final String ACTIVITY_CALLBACK = "TestCallback";
    public static final String AD = "GER";
    public static final String PURPOSE_CODE = "9";
    public static final String RECIPIENT = "SWE";

    public static final String ASSET_IRCS = "IRCS";
    public static final String ASSET_CFR = "CFR";
    public static final String ASSET_EXT_MARKING = "EXT_MARKING";
    public static final String ASSET_NAME = "ASSET_NAME";
    public static final String ASSET_FLAG_STATE = "SWE";
    public static final String FLUX_OWNER = "GER";
    public static final String POSITION_TYPE = "POS";

    public static final Double CALC_COURSE = 1.0;
    public static final Double CALC_SPEED = 2.0;
    public static final Double MEASURED_SPEED = 3.0;
    public static final Double MEASURED_COURSE = 4.0;
    public static final Double REPORTED_SPEED = 5.0;
    public static final Double REPORTED_COURSE = 6.0;
    public static final Double ALTITUDE = 7.0;
    public static final Double LATITUDE = 8.0;
    public static final Double LONGITUDE = 9.0;

    public static final Date NOW_DATE = new Date();
    public static final XMLGregorianCalendar NOW_DATE_GREGORIAN = getXmlGregorianTime(NOW_DATE);

    public static final String FLUX_DATA_FLOW = "http://dataflow";
    public static final String UUID_REGEX = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
    public static final String REGISTER_CLASSNAME = "ec.eu.uvms.classname";
    public static final String OWNER_FLUX_PARTY = "SWE";
    

    private static XMLGregorianCalendar getXmlGregorianTime(Date date) {
        try {
            GregorianCalendar c = new GregorianCalendar();
            c.setTime(date);
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        } catch (DatatypeConfigurationException ex) {
            Logger.getLogger(MovementTypeMock.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
