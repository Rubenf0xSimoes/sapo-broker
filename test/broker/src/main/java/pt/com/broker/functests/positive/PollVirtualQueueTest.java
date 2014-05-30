package pt.com.broker.functests.positive;

import java.util.Arrays;
import java.util.concurrent.Future;

import org.caudexorigo.text.RandomStringUtils;

import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.client.nio.HostInfo;
import pt.com.broker.client.nio.events.NotificationListenerAdapter;
import pt.com.broker.functests.Action;
import pt.com.broker.functests.Consequence;
import pt.com.broker.functests.Step;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.BrokerTest;

import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetSubscribe;

public class PollVirtualQueueTest extends BrokerTest
{
	private String baseName = RandomStringUtils.randomAlphanumeric(10);
	private String topicName;
	private String queueName;

	public PollVirtualQueueTest()
	{
		super("Virtual Poll test");
		baseName = RandomStringUtils.randomAlphanumeric(10);
		topicName = String.format("/topic/%s", baseName);
		queueName = "app@" + topicName;
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
					BrokerClient bk = new BrokerClient(ConfigurationInfo.getParameter("agent1-host"), BrokerTest.getAgent1Port(), getEncodingProtocolType());
                    bk.connect();

					NetSubscribe subscribe = new NetSubscribe(queueName, DestinationType.VIRTUAL_QUEUE);
					Future f = bk.subscribe(subscribe, new NotificationListenerAdapter() {

                        @Override
                        public boolean onMessage(NetNotification message, HostInfo host) {
                            return false;
                        }


                    });

                    f.get();

					f = bk.unsubscribe(DestinationType.VIRTUAL_QUEUE, queueName);

					f.get();


                    NetBrokerMessage brokerMessage = new NetBrokerMessage(getData());

					bk.publish(brokerMessage, topicName, DestinationType.TOPIC);

                    Thread.sleep(3000);

				    bk.close().get();

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
					BrokerClient bk = new BrokerClient(ConfigurationInfo.getParameter("agent1-host") , BrokerTest.getAgent1Port() , getEncodingProtocolType());

                    bk.connect();

                    NetNotification msg = bk.poll(queueName);


					bk.acknowledge(msg).get();





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

                    bk.close().get();

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
