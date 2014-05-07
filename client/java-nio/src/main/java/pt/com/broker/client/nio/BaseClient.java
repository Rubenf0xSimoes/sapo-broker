package pt.com.broker.client.nio;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.caudexorigo.text.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.client.nio.bootstrap.BaseBootstrap;
import pt.com.broker.client.nio.bootstrap.Bootstrap;
import pt.com.broker.client.nio.bootstrap.ChannelInitializer;
import pt.com.broker.client.nio.utils.HostContainer;
import pt.com.broker.types.*;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Created by luissantos on 05-05-2014.
 */
public abstract class BaseClient {

    private static final Logger log = LoggerFactory.getLogger(BaseClient.class);

    protected HostContainer hosts;

    BaseBootstrap bootstrap;



    public BaseClient(NetProtocolType ptype) {
        init(ptype);
    }

    public BaseClient(String host, int port) {

        this(new HostInfo(host, port), NetProtocolType.JSON);


    }

    public BaseClient(String host, int port, NetProtocolType ptype) {

        this(new HostInfo(host, port), ptype);


    }

    public BaseClient(HostInfo host, NetProtocolType ptype) {

        this(ptype);

        this.addServer(host);
    }


    protected ChannelFuture sendNetMessage(NetMessage msg) {
        return this.sendNetMessage(msg,null);
    }

    protected ChannelFuture sendNetMessage(NetMessage msg, Channel c) {

        Channel channel = (c == null) ? getChannel() : c;

        return channel.writeAndFlush(msg);

    }

    protected NetMessage buildMessage(NetAction action, Map<String, String> headers) {
        NetMessage message = new NetMessage(action, headers);

        return message;
    }


    protected NetMessage buildMessage(NetAction action) {
        return this.buildMessage(action, new HashMap<String, String>());
    }

    public ChannelFuture publishMessage(String brokerMessage, String destinationName,NetAction.DestinationType dtype) {

        return publishMessage(brokerMessage.getBytes(), destinationName, dtype);
    }

    public ChannelFuture publishMessage(byte[] brokerMessage, String destinationName , NetAction.DestinationType dtype) {

        NetBrokerMessage msg = new NetBrokerMessage(brokerMessage);

        return publishMessage(msg, destinationName, dtype);
    }

    public ChannelFuture publishMessage(NetBrokerMessage brokerMessage, String destination, NetAction.DestinationType dtype) {

        if ((brokerMessage == null) || StringUtils.isBlank(destination)) {
            throw new IllegalArgumentException("Mal-formed Enqueue request");
        }

        NetPublish publish = new NetPublish(destination, dtype, brokerMessage);


        return publishMessage(publish,destination,dtype);

    }

    public ChannelFuture publishMessage(NetPublish message, String destination, NetAction.DestinationType dtype) {

        NetAction action = new NetAction(message);

        return sendNetMessage(new NetMessage(action, message.getMessage().getHeaders()));

    }

    protected Channel getChannel() {

        Channel c = getHosts().getActiveChannel();

        log.debug("Selected channel is: "+c.toString());

        if(c==null){
            throw new RuntimeException("Was not possible to get an active channel");
        }

        return c;
    }

    public Future<HostInfo> connect() throws Exception {

        return hosts.connect();

    }

    public HostContainer getHosts() {
        return hosts;
    }

    public void setHosts(HostContainer hosts) {
        this.hosts = hosts;
    }

    public BaseBootstrap getBootstrap() {
        return bootstrap;
    }

    public void setBootstrap(BaseBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public Future close() {
        return getBootstrap().shutdownGracefully();
    }


    public void addServer(HostInfo host) {

        getHosts().add(host);
    }

    public void addServer(String hostname, int port) {

        this.addServer(new HostInfo(hostname, port));
    }


    protected  abstract void init(NetProtocolType ptype);



}


