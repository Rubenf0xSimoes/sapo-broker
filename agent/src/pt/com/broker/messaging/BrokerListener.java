package pt.com.broker.messaging;

import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.session.IoSession;

import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.gcs.messaging.InternalMessage;
import pt.com.gcs.messaging.MessageListener;

/**
 * BrokerListener is a base class for types representing message consumers.
 * 
 */

public abstract class BrokerListener implements MessageListener
{
	protected static NetMessage buildNotification(InternalMessage msg, DestinationType dtype)
	{
		return buildNotification(msg, null, dtype);
	}

	protected static NetMessage buildNotification(InternalMessage msg, String subscriptionName, DestinationType dtype)
	{
		NetNotification notification = new NetNotification(msg.getPublishDestination(), dtype, msg.getContent(), subscriptionName);

		NetAction action = new NetAction(NetAction.ActionType.NOTIFICATION);
		action.setNotificationMessage(notification);

		notification.getMessage().setMessageId(msg.getMessageId());

		Map<String, String> params = new HashMap<String, String>();
		params.put("FROM", msg.getSourceApp());
		params.put("ACTION", "http://services.sapo.pt/broker/notification/" + msg.getMessageId());

		NetMessage message = new NetMessage(action, params);

		return message;
	}

	public abstract int addConsumer(IoSession iosession);

	public abstract int removeSessionConsumer(IoSession iosession);
}
