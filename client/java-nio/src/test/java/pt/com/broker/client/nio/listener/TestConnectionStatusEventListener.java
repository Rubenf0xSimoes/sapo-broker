package pt.com.broker.client.nio.listener;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import io.netty.channel.embedded.EmbeddedChannel;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import pt.com.broker.client.nio.events.connection.ConnectionEventListener;
import pt.com.broker.client.nio.events.connection.ConnectionStatusChangeEventImpl;
import pt.com.broker.client.nio.handlers.ConnectionStatusChangeEventHandler;
import pt.com.broker.client.nio.server.HostInfo;

/**
 * This class tests in simultaneous the ConnectionEventListener interface and the ConnectionStatusChangeEventHandler class. The main goal is to guaranteed that the listeners are called when a ConnectionStatusChangeEvent is generated. As more status are added to the listeners, the test cases will also reflect those status.
 * */
public class TestConnectionStatusEventListener
{

	private final ConnectionEventListener eventListener = mock(ConnectionEventListener.class);
	private final List<ConnectionEventListener> connectionEventListeners = new ArrayList<ConnectionEventListener>();
	private final HostInfo localhost = new HostInfo("localhost", 3323);
	private EmbeddedChannel channel;

	@Before
	public void setup()
	{
		connectionEventListeners.add(eventListener);
		channel = new EmbeddedChannel(
				new ConnectionStatusChangeEventHandler(connectionEventListeners)
				);
	}

	@Test
	public void testConnectionStatusEventConnected()
	{
		// Let's trigger an connection event
		channel.pipeline().fireUserEventTriggered(new ConnectionStatusChangeEventImpl(localhost, HostInfo.STATUS.OPEN));
		// Verify that the connected method is called.
		verify(eventListener).connected(localhost);
	}

	@Test
	public void testConnectionStatusEventDisconnected()
	{
		// Let's trigger 2 disconnected events with different status
		channel.pipeline().fireUserEventTriggered(new ConnectionStatusChangeEventImpl(localhost, HostInfo.STATUS.CLOSED));
		channel.pipeline().fireUserEventTriggered(new ConnectionStatusChangeEventImpl(localhost, HostInfo.STATUS.DISABLE));
		// Verify that the connected method is called.
		verify(eventListener, times(2)).disconnected(localhost);
	}

}
