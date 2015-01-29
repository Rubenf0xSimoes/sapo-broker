package pt.com.broker.client.nio;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.client.nio.bootstrap.BaseBootstrap;
import pt.com.broker.client.nio.codecs.BindingSerializerFactory;
import pt.com.broker.client.nio.server.HostContainer;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.client.nio.utils.ChannelWrapperFuture;
import pt.com.broker.client.nio.utils.HostInfoFuture;
import pt.com.broker.types.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by luissantos on 05-05-2014.
 *
 * @author vagrant
 * @version $Id: $Id
 */
public abstract class BaseClient{

    private static final Logger log = LoggerFactory.getLogger(BaseClient.class);
    
    protected HostContainer hosts;
    BaseBootstrap bootstrap;
    BindingSerializer serializer = null;
    NetProtocolType protocolType = NetProtocolType.JSON;

    /**
     * <p>Constructor for BaseClient.</p>
     *
     * @param ptype a {@link pt.com.broker.types.NetProtocolType} object.
     */
    public BaseClient(NetProtocolType ptype) {

        setProtocolType(ptype);
        init();

    }

    /**
     * <p>Constructor for BaseClient.</p>
     *
     * @param host a {@link java.lang.String} object.
     * @param port a int.
     */
    public BaseClient(String host, int port) {
        this(new HostInfo(host, port), NetProtocolType.PROTOCOL_BUFFER);
    }

    /**
     * <p>Constructor for BaseClient.</p>
     *
     * @param host a {@link java.lang.String} object.
     * @param port a int.
     * @param ptype a {@link pt.com.broker.types.NetProtocolType} object.
     */
    public BaseClient(String host, int port, NetProtocolType ptype) {

        this(new HostInfo(host, port), ptype);


    }

    /**
     * <p>Constructor for BaseClient.</p>
     *
     * @param host a {@link pt.com.broker.client.nio.server.HostInfo} object.
     * @param ptype a {@link pt.com.broker.types.NetProtocolType} object.
     */
    public BaseClient(HostInfo host, NetProtocolType ptype) {

        this(ptype);

        this.addServer(host);
    }

    /**
     * <p>sendNetMessage.</p>
     *
     * @param msg a {@link pt.com.broker.types.NetMessage} object.
     * @return a {@link io.netty.channel.ChannelFuture} object.
     */
    protected HostInfoFuture sendNetMessage(NetMessage msg) {

        HostInfo host;

        try {

            host = getAvailableHost();

            return this.sendNetMessage(msg, host);

        }catch (Exception e){
            return new HostNotAvailableFuture<HostInfo>();
        }




    }

