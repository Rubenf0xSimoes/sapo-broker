package pt.com.broker.functests.helpers;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.nio.events.NotificationListenerAdapter;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetNotification;

public class MultipleNotificationsBrokerListener extends NotificationListenerAdapter
{
	private static final Logger log = LoggerFactory.getLogger(MultipleNotificationsBrokerListener.class);

	private NetAction.DestinationType destinationType;
	private List<NetNotification> list;
	private int expectedNotifications;
	private SetValueFuture<List<NetNotification>> value = new SetValueFuture<List<NetNotification>>();

	public MultipleNotificationsBrokerListener(NetAction.DestinationType destinationType, int expectedNotifications)
	{
		this.destinationType = destinationType;
		this.expectedNotifications = expectedNotifications;
		this.list = new ArrayList<>(expectedNotifications);
		// this.list = new CopyOnWriteArrayList<NetNotification>();
	}

	@Override
	public boolean onMessage(NetNotification message, HostInfo host)
	{

		log.info("OnMessage :" + message.getMessage().getMessageId());

		synchronized (list)
		{
			list.add(message);
			if (list.size() == expectedNotifications)
			{
				value.set(list);
			}
		}

		return true;
	}

	public SetValueFuture<List<NetNotification>> getFuture()
	{
		return value;
	}

}
