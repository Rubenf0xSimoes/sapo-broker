package pt.com.gcs.messaging;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.caudexorigo.cryto.MD5;
import org.caudexorigo.text.StringUtils;

import pt.com.broker.types.NetBrokerMessage;
import pt.com.gcs.conf.GcsInfo;

/**
 * InternalMessage is the internal representation of a message. It contains a NetBrokerMessage and other fields related with the original message. <br/>
 * It's used for storage and passing between agents.
 * 
 */

public class InternalMessage implements Externalizable
{

	private static final AtomicLong SEQ = new AtomicLong(0L);
	private static final long serialVersionUID = -3656321513130930115L;
	public static final int DEFAULT_PRIORITY = 4;
	private static final long DEFAULT_EXPIRY;// = 1000L * 3600L * 24L * 7L; // 7days
	private static final String SEPARATOR = "<#>";

	private static final String BASE_MESSAGE_ID;

	private String id;
	private NetBrokerMessage content;
	private String destination;
	private String publishDestination;
	private String correlationId;
	private int priority = DEFAULT_PRIORITY;
	private String sourceApp = "Undefined Source";
	private long timestamp = System.currentTimeMillis();
	private long expiration = timestamp + DEFAULT_EXPIRY;
	private pt.com.gcs.messaging.MessageType type = pt.com.gcs.messaging.MessageType.UNDEF;
	private boolean isFromRemotePeer = false;

	static
	{
		BASE_MESSAGE_ID = MD5.getHashString(UUID.randomUUID().toString());
		DEFAULT_EXPIRY = GcsInfo.getMessageStorageTime();
	}

	protected static String getBaseMessageId()
	{
		return BASE_MESSAGE_ID + "#";
	}

	private void checkArg(String value)
	{
		if (StringUtils.isBlank(value))
		{
			throw new IllegalArgumentException("Invalid argument. Message initializers must not empty");
		}
	}

	private void checkArg(Object value)
	{
		if (value == null)
		{
			throw new IllegalArgumentException("Invalid argument. Message initializers must not be null");
		}
	}

	public InternalMessage()
	{
		id = BASE_MESSAGE_ID + "#" + SEQ.incrementAndGet();
	}

	public InternalMessage(String destination, NetBrokerMessage content)
	{
		checkArg(destination);
		checkArg(content);
		this.content = content;
		this.destination = destination;
		id = BASE_MESSAGE_ID + "#" + SEQ.incrementAndGet();
	}

	public InternalMessage(String id, String destination, NetBrokerMessage content)
	{
		checkArg(destination);
		checkArg(content);
		checkArg(id);
		this.content = content;
		this.destination = destination;
		this.id = id;
	}

	public String getDestination()
	{
		return destination;
	}

	public void setDestination(String destination)
	{
		this.destination = destination;
	}

	public String getMessageId()
	{
		return id;
	}

	public void setMessageId(String id)
	{
		this.id = id;
	}

	public NetBrokerMessage getContent()
	{
		return content;
	}

	public void setContent(NetBrokerMessage content)
	{
		this.content = content;
	}

	public String getCorrelationId()
	{
		return correlationId;
	}

	public void setCorrelationId(String cid)
	{
		if (StringUtils.isNotBlank(cid))
		{
			correlationId = cid;
		}
	}

	public int getPriority()
	{
		return priority;
	}

	public void setPriority(int priority)
	{
		this.priority = priority;
	}

	public String getSourceApp()
	{
		return sourceApp;
	}

	public void setSourceApp(String sourceApp)
	{
		this.sourceApp = sourceApp;
	}

	public long getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp(long timestamp)
	{
		this.timestamp = timestamp;
	}

	public void setExpiration(long expiration)
	{
		this.expiration = expiration;
	}

	public long getExpiration()
	{
		return expiration;
	}

	public void setType(MessageType type)
	{
		this.type = type;
	}

	public pt.com.gcs.messaging.MessageType getType()
	{
		return type;
	}

	public void setFromRemotePeer(boolean isFromRemotePeer)
	{
		this.isFromRemotePeer = isFromRemotePeer;
	}

	public boolean isFromRemotePeer()
	{
		return isFromRemotePeer;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{

		correlationId = in.readUTF();
		if (correlationId.equals(""))
			correlationId = null;

		destination = in.readUTF();
		if (destination.equals(""))
			destination = null;

		publishDestination = in.readUTF();
		if (publishDestination.equals(""))
			publishDestination = null;

		id = in.readUTF();
		if (id.equals(""))
			id = null;

		priority = in.readInt();
		sourceApp = in.readUTF();
		if (sourceApp.equals(""))
			sourceApp = null;
		timestamp = in.readLong();
		expiration = in.readLong();
		type = MessageType.lookup(in.readInt());

		content = NetBrokerMessage.read(in);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{

		out.writeUTF((correlationId != null) ? correlationId : "");
		out.writeUTF((destination != null) ? destination : "");
		out.writeUTF((publishDestination != null) ? publishDestination : "");
		out.writeUTF((id != null) ? id : "");
		out.writeInt(priority);
		out.writeUTF((sourceApp != null) ? sourceApp : "");
		out.writeLong(timestamp);
		out.writeLong(expiration);
		out.writeInt(getType().getValue());

		content.write(out);

		out.flush();
	}

	@Override
	public String toString()
	{
		StringBuilder buf = new StringBuilder(100);
		buf.append(getContent());
		buf.append(SEPARATOR);
		buf.append(getCorrelationId());
		buf.append(SEPARATOR);
		buf.append(getDestination());
		buf.append(SEPARATOR);
		buf.append(getMessageId());
		buf.append(SEPARATOR);
		buf.append(getPriority());
		buf.append(SEPARATOR);
		buf.append(getSourceApp());
		buf.append(SEPARATOR);
		buf.append(getTimestamp());
		buf.append(SEPARATOR);
		buf.append(getExpiration());
		buf.append(SEPARATOR);
		buf.append(getType().getValue());

		return buf.toString();
	}

	public void setPublishDestination(String publishDestination)
	{
		this.publishDestination = publishDestination;
	}

	public String getPublishDestination()
	{
		return publishDestination;
	}

}