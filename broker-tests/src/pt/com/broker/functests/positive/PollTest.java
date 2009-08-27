package pt.com.broker.functests.positive;

import java.util.Arrays;

import org.caudexorigo.text.RandomStringUtils;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.functests.Action;
import pt.com.broker.functests.Consequence;
import pt.com.broker.functests.Step;
import pt.com.broker.functests.helpers.BrokerTest;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetNotification;

public class PollTest extends BrokerTest
{
	private String baseName = RandomStringUtils.randomAlphanumeric(10);
	private String queueName = String.format("/poll/%s", baseName);

	public PollTest()
	{
		super("Poll test");
	}

	@Override
	protected void build() throws Throwable
	{
		setAction(new Action("Poll Test", "Producer")
		{

			@Override
			public Step run() throws Exception
			{
				try
				{
					BrokerClient bk = new BrokerClient("127.0.0.1", 3323, "tcp://mycompany.com/mypublisher", getEncodingProtocolType());
					NetBrokerMessage brokerMessage = new NetBrokerMessage(getData());

					bk.enqueueMessage(brokerMessage, queueName);

					bk.close();

					setDone(true);
					setSucess(true);
				}
				catch (Throwable t)
				{
					throw new Exception(t);
				}
				return this;
			}

		});

		addConsequences(new Consequence("Poll Test", "Consumer")
		{
			@Override
			public Step run() throws Exception
			{
				try
				{
					BrokerClient bk = new BrokerClient("127.0.0.1", 3323, "tcp://mycompany.com/mypublisher", getEncodingProtocolType());

					NetNotification msg = bk.poll(queueName);

					bk.acknowledge(msg);

					bk.close();

					if (msg.getMessage() == null)
					{
						setReasonForFailure("Broker Message is null");
						return this;
					}
					if (msg.getMessage().getPayload() == null)
					{
						setReasonForFailure("Message payload is null");
						return this;
					}

					if (!Arrays.equals(msg.getMessage().getPayload(), getData()))
					{
						setReasonForFailure("Message payload is different from expected");
						return this;
					}

					setDone(true);
					setSucess(true);
				}
				catch (Throwable t)
				{
					throw new Exception(t);
				}
				return this;
			}

		});

	}

}
