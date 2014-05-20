package pt.com.broker.client.nio.consumer;


import org.junit.Assert;
import org.junit.Test;
import pt.com.broker.client.nio.events.MessageAcceptedAdapter;
import pt.com.broker.client.nio.events.MessageAcceptedListener;
import pt.com.broker.client.nio.types.ActionIdDecorator;
import pt.com.broker.types.NetAccepted;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetMessage;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by luissantos on 12-05-2014.
 */
public class AcceptRequestsTest {



    @Test()
    public void addRemove(){

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

        PendingAcceptRequestsManager manager = new PendingAcceptRequestsManager(scheduledExecutorService);

        String actionID = UUID.randomUUID().toString();

        MessageAcceptedListener acceptedListener = new MessageAcceptedAdapter() {

            @Override
            public boolean onMessage(NetMessage message) {

                return true;
            }

            @Override
            public void onFault(NetMessage message) {

            }

            @Override
            public void onTimeout(String actionID) {

            }
        };


        try {

            manager.addAcceptRequest(actionID,1000,acceptedListener);

        } catch (Throwable throwable) {

            throwable.printStackTrace();

        }

        MessageAcceptedListener listener1 = manager.getListener(actionID);

        Assert.assertSame(acceptedListener,listener1);


        MessageAcceptedListener listener2 =  manager.removeAcceptRequest(actionID);

        Assert.assertNotNull(listener2);



        MessageAcceptedListener listener3 = manager.getListener(actionID);

        Assert.assertNull(listener3);


    }

    @Test()
    public void testTimeout() throws InterruptedException {

        long timeout = 2000L;

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

        PendingAcceptRequestsManager manager = new PendingAcceptRequestsManager(scheduledExecutorService);

        final String actionID = UUID.randomUUID().toString();

        final AtomicBoolean isTimeout = new AtomicBoolean(false);


        MessageAcceptedListener acceptedListener = new MessageAcceptedAdapter() {

            @Override
            public boolean onMessage(NetMessage message) {

                return true;
            }

            @Override
            public void onFault(NetMessage message) {

            }

            @Override
            public void onTimeout(String _actionID) {

                if(actionID.equals(_actionID)){
                    isTimeout.set(true);
                }

            }
        };


        try {

            manager.addAcceptRequest(actionID,timeout,acceptedListener);

        } catch (Throwable throwable) {

            throwable.printStackTrace();

        }

        scheduledExecutorService.shutdown();
        scheduledExecutorService.awaitTermination(timeout*2, TimeUnit.MILLISECONDS);

        Assert.assertTrue("Timout failed",isTimeout.get());

    }


    @Test()
    public void testDeliver() throws Exception {


        long timeout = 2000L;

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

        PendingAcceptRequestsManager manager = new PendingAcceptRequestsManager(scheduledExecutorService);

        final String actionID = UUID.randomUUID().toString();

        final AtomicBoolean onMessage = new AtomicBoolean(false);


        MessageAcceptedListener acceptedListener = new MessageAcceptedAdapter() {

            @Override
            public boolean onMessage(NetMessage message) {

                ActionIdDecorator decorator = new ActionIdDecorator(message);

                if(actionID.equals(decorator.getActiondId())){
                    onMessage.set(true);
                }


                return true;
            }

            @Override
            public void onFault(NetMessage message) {

            }

            @Override
            public void onTimeout(String _actionID) {


            }
        };


        try {

            manager.addAcceptRequest(actionID,timeout,acceptedListener);

        } catch (Throwable throwable) {

            throwable.printStackTrace();

        }


        NetAccepted netAccepted = new NetAccepted(actionID);

        NetAction netAction = new NetAction(NetAction.ActionType.ACCEPTED);

        netAction.setAcceptedMessage(netAccepted);

        NetMessage netMessage = new NetMessage(netAction);


        manager.deliverMessage(netMessage);

        Assert.assertTrue(onMessage.get());

    }
}
