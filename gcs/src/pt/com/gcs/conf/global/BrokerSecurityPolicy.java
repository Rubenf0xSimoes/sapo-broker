//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.06.04 at 12:31:09 PM WEST 
//

package pt.com.gcs.conf.global;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for BrokerSecurityPolicy complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name=&quot;BrokerSecurityPolicy&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element name=&quot;policies&quot; type=&quot;{}Policies&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element name=&quot;agents&quot; type=&quot;{}Agents&quot; minOccurs=&quot;0&quot;/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BrokerSecurityPolicy", propOrder = { "policies", "agents" })
public class BrokerSecurityPolicy
{

	protected Policies policies;
	protected Agents agents;

	/**
	 * Gets the value of the policies property.
	 * 
	 * @return possible object is {@link Policies }
	 * 
	 */
	public Policies getPolicies()
	{
		return policies;
	}

	/**
	 * Sets the value of the policies property.
	 * 
	 * @param value
	 *            allowed object is {@link Policies }
	 * 
	 */
	public void setPolicies(Policies value)
	{
		this.policies = value;
	}

	/**
	 * Gets the value of the agents property.
	 * 
	 * @return possible object is {@link Agents }
	 * 
	 */
	public Agents getAgents()
	{
		return agents;
	}

	/**
	 * Sets the value of the agents property.
	 * 
	 * @param value
	 *            allowed object is {@link Agents }
	 * 
	 */
	public void setAgents(Agents value)
	{
		this.agents = value;
	}

}
