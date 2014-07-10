package pt.com.gcs.net.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.codec.protobuf.ProtoBufBindingSerializer;
import pt.com.broker.types.NetMessage;

import java.util.List;

/**
 * Encoder implementation. Used to encode messages exchanged between agents.
 * 
 * The wire message format is as simple as could be:
 * 
 * <pre>
 * ----------- 
 *  | Length  | -&gt; integer in network order: message:length
 *  -----------
 *  | Payload | -&gt; message payload
 *  -----------
 * </pre>
 * 
 * This applies to both input and ouput messages.
 */
@ChannelHandler.Sharable
public class GcsEncoder extends MessageToMessageEncoder
{
	private static final Logger log = LoggerFactory.getLogger(GcsEncoder.class);

	private final ProtoBufBindingSerializer serializer = new ProtoBufBindingSerializer();



    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, List out) throws Exception {

        if (!(msg instanceof NetMessage))
        {
            String errorMessage = "Message to be encoded is from an unexpected type - " + msg.getClass().getName();
            log.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }



        ByteBuf bout = ctx.alloc().buffer();
        ByteBufOutputStream sout = new ByteBufOutputStream(bout);
        sout.writeInt(0);

        serializer.marshal((NetMessage) msg, sout);

        int len = bout.writerIndex() - 4;

        bout.setInt(0, len);

        out.add(bout);

    }
}
