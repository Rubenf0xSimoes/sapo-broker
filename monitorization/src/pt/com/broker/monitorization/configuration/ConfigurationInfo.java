package pt.com.broker.monitorization.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pt.com.broker.client.HostInfo;
import pt.com.broker.monitorization.configuration.Agents.Agent;

public class ConfigurationInfo
{
	private static final Logger log = LoggerFactory.getLogger(ConfigurationInfo.class);

	private static MonitorizationConfiguration configuration;

	static
	{
		JAXBContext jc;
		Unmarshaller u = null;
		try
		{
			jc = JAXBContext.newInstance("pt.com.broker.monitorization.configuration");
			u = jc.createUnmarshaller();
			String filename = "./conf/configuration.xml";
			File f = new File(filename);
			boolean b = f.exists();
			if (!b)
			{
				log.error("Configuration file (" + filename + ") was not found.");
			}
			configuration = (MonitorizationConfiguration) u.unmarshal(f);
		}
		catch (Throwable e)
		{
			configuration = null;
			log.error("Configuration initialization failed.", e);
		}
	}

	private static String globalConfigFile = null;
	// Agents through which the monitorization console connects to a agent cloud
	private static List<HostInfo> agents = new ArrayList<HostInfo>();
	// Agent in the cloud
	public static List<HostInfo> cloudAgents = new ArrayList<HostInfo>();

	public static void init()
	{
		getGlobalConfig();
		extractConnectionExceptions();
		extractCloudAgents();
	}

	public static String getGlobalConfigFile()
	{
		return globalConfigFile;
	}

	public static List<HostInfo> getCloudAgents()
	{
		return cloudAgents;
	}
	
	public static List<HostInfo>  getAgents()
	{
		return agents;
	}

	private static void getGlobalConfig()
	{
		if (configuration == null)
			return;
		globalConfigFile = configuration.getGlobalConfigFile().getLocation();
	}

	private static void extractCloudAgents()
	{
		if (configuration == null)
			return;
		List<HostInfo> allAgents = getAgentsFromGlobalConfFile();

		for (HostInfo agent : allAgents)
		{
			int port = getPortForAgent(agent.getHostname() + ":" + agent.getPort());
			cloudAgents.add(new HostInfo(agent.getHostname(), port));
		}
	}

	private static int getPortForAgent(String agentHostname)
	{
		int DEFAULT_PORT = 3323;

		if (configuration == null)
			return DEFAULT_PORT;
		ExceptionAgents tcpPortExceptions = configuration.getTcpPortExceptions();
		for (pt.com.broker.monitorization.configuration.ExceptionAgents.Agent agent : tcpPortExceptions.getAgent())
		{
			if (agent.getAgentName().equals(agentHostname))
			{
				System.out.println("####### Not using default tcp port! Agent: " + agentHostname + "port: " + agent.getTcpPort().intValue());
				return agent.getTcpPort().intValue();
			}
		}

		return DEFAULT_PORT;
	}

	private static void extractConnectionExceptions()
	{
		for (Agent agent : configuration.getAgents().getAgent())
		{
			agents.add(new HostInfo(agent.getHostname(), agent.getPort().intValue()));
		}
	}

	private static List<HostInfo> getAgentsFromGlobalConfFile()
	{
		if (globalConfigFile == null)
			return new ArrayList<HostInfo>();

		ArrayList<HostInfo> agents = new ArrayList<HostInfo>();

		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			File xmlFile = new File(globalConfigFile);

			// Create the builder and parse the file
			Document doc = factory.newDocumentBuilder().parse(xmlFile);

			XPath xpath = XPathFactory.newInstance().newXPath();

			NodeList list = (NodeList) xpath.evaluate("/global-config/domain/peer", doc, XPathConstants.NODESET);

			for (int i = 0; i != list.getLength(); ++i)
			{
				Node item = list.item(i);
				String localhost = null;
				int port = 0;

				for (int j = 0; j != item.getChildNodes().getLength(); ++j)
				{
					Node peerInfoBit = item.getChildNodes().item(j);
					if(peerInfoBit.getNodeType() != Node.ELEMENT_NODE)
						continue;
					
					String nodeName = peerInfoBit.getNodeName();

					NodeList childNodes = peerInfoBit.getChildNodes();
					for (int k = 0; k != childNodes.getLength(); ++k)
					{
						Node node = childNodes.item(k);
						
						String textContent = node.getTextContent();
						

						if (nodeName.equals("ip"))
						{
							localhost = textContent;
						}
						else if (nodeName.equals("port"))
						{
							port = Integer.parseInt(textContent);
						}
						else
						{
							log.error("Unexpected element: " + peerInfoBit.getNodeName());
						}

					}
				}
				agents.add(new HostInfo(localhost, port));

			}

		}
		catch (Throwable t)
		{
			log.error("Error while processing global config file", t);
		}

		return agents;
	}
}
