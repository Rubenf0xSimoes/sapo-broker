package pt.com.broker.client;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.com.broker.client.messaging.BrokerListener;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetSubscribe;

/**
 * BrokerAsyncConsumer represents a Asynchronous client.
 * 
 */

public class BrokerAsyncConsumer
{
	private final NetSubscribe subscription;

	private final BrokerListener _wrappedListener;

	private final Pattern _subscriptionName;

	public BrokerAsyncConsumer(NetSubscribe subscrition, BrokerListener listener)
	{
		super();
		_wrappedListener = listener;
		this.subscription = subscrition;
		_subscriptionName = Pattern.compile(subscrition.getDestination());
	}

	public NetSubscribe getSubscription()
	{
		return subscription;
	}

	public BrokerListener getListener()
	{
		return _wrappedListener;
	}

	public boolean deliver(NetNotification msg)
	{
		String toMatch = null;
		if (msg.getDestinationType() != NetAction.DestinationType.TOPIC)
		{
			// VIRTUAL QUEUES destination is the same as the subscription but
			// destination is
			toMatch = msg.getSubscription();
		}
		else
		{
			toMatch = msg.getDestination();
		}

		Matcher m = _subscriptionName.matcher(toMatch);
		if (m.matches())
		{
			_wrappedListener.onMessage(msg);
			return true;
		}
		else
		{
			return false;
		}
	}

}