    /**
     * <p>sendNetMessage.</p>
     *
     * @param msg a {@link pt.com.broker.types.NetMessage} object.
     * @param host a {@link pt.com.broker.client.nio.server.HostInfo} object.
     * @return a {@link io.netty.channel.ChannelFuture} object.
     */
    protected ChannelWrapperFuture sendNetMessage(NetMessage msg, HostInfo host) {


        Channel channel = host.getChannel();

        if(channel == null){
            throw new RuntimeException("Host not connected");
        }

        ChannelFuture f =  channel.writeAndFlush(msg);

        f.addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
            	//FIXME: This code should retry at least 3 times to resend the message.
            	//Is it suposed to be here the resend mechanics?
                if(!future.isSuccess()){
                    log.error("Error sending message!!! Message lost",future.cause());
                }
            }
        });


        return new ChannelWrapperFuture(f);

    }



    /**
     * <p>buildMessage.</p>
     *
     * @param action a {@link pt.com.broker.types.NetAction} object.
     * @param headers a {@link java.util.Map} object.
     * @return a {@link pt.com.broker.types.NetMessage} object.
     */
    protected NetMessage buildMessage(NetAction action, Map<String, String> headers) {
        NetMessage message = new NetMessage(action, headers);

        return message;
    }


    /**
     * <p>buildMessage.</p>
     *
     * @param action a {@link pt.com.broker.types.NetAction} object.
     * @return a {@link pt.com.broker.types.NetMessage} object.
     */
    protected NetMessage buildMessage(NetAction action) {
        return this.buildMessage(action, new HashMap<String, String>());
    }

    /**
     * <p>publish.</p>
     *
     * @param brokerMessage a {@link java.lang.String} object.
     * @param destinationName a {@link java.lang.String} object.
     * @param dtype a {@link pt.com.broker.types.NetAction.DestinationType} object.
     * @return a {@link java.util.concurrent.Future} object.
     */
    public Future<HostInfo> publish(String brokerMessage, String destinationName, NetAction.DestinationType dtype) {

        return publish(brokerMessage.getBytes(), destinationName, dtype);
    }

    /**
     * <p>publish.</p>
     *
     * @param brokerMessage an array of byte.
     * @param destinationName a {@link java.lang.String} object.
     * @param dtype a {@link pt.com.broker.types.NetAction.DestinationType} object.
     * @return a {@link java.util.concurrent.Future} object.
     */
    public Future<HostInfo> publish(byte[] brokerMessage, String destinationName, NetAction.DestinationType dtype) {

        NetBrokerMessage msg = new NetBrokerMessage(brokerMessage);

        return publish(msg, destinationName, dtype);
    }

    /**
     * <p>publish.</p>
     *
     * @param brokerMessage a {@link pt.com.broker.types.NetBrokerMessage} object.
     * @param destination a {@link java.lang.String} object.
     * @param dtype a {@link pt.com.broker.types.NetAction.DestinationType} object.
     * @return a {@link java.util.concurrent.Future} object.
     */
    public Future<HostInfo> publish(NetBrokerMessage brokerMessage, String destination, NetAction.DestinationType dtype) {

        if ((brokerMessage == null) || StringUtils.isBlank(destination)) {
            throw new IllegalArgumentException("Mal-formed Enqueue request");
        }

        NetPublish publish = new NetPublish(destination, dtype, brokerMessage);


        return publish(publish, destination, dtype);

    }

    /**
     * <p>publish.</p>
     *
     * @param message a {@link pt.com.broker.types.NetPublish} object.
     * @param destination a {@link java.lang.String} object.
     * @param dtype a {@link pt.com.broker.types.NetAction.DestinationType} object.
     * @return a {@link java.util.concurrent.Future} object.
     */
    public Future<HostInfo> publish(NetPublish message, String destination, NetAction.DestinationType dtype) {

        NetAction action = new NetAction(message);

        return sendNetMessage(new NetMessage(action, message.getMessage().getHeaders()));

    }



    /**
     * <p>getAvailableHost.</p>
     *
     * @return a {@link pt.com.broker.client.nio.server.HostInfo} object.
     */
    protected HostInfo getAvailableHost(){

       HostInfo h = null;

        try {

            h = getHosts().getAvailableHost();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(h==null){
            throw new RuntimeException("Was not possible to get an active channel");
        }

//        log.debug("Selected channel is: "+ h);

        return h;
    }

    /**
     * <p>connect.</p>
     *
     * @return a {@link pt.com.broker.client.nio.server.HostInfo} object.
     */
    public HostInfo connect(){

        return hosts.connect();

    }

    /**
     * <p>connectAsync.</p>
     *
     * @return a {@link java.util.concurrent.Future} object.
     */
    public Future<HostInfo> connectAsync(){

        return hosts.connectAsync();

    }


    /**
     * <p>Getter for the field <code>hosts</code>.</p>
     *
     * @return a {@link pt.com.broker.client.nio.server.HostContainer} object.
     */
    public HostContainer getHosts() {
        return hosts;
    }

    /**
     * <p>Setter for the field <code>hosts</code>.</p>
     *
     * @param hosts a {@link pt.com.broker.client.nio.server.HostContainer} object.
     */
    public void setHosts(HostContainer hosts) {
        this.hosts = hosts;
    }

    /**
     * <p>Getter for the field <code>bootstrap</code>.</p>
     *
     * @return a {@link pt.com.broker.client.nio.bootstrap.BaseBootstrap} object.
     */
    protected BaseBootstrap getBootstrap() {
        return bootstrap;
    }

    /**
     * <p>Setter for the field <code>bootstrap</code>.</p>
     *
     * @param bootstrap a {@link pt.com.broker.client.nio.bootstrap.BaseBootstrap} object.
     */
    public void setBootstrap(BaseBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    /**
     * <p>close.</p>
     *
     * @return a {@link java.util.concurrent.Future} object.
     */
    public Future close() {

         getHosts().disconnect();

        return getBootstrap().shutdownGracefully();

    }


    /**
     * <p>addServer.</p>
     *
     * @param host a {@link pt.com.broker.client.nio.server.HostInfo} object.
     */
    public void addServer(HostInfo host) {

        getHosts().add(host);
    }

    /**
     * <p>addServer.</p>
     *
     * @param hostname a {@link java.lang.String} object.
     * @param port a int.
     * @return a {@link pt.com.broker.client.nio.server.HostInfo} object.
     */
    public HostInfo addServer(String hostname, int port) {

        HostInfo host = new HostInfo(hostname, port);

        this.addServer(host);

        return host;
    }


    /**
     * <p>init.</p>
     */
    protected  abstract void init();


    /**
     * <p>Getter for the field <code>protocolType</code>.</p>
     *
     * @return a {@link pt.com.broker.types.NetProtocolType} object.
     */
    public NetProtocolType getProtocolType() {
        return protocolType;
    }

    /**
     * <p>Setter for the field <code>protocolType</code>.</p>
     *
     * @param protocolType a {@link pt.com.broker.types.NetProtocolType} object.
     */
    public void setProtocolType(NetProtocolType protocolType) {

        this.protocolType = protocolType;

        try {
            serializer = BindingSerializerFactory.getInstance(protocolType);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    /**
     * <p>Getter for the field <code>serializer</code>.</p>
     *
     * @return a {@link pt.com.broker.types.BindingSerializer} object.
     */
    protected BindingSerializer getSerializer() {
        return serializer;
    }


    protected class ExceptionFuture<T extends HostInfo> extends HostInfoFuture {


        Exception exception;

        public ExceptionFuture(Exception exception) {
            this.exception = exception;
        }

        public Exception getException() {
            return exception;
        }

        @Override
        public boolean cancel(boolean b) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return true;
        }

        @Override
        public boolean isDone() {
            return false;
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            throw new ExecutionException(getException());
        }

        @Override
        public T get(long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, java.util.concurrent.TimeoutException {
            return get();
        }


    }

    protected class HostNotConnected<T extends HostInfo> extends ExceptionFuture<T> {


        public HostNotConnected() {
            super(new Exception("No Host available to connect"));
        }

    };

    protected class HostNotAvailableFuture<T extends HostInfo> extends ExceptionFuture<T> {


        public HostNotAvailableFuture() {
            super(new Exception("No Host available to connect"));
        }

    };
}


