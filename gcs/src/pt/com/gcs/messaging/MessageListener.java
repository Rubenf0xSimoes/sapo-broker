package pt.com.gcs.messaging;

import pt.com.types.NetAction.DestinationType;

public interface MessageListener
{
	public boolean onMessage(InternalMessage message);

	public String getDestinationName();

	public DestinationType getDestinationType();

}
