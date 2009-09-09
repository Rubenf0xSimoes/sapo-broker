package pt.com.broker.functests.positive;

import pt.com.broker.client.SslBrokerClient;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.GenericPubSubTest;

public class SslTopicNameSpeficied extends GenericPubSubTest
{
	public SslTopicNameSpeficied()
	{
		this("PubSub - SSL Topic name specified");
	}

	public SslTopicNameSpeficied(String testName)
	{
		super(testName);
		
		String keyStoreLocation = ConfigurationInfo.getParameter("keystoreLocation");
		String keystorePassword = ConfigurationInfo.getParameter("keystorePassword");
		
		SslBrokerClient bk = null;
		try
		{
			bk = new SslBrokerClient(ConfigurationInfo.getParameter("agent1-host"), 
					Integer.parseInt(ConfigurationInfo.getParameter("agent1-ssl-port")), "tcp://mycompany.com/test", getEncodingProtocolType(), keyStoreLocation, keystorePassword.toCharArray());
		}
		catch (Throwable e)
		{
			super.setFailure(e);
		}
		setInfoConsumer(bk);
	}

}