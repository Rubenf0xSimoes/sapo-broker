package pt.com.broker.functests.positive;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.BrokerTest;
import pt.com.broker.functests.helpers.MultipleGenericPubSubTest;
import pt.com.broker.functests.helpers.MultipleNotificationsBrokerListener;

public class Multiple1NTopic extends MultipleGenericPubSubTest
{
	public Multiple1NTopic()
	{
		super("Topic - 1 producer N consumers");
	}

	@Override
	protected void addConsumers()
	{
		super.addConsumers();
		try
		{
			TestClientInfo tci = new TestClientInfo();

			tci.brokerClient = new BrokerClient(ConfigurationInfo.getParameter("agent1-host"), BrokerTest.getAgent1Port(), "tcp://mycompany.com/test", getEncodingProtocolType());
			tci.brokerListenter = new MultipleNotificationsBrokerListener(getDestinationType(), 1);
			tci.numberOfExecutions = getInfoProducers().size();

			this.addInfoConsumer(tci);
		}
		catch (Throwable t)
		{
			this.setFailure(t);
		}
	}
}
