package pt.com.broker.codec.thrift;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class ThriftCodec implements ProtocolCodecFactory
{

	public static final int HEADER_LENGTH = 4;

	private ThriftEncoder encoder;
	private ThriftDecoder decoder;

	public ThriftCodec()
	{
		encoder = new ThriftEncoder();
		decoder = new ThriftDecoder();
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
