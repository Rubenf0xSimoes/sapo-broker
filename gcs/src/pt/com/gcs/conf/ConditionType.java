//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.03.02 at 12:09:37 PM WET 
//


package pt.com.gcs.conf;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ConditionType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ConditionType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="ROLE"/>
 *     &lt;enumeration value="CHANNELTYPE"/>
 *     &lt;enumeration value="ADDRESS"/>
 *     &lt;enumeration value="AND"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ConditionType")
@XmlEnum
public enum ConditionType {

    ROLE,
    CHANNELTYPE,
    ADDRESS,
    AND;

    public String value() {
        return name();
    }

    public static ConditionType fromValue(String v) {
        return valueOf(v);
    }

}
