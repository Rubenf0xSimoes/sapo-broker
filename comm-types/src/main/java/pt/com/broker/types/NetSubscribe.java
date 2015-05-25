package pt.com.broker.types;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

/**
 * Represents a Subscription message.
 * 
 */

public final class NetSubscribe implements NetSubscribeAction
{
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(NetSubscribe.class);
	private String actionId;
	private String destination;
	private NetAction.DestinationType destinationType;

	private Map<String, String> headers;

	public NetSubscribe(String destination, NetAction.DestinationType destinationType)
	{
		this.destination = destination;
		this.destinationType = destinationType;
	}

	public void setActionId(String actionId)
	{
		this.actionId = actionId;
	}

	public String getActionId()
	{
		return actionId;
	}

	public String getDestination()
	{
		return destination;
	}

	public NetAction.DestinationType getDestinationType()
	{
		return destinationType;
	}

	public void setHeaders(Map<String, String> headers)
	{
		this.headers = headers;
	}

	public Map<String, String> getHeaders()
	{
		return headers;
	}

	public void addHeader(String header, String value)
	{
		if (headers == null)
		{
			headers = new HashMap<String, String>();
		}
		headers.put(header, value);
	}
}
