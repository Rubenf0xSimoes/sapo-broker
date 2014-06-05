package pt.com.broker.client.nio.handlers;

import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.client.nio.consumer.ConsumerManager;
import pt.com.broker.client.nio.consumer.PongConsumerManager;
import pt.com.broker.client.nio.types.ActionIdDecorator;
import pt.com.broker.client.nio.utils.ChannelDecorator;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetPublish;

import java.net.InetSocketAddress;


/**
 * Created by luissantos on 22-04-2014.
 */
@ChannelHandler.Sharable
public class PongMessageHandler extends SimpleChannelInboundHandler<NetMessage> {

    public static final String HEART_BEAT_ACTION_ID = "24bb963d-6d6c-441e-ab4d-999d73578452";

    private static final Logger log = LoggerFactory.getLogger(PongMessageHandler.class);

    PongConsumerManager manager;

    public PongMessageHandler(PongConsumerManager manager) {
        super();

        this.manager = manager;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NetMessage msg){


        NetAction action = msg.getAction();

        if(action.getActionType() != NetAction.ActionType.PONG || action.getPongMessage()  == null){
            ctx.fireChannelRead(msg);
            return;
        }




        try {

            if(getActionId(msg).equals(HEART_BEAT_ACTION_ID)){
                log.debug("Got a heartbeat pong response");
                return;
            }

            log.debug("Got a pong message");

            ChannelDecorator decorator = new ChannelDecorator(ctx.channel());

            manager.deliverMessage( msg, decorator.getHost() );




        } catch (Throwable throwable) {

            log.error("Was not possible to deliver pong message", throwable);

        }finally {
            ctx.fireChannelReadComplete();
        }


    }


    public PongConsumerManager getManager() {
        return manager;
    }

    public void setManager(PongConsumerManager manager) {
        this.manager = manager;
    }


    private static String getActionId(NetMessage netMessage){

        ActionIdDecorator decorator = new ActionIdDecorator(netMessage);

        return  decorator.getActionId();

    }
}
