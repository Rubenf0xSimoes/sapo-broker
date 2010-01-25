package pt.com.gcs.messaging;

import pt.com.broker.types.NetAction.DestinationType;

/**
 * Classes implement MessageListener in order to be notified of new topic publications.
 * 
 */

public interface MessageListener
{
	/**
	 * 
	 * @param message Message to be delivered.
	 * @return Negative value if the message wasn't delivered or a positive value indicating for how long the message should be reserved (if applicable).
	 */
	public long onMessage(InternalMessage message);

	public String getDestinationName();

	public DestinationType getSourceDestinationType();
	
	public DestinationType getTargetDestinationType();
	
	public boolean ready();
	
	public boolean isActive();
}
