package pt.com.broker.functests.positive;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.MultipleGenericVirtualQueuePubSubTest;
import pt.com.broker.functests.helpers.MultipleNotificationsBrokerListener;

public class VirtualQueueTopicNameWildcardRemote extends MultipleGenericVirtualQueuePubSubTest
{
	public VirtualQueueTopicNameWildcardRemote()
	{
		super("VirtualQueue Remote- Topic name wildcard");
		setSubscriptionName(String.format("xpto@/%s/.*", getBaseName()));
	}

	protected void addConsumers()
	{
		try
		{
			TestClientInfo tci = new TestClientInfo();

			tci.brokerClient = new BrokerClient(ConfigurationInfo.getParameter("agent2-host"), 
					Integer.parseInt(ConfigurationInfo.getParameter("agent2-port")), "tcp://mycompany.com/test");
			tci.brokerListenter = new MultipleNotificationsBrokerListener(getConsumerDestinationType(), getConsumerNotifications());
			tci.numberOfExecutions = getConsumerNotifications();

			this.addInfoConsumer(tci);
		}
		catch (Throwable t)
		{
			setFailure(t);
		}

	}
}
