package pt.com.broker.types;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a Messaging level message. 
 *
 */

public class NetMessage
{

	private NetAction action;
	private Map<String, String> headers;

	public NetMessage(NetAction action)
	{
		this(action, null);
	}

	public NetMessage(NetAction action, Map<String, String> headers)
	{
		this.action = action;
		if (headers != null)
			this.headers = headers;
		else
			this.headers = new HashMap<String, String>();
	}

	public Map<String, String> getHeaders()
	{
		return headers;
	}

	public NetAction getAction()
	{
		return action;
	}
}
