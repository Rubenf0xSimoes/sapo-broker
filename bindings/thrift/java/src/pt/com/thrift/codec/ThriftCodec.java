package pt.com.thrift.codec;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * The network protocol is as simple as could be:
 * 
 * <pre>
 *  ----------- 
 *  | Length  | -&gt; integer in network order
 *  -----------
 *  | Payload | -&gt; Binary message
 *  -----------
 * </pre>
 * 
 * This applies to both input and ouput messages.
 */
public class ThriftCodec implements ProtocolCodecFactory
{

	public static final int HEADER_LENGTH = 4;

	// TODO: Create a constructor that specifies this value. The original value was defined by: MQ.MAX_MESSAGE_SIZE
	public static final int MAX_MESSAGE_SIZE = 4 * 1024;

	private ThriftEncoder encoder;
	private ThriftDecoder decoder;

	public ThriftCodec()
	{
		encoder = new ThriftEncoder();
		decoder = new ThriftDecoder(MAX_MESSAGE_SIZE);
	}

	@Override
	public ProtocolDecoder getDecoder(IoSession session) throws Exception
	{
		return decoder;
	}

	@Override
	public ProtocolEncoder getEncoder(IoSession session) throws Exception
	{
		return encoder;
	}

}
