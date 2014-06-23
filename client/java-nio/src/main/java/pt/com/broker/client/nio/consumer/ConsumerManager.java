package pt.com.broker.client.nio.consumer;


import org.caudexorigo.text.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.client.nio.events.BrokerListener;
import pt.com.broker.client.nio.types.DestinationDataDelegator;
import pt.com.broker.types.*;
import pt.com.broker.types.NetAction.DestinationType;

import java.net.InetSocketAddress;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by luissantos on 22-04-2014.
 *
 * @author vagrant
 * @version $Id: $Id
 */
public class ConsumerManager {

    private static final Logger log = LoggerFactory.getLogger(ConsumerManager.class);

    protected final EnumMap<NetAction.DestinationType, Map<String, BrokerAsyncConsumer>> _consumerList = new EnumMap<NetAction.DestinationType, Map<String, BrokerAsyncConsumer>>(NetAction.DestinationType.class);



    /**
     * <p>Constructor for ConsumerManager.</p>
     */
    public ConsumerManager() {

        _consumerList.put(DestinationType.TOPIC, new ConcurrentHashMap<String, BrokerAsyncConsumer>());
        _consumerList.put(DestinationType.QUEUE, new ConcurrentHashMap<String, BrokerAsyncConsumer>()); // VIRTAL_QUEUE BEHAVES THE SAME WAY AS QUEUE

    }




    /**
     * <p>addSubscription.</p>
     *
     * @param subscribe a {@link pt.com.broker.types.NetSubscribeAction} object.
     * @param listener a {@link pt.com.broker.client.nio.events.BrokerListener} object.
     * @param hostInfo a {@link pt.com.broker.client.nio.server.HostInfo} object.
     */
    public void addSubscription(NetSubscribeAction subscribe, BrokerListener listener, HostInfo hostInfo){

        BrokerAsyncConsumer consumer = new BrokerAsyncConsumer(subscribe.getDestination(), subscribe.getDestinationType() , listener);

        consumer.setHost(hostInfo);

        addSubscription(consumer);
    }


    private InetSocketAddress getSocket(HostInfo host){
        InetSocketAddress socketAddress = new InetSocketAddress(host.getHostname(),host.getPort());

        return socketAddress;
    }

    private String getDestinationKey(String destination , DestinationType destinationType  , HostInfo host ){

        if(destinationType == DestinationType.TOPIC){
            return destination;
        }


        InetSocketAddress socketAddress = getSocket(host);

        if(StringUtils.isEmpty(destination)){
            throw new IllegalArgumentException("Invalid Destination");
        }

        String hostname = socketAddress.getHostName();

        int port = socketAddress.getPort();

        return hostname + ":" + port +"#"+ destination;
    }

    /**
     * <p>addSubscription.</p>
     *
     * @param consumer a {@link pt.com.broker.client.nio.consumer.BrokerAsyncConsumer} object.
     */
    public void addSubscription(BrokerAsyncConsumer consumer){

        DestinationType destinationType = consumer.getDestinationType();

        if(StringUtils.isEmpty(consumer.getDestinationName())){
            throw new IllegalArgumentException("Invalid Destination name");
        }

        if(destinationType == null){
            throw new IllegalArgumentException("Invalid Destination Type");
        }

        String destination = getDestinationKey(consumer.getDestinationName(),consumer.getDestinationType(),consumer.getHost());

        synchronized (_consumerList){

                Map<String, BrokerAsyncConsumer> subscriptions = getSubscriptions(destinationType);


                if (subscriptions.containsKey(destination))
                {
                    throw new IllegalArgumentException("A listener for the destination "+destination+" already exists");
                }

                subscriptions.put(destination,consumer);
                log.info("Added Async Consumer for {} {} ", consumer.getHost(), consumer.getDestinationName());
        }

    }

    /**
     * <p>removeSubscription.</p>
     *
     * @param netSubscribeAction a {@link pt.com.broker.types.NetSubscribeAction} object.
     * @param host a {@link pt.com.broker.client.nio.server.HostInfo} object.
     * @return a {@link pt.com.broker.client.nio.consumer.BrokerAsyncConsumer} object.
     */
    public BrokerAsyncConsumer removeSubscription(NetSubscribeAction netSubscribeAction, HostInfo host ){
        return removeSubscription(netSubscribeAction.getDestinationType(),netSubscribeAction.getDestination(), host);
    }

    /**
     * <p>removeSubscription.</p>
     *
     * @param destinationType a {@link pt.com.broker.types.NetAction.DestinationType} object.
     * @param destinationType a {@link pt.com.broker.types.NetAction.DestinationType} object.
     * @param destination a {@link java.lang.String} object.
     * @param host a {@link pt.com.broker.client.nio.server.HostInfo} object.
     * @return a {@link pt.com.broker.client.nio.consumer.BrokerAsyncConsumer} object.
     */
    public BrokerAsyncConsumer removeSubscription(DestinationType destinationType, String destination , HostInfo host ){

        synchronized (_consumerList) {

            Map<String, BrokerAsyncConsumer> subscriptions = getSubscriptions(destinationType);

            String key = getDestinationKey(destination,destinationType, host);

            BrokerAsyncConsumer brokerAsyncConsumer =  subscriptions.remove(key);

            if(brokerAsyncConsumer!=null){
                log.debug("Removing key: "+key);
            }

            return brokerAsyncConsumer;
        }
    }


