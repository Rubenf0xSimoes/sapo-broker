package pt.com.broker.codec.xml;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * SOAP codec.
 *
 */
public class SoapCodecV2 implements ProtocolCodecFactory
{
	public static final int HEADER_LENGTH = 4;

	private SoapEncoderV2 encoder;

	private SoapDecoderV2 decoder;

	public SoapCodecV2(int maxMessageSize)
	{
		encoder = new SoapEncoderV2();
		decoder = new SoapDecoderV2(maxMessageSize);
	}

	@Override
	public ProtocolDecoder getDecoder(IoSession arg0) throws Exception
	{
		return decoder;
	}

	@Override
	public ProtocolEncoder getEncoder(IoSession arg0) throws Exception
	{
		return encoder;
	}
}
