package pt.com.broker.functests.positive;

import org.caudexorigo.concurrent.Sleep;




import pt.com.broker.client.nio.AcceptRequest;
import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.client.nio.events.BrokerListenerAdapter;
import pt.com.broker.client.nio.events.MessageAcceptedListener;
import pt.com.broker.functests.Action;
import pt.com.broker.functests.Consequence;
import pt.com.broker.functests.Epilogue;
import pt.com.broker.functests.Prerequisite;
import pt.com.broker.functests.Step;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.BrokerTest;
import pt.com.broker.functests.helpers.GenericBrokerListener;
import pt.com.broker.functests.helpers.SetValueFuture;
import pt.com.broker.types.*;
import pt.com.broker.types.NetAction.DestinationType;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class TopicPubSubWithActionId extends BrokerTest
{
	private String destinationName = "/topic/foo";
	private String subscriptionName = "/topic/foo";

	private DestinationType destinationType = DestinationType.TOPIC;

	private BrokerListenerAdapter brokerListener;

	private BrokerClient infoConsumer;
	private BrokerClient infoProducer;

	private boolean constructionFailed = false;
	private Throwable reasonForFailure;

	private SetValueFuture<Boolean> future = new SetValueFuture<Boolean>();

	public TopicPubSubWithActionId()
	{
		super("GenericPubSubTest");
		try
		{
			infoConsumer = new BrokerClient(ConfigurationInfo.getParameter("agent1-host"), BrokerTest.getAgent1Port() , getEncodingProtocolType());

            infoConsumer.connect();

			infoProducer = new BrokerClient("192.168.100.1", BrokerTest.getAgent1Port() , getEncodingProtocolType());

            infoProducer.connect();
		}
		catch (Throwable t)
		{
            t.printStackTrace();
			constructionFailed = true;
			reasonForFailure = t;
		}
	}

	@Override
	public void build() throws Throwable
	{
		if (constructionFailed)
			throw reasonForFailure;

		brokerListener = new BrokerListenerAdapter() {
            @Override
            public boolean onMessage(NetMessage message) {
                return true;
            }
        };

		addPrerequisites();

		addAction();

		addConsequences();

		addEpilogues();
	}

	public void setFailure(Throwable throwable)
	{
		constructionFailed = true;
		reasonForFailure = throwable;

	}

	protected void addPrerequisites()
	{
		this.addPrerequisite(new Prerequisite("Subscription")
		{
			public Step run() throws Exception
			{
				try
				{
					AcceptRequest accReq = new AcceptRequest("123456789", new MessageAcceptedListener()
					{

                        @Override
                        public boolean onMessage(NetMessage message) {

                            future.set(Boolean.TRUE);
                            return true;
                        }

                        @Override
                        public void onFault(NetMessage message) {
                            future.set(Boolean.FALSE);
                        }

                        @Override
                        public void onTimeout(String actionID) {
                            future.set(Boolean.FALSE);
                        }


					}, 4000);

					NetSubscribe subscribe = new NetSubscribe(subscriptionName, destinationType);


					Future f = infoConsumer.subscribe(subscribe, brokerListener, accReq);


                    f.get();


                    Thread.sleep(4000);
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

	protected void addAction()
	{
		this.setAction(new Action("Publish", "producer")
		{
			public Step run() throws Exception
			{

				try
				{

					NetBrokerMessage brokerMessage = new NetBrokerMessage(getData());


                    Future f = infoProducer.publishMessage(brokerMessage, destinationName, destinationType);

                    f.get();


                    Thread.sleep(2000);


					infoProducer.close();

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

	protected void addConsequences()
	{

		this.addConsequences(new Consequence("Action id receiver", "producer")
		{

			@Override
			public Step run() throws Exception
			{
				Boolean result = future.get();

				setSucess(result.booleanValue());
				setDone(true);

				return this;
			}

		});
	}

	protected void addEpilogues()
	{
		this.addEpilogue(new Epilogue("Epilogue")
		{
			public Step run() throws Exception
			{

				try
				{
					//infoConsumer.unsubscribe(NetAction.DestinationType.TOPIC, subscriptionName);

					Sleep.time(1000);
					infoConsumer.close();

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

    @Override
    protected void end() {

        try {

            infoConsumer.close().get();
            infoProducer.close().get();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


    }
}
