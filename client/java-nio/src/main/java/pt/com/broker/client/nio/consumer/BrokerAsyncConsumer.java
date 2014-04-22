package pt.com.broker.client.nio.consumer;

import pt.com.broker.client.nio.events.BrokerListener;
import pt.com.broker.client.nio.events.BrokerListenerAdapter;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetSubscribe;

/**
 * Created by luissantos on 22-04-2014.
 */
public class BrokerAsyncConsumer {

    private final NetSubscribe subscription;

    private final BrokerListener listener;

    public BrokerAsyncConsumer(NetSubscribe subscrition, BrokerListener listener)
    {
        super();
        this.listener = listener;
        this.subscription = subscrition;
    }

    public boolean deliver(NetNotification msg) throws Throwable {

        if(listener instanceof BrokerListenerAdapter){
            ((BrokerListenerAdapter) listener).deliverMessage(msg);
        }else{
            listener.onMessage(msg);
        }


        return true;
    }

    public NetSubscribe getSubscription() {
        return subscription;
    }

    public BrokerListener getListener() {
        return listener;
    }
}