    /**
     * <p>getConsumer.</p>
     *
     * @param destinationType a {@link pt.com.broker.types.NetAction.DestinationType} object.
     * @param destinationType a {@link pt.com.broker.types.NetAction.DestinationType} object.
     * @param destination a {@link java.lang.String} object.
     * @param host a {@link pt.com.broker.client.nio.server.HostInfo} object.
     * @return a {@link pt.com.broker.client.nio.consumer.BrokerAsyncConsumer} object.
     */
    public BrokerAsyncConsumer getConsumer(DestinationType destinationType , String destination ,  HostInfo host){

        Map<String, BrokerAsyncConsumer> subscriptions = getSubscriptions(destinationType);

        return subscriptions.get(getDestinationKey(destination,destinationType,host));

    }

    /**
     * <p>getConsumer.</p>
     *
     * @param netMessage a {@link pt.com.broker.types.NetMessage} object.
     * @param host a {@link pt.com.broker.client.nio.server.HostInfo} object.
     * @return a {@link pt.com.broker.client.nio.consumer.BrokerAsyncConsumer} object.
     */
    protected BrokerAsyncConsumer getConsumer(NetMessage netMessage, HostInfo host){


        DestinationDataDelegator delegator = new DestinationDataDelegator(netMessage);


        String destination = delegator.getSubscription();
        DestinationType dtype = delegator.getDestinationType();

        return getConsumer(dtype,destination,host);

    }

    /**
     * <p>getSubscriptions.</p>
     *
     * @param dtype a {@link pt.com.broker.types.NetAction.DestinationType} object.
     * @return a {@link java.util.Map} object.
     */
    public Map<String, BrokerAsyncConsumer> getSubscriptions(NetAction.DestinationType dtype){

        /* VirtualQueue is also queue so we must test this */
        DestinationType type = DestinationType.TOPIC.equals(dtype) ? DestinationType.TOPIC : DestinationType.QUEUE;

        Map<String, BrokerAsyncConsumer> subscriptions =  _consumerList.get(type);

        return subscriptions;

    }



    /**
     * <p>deliverMessage.</p>
     *
     * @param netMessage a {@link pt.com.broker.types.NetMessage} object.
     * @param host a {@link pt.com.broker.client.nio.server.HostInfo} object.
     * @throws java.lang.Throwable if any.
     */
    public void deliverMessage(NetMessage netMessage, HostInfo host) throws Throwable {




        BrokerAsyncConsumer consumer = getConsumer(netMessage, host);


        if(consumer == null){

            log.warn("No consumer found for message: "+netMessage);
            return;
        }


        consumer.deliver(netMessage, host);

    }


    /**
     * <p>getSubscriptions.</p>
     *
     * @param dtype a {@link pt.com.broker.types.NetAction.DestinationType} object.
     * @param host a {@link pt.com.broker.client.nio.server.HostInfo} object.
     * @return a {@link java.util.Map} object.
     */
    public Map<String, BrokerAsyncConsumer> getSubscriptions(NetAction.DestinationType dtype, HostInfo host){

        Map<String, BrokerAsyncConsumer> map = new HashMap<String, BrokerAsyncConsumer>();

        for(Map.Entry<String, BrokerAsyncConsumer> entry  : getSubscriptions(dtype).entrySet()){
            String key = entry.getKey();
            BrokerAsyncConsumer consumer = entry.getValue();

            if(consumer.getHost().equals(host)){
                map.put(key,consumer);
            }
        }

        return map;
    }

    /**
     * <p>removeSubscriptions.</p>
     *
     * @param dtype a {@link pt.com.broker.types.NetAction.DestinationType} object.
     * @param host a {@link pt.com.broker.client.nio.server.HostInfo} object.
     * @return a {@link java.util.Map} object.
     */
    public Map<String, BrokerAsyncConsumer> removeSubscriptions(NetAction.DestinationType dtype, HostInfo host){

        synchronized (_consumerList) {

            Map<String, BrokerAsyncConsumer> map = new HashMap<String, BrokerAsyncConsumer>(2);

            for (Map.Entry<String, BrokerAsyncConsumer> entry : getSubscriptions(dtype).entrySet()) {

                String key = entry.getKey();
                BrokerAsyncConsumer consumer = entry.getValue();

                if(removeSubscription(dtype, consumer.getDestinationName(), host)!=null){
                    map.put(key, consumer);
                }

            }

            return map;
        }
    }





}
