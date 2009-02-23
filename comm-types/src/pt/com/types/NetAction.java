package pt.com.types;

public final class NetAction
{

	public enum ActionType
	{
		PUBLISH, POLL, ACCEPTED, ACKNOWLEDGE_MESSAGE, SUBSCRIBE, UNSUBSCRIBE, NOTIFICATION, FAULT, PING, PONG
	};

	public enum DestinationType
	{
		TOPIC, QUEUE, VIRTUAL_QUEUE
	};

	protected ActionType actionType;

	private NetPublish publishMessage;
	private NetPoll pollMessage;
	private NetAccepted acceptedMessage;
	private NetAcknowledgeMessage acknowledgeMessage;
	private NetSubscribe subscribeMessage;
	private NetUnsubscribe unsbuscribeMessage;
	private NetNotification notificationMessage;
	private NetFault faultMessage;
	private NetPing pingMessage;
	private NetPong pongMessage;

	public NetAction(ActionType actionType)
	{
		this.actionType = actionType;
	}

	public ActionType getActionType()
	{
		return actionType;
	}

	public void setPublishMessage(NetPublish publishMessage)
	{
		this.publishMessage = publishMessage;
	}

	public NetPublish getPublishMessage()
	{
		return publishMessage;
	}

	public void setPollMessage(NetPoll pollMessage)
	{
		this.pollMessage = pollMessage;
	}

	public NetPoll getPollMessage()
	{
		return pollMessage;
	}

	public void setAcceptedMessage(NetAccepted acceptedMessage)
	{
		this.acceptedMessage = acceptedMessage;
	}

	public NetAccepted getAcceptedMessage()
	{
		return acceptedMessage;
	}

	public void setAcknowledgeMessage(NetAcknowledgeMessage acknowledgeMessage)
	{
		this.acknowledgeMessage = acknowledgeMessage;
	}

	public NetAcknowledgeMessage getAcknowledgeMessage()
	{
		return acknowledgeMessage;
	}

	public void setSubscribeMessage(NetSubscribe subscribeMessage)
	{
		this.subscribeMessage = subscribeMessage;
	}

	public NetSubscribe getSubscribeMessage()
	{
		return subscribeMessage;
	}

	public void setUnsbuscribeMessage(NetUnsubscribe unsbuscribeMessage)
	{
		this.unsbuscribeMessage = unsbuscribeMessage;
	}

	public NetUnsubscribe getUnsbuscribeMessage()
	{
		return unsbuscribeMessage;
	}

	public void setNotificationMessage(NetNotification notificationMessage)
	{
		this.notificationMessage = notificationMessage;
	}

	public NetNotification getNotificationMessage()
	{
		return notificationMessage;
	}

	public void setFaultMessage(NetFault faultMessage)
	{
		this.faultMessage = faultMessage;
	}

	public NetFault getFaultMessage()
	{
		return faultMessage;
	}

	public void setPingMessage(NetPing pingMessage)
	{
		this.pingMessage = pingMessage;
	}

	public NetPing getPingMessage()
	{
		return pingMessage;
	}

	public void setPongMessage(NetPong pongMessage)
	{
		this.pongMessage = pongMessage;
	}

	public NetPong getPongMessage()
	{
		return pongMessage;
	}
}