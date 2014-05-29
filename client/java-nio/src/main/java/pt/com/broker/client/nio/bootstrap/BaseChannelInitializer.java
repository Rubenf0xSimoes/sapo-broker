package pt.com.broker.client.nio.bootstrap;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.ssl.SslHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.client.nio.codecs.BindingSerializerFactory;
import pt.com.broker.client.nio.codecs.BrokerMessageDecoder;
import pt.com.broker.client.nio.codecs.BrokerMessageEncoder;
import pt.com.broker.client.nio.consumer.ConsumerManager;
import pt.com.broker.client.nio.consumer.PongConsumerManager;
import pt.com.broker.client.nio.handlers.PongMessageHandler;
import pt.com.broker.client.nio.handlers.ReceiveMessageHandler;
import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.NetProtocolType;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

/**
 * Created by luissantos on 05-05-2014.
 */
public abstract class BaseChannelInitializer extends io.netty.channel.ChannelInitializer<Channel> {


    protected static final Logger log = LoggerFactory.getLogger(BaseChannelInitializer.class);

    protected final BindingSerializer serializer;

    private boolean oldFraming = false;

    public BaseChannelInitializer(BindingSerializer serializer) {
        this.serializer= serializer;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {

        ChannelPipeline pipeline = ch.pipeline();

        if(isOldFraming()){

            /* add Message <> byte encode decoder */
            pipeline.addLast("broker_message_decoder",new pt.com.broker.client.nio.codecs.oldframing.BrokerMessageDecoder(serializer));
            pipeline.addLast("broker_message_encoder",new pt.com.broker.client.nio.codecs.oldframing.BrokerMessageEncoder(serializer));


        }else{

            /* add Message <> byte encode decoder */
            pipeline.addLast("broker_message_decoder",new BrokerMessageDecoder(serializer));
            pipeline.addLast("broker_message_encoder",new BrokerMessageEncoder(serializer));
        }


        /*ch.pipeline().addLast("byte_message_encoder",new MessageToByteEncoder<Byte[]>(){

            @Override
            protected void encode(ChannelHandlerContext ctx, Byte[] msg, ByteBuf out) throws Exception {

                byte[] data = new byte[msg.length];

                int pos = 0;
                for(Byte bye : msg){
                    data[pos++] = bye.byteValue();
                }

                out.writeBytes(data);
            }
        });*/




    }


    protected boolean isOldFraming(){
        return  oldFraming;
    }

    public void setOldFraming(boolean oldFraming) {
        this.oldFraming = oldFraming;
    }
}
