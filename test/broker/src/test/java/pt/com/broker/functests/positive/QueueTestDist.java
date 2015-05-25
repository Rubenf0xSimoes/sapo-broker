package pt.com.broker.functests.positive;

import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.types.NetProtocolType;

public class QueueTestDist extends QueueTest
{

	public QueueTestDist(NetProtocolType protocolType)
	{
		super(protocolType);

		setName("Queue test with distant consumer");
		try
		{
			BrokerClient bk = new BrokerClient(getAgent2Hostname(), getAgent2Port(), getEncodingProtocolType());
			bk.connect();

			setInfoConsumer(bk);
		}
		catch (Throwable t)
		{
			setFailure(t);
		}

	}

}
