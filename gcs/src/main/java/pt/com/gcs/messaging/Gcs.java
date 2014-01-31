package pt.com.gcs.messaging;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.caudexorigo.Shutdown;
import org.caudexorigo.concurrent.CustomExecutors;
import org.caudexorigo.text.StringUtils;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.Headers;
import pt.com.broker.types.MessageListener;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetPublish;
import pt.com.gcs.conf.GcsInfo;
import pt.com.gcs.conf.GlobalConfig;
import pt.com.gcs.net.Peer;
import pt.com.gcs.net.codec.GcsDecoder;
import pt.com.gcs.net.codec.GcsEncoder;

/**
 * Gcs is a facade for handling several message related functionality such as publish, acknowledge, etc.
 */

public class Gcs
{
	private static Logger log = LoggerFactory.getLogger(Gcs.class);

	private static final String SERVICE_NAME = "SAPO GCS";

	private static final Gcs instance = new Gcs();

	public static final int RECOVER_INTERVAL = 50;

	public static final int RECONNECT_INTERVAL = 5000;

	private Set<Channel> agentsConnection = new HashSet<Channel>();

	private ClientBootstrap connector;

	private static final long EXPIRATION_TIME;

	public static void ackMessage(String queueName, final String msgId)
	{
		if (StringUtils.isBlank(queueName))
		{
			throw new IllegalArgumentException("Can not acknowledge a message with a blank queue name");
		}
		if (StringUtils.isBlank(msgId))
		{
			throw new IllegalArgumentException("Can not acknowledge a message with a blank message-id");
		}
		instance.iackMessage(queueName, msgId);
	}

	public static void addAsyncConsumer(String subscriptionKey, MessageListener listener)
	{
		if (StringUtils.isBlank(subscriptionKey))
		{
			throw new IllegalArgumentException("Can not make a subscription with a blank subscription name");
		}

		if (listener == null)
		{
			throw new IllegalArgumentException("Can not make a subscription with a null listener");
		}

		if (listener.getSourceDestinationType() == DestinationType.TOPIC)
		{
			instance.iaddTopicConsumer(subscriptionKey, listener);
		}
		else if (listener.getSourceDestinationType() == DestinationType.QUEUE)
		{
			instance.iaddQueueConsumer(subscriptionKey, listener);
		}
	}

	protected static void connect(SocketAddress address)
	{
		if (GlobalConfig.contains((InetSocketAddress) address))
		{
			String remoteAgentId = OutboundRemoteChannels.socketToAgentId(address);

			Channel channel = OutboundRemoteChannels.get(remoteAgentId);
			if (channel != null)
			{
				// A connection to the remote agent existed. Close it!
				OutboundRemoteChannels.remove(remoteAgentId);
				ChannelFuture channelFuture = channel.close();
				channelFuture.awaitUninterruptibly(5, TimeUnit.SECONDS);
			}

			log.info("Connecting to '{}'.", address.toString());

			ChannelFuture cf = instance.connector.connect(address);
			cf.awaitUninterruptibly(5, TimeUnit.SECONDS);

			boolean sucess = cf.isSuccess();

			if (!sucess)
			{
				log.info("Connection fail to '{}'.", address.toString());
				if (!cf.isDone())
				{
					cf.cancel();
					// If the connection is established between isDone and Cancel, close it
					if (cf.getChannel().isConnected())
					{
						log.warn("Connection to '{}' established after beeing canceled.", address.toString());
						cf.getChannel().close();
					}
				}
				GcsExecutor.schedule(new Connect(address), RECONNECT_INTERVAL, TimeUnit.MILLISECONDS);
			}
			else
			{
				OutboundRemoteChannels.add(remoteAgentId, cf.getChannel());
				log.info("Connection established to '{}'.", address.toString());
				synchronized (instance.agentsConnection)
				{
					instance.agentsConnection.add(cf.getChannel());
				}
			}
		}
		else
		{
			log.info("Peer '{}' does not appear in the world map, it will be ignored.", address.toString());
		}
	}

	public static boolean enqueue(NetMessage nmsg, String queueName)
	{
		nmsg.getHeaders().put("TYPE", "COM_QUEUE");
		return instance.ienqueue(nmsg, queueName);
	}

	protected static void reloadWorldMap()
	{
		log.info("Reloading the world map");
		Set<Channel> connectedSessions = getManagedConnectorSessions();

		ArrayList<Channel> sessionsToClose = new ArrayList<Channel>(connectedSessions.size());

		for (Channel channel : connectedSessions)
		{
			InetSocketAddress inet = (InetSocketAddress) channel.getRemoteAddress();

			// remove connections to agents that were removed from world map
			if (!GlobalConfig.contains(inet))
			{
				log.info("Remove peer '{}'", inet.toString());
				sessionsToClose.add(channel);
			}
		}
		for (Channel channel : sessionsToClose)
		{
			channel.close();
		}

		List<InetSocketAddress> remoteSessions = new ArrayList<InetSocketAddress>(connectedSessions.size());
		for (Channel channel : connectedSessions)
		{
			remoteSessions.add((InetSocketAddress) channel.getRemoteAddress());
		}

		List<Peer> peerList = GlobalConfig.getPeerList();
		for (Peer peer : peerList)
		{
			SocketAddress addr = new InetSocketAddress(peer.getHost(), peer.getPort());
			// Connect only if not already connected
			if (!remoteSessions.contains(addr))
			{
				connect(addr);
			}
		}
	}

	public static Set<Channel> getManagedConnectorSessions()
	{
		LinkedHashSet<Channel> connections = null;
		synchronized (instance.agentsConnection)
		{
			connections = new LinkedHashSet<Channel>(instance.agentsConnection);
		}

		return connections;
	}

	protected static List<Peer> getPeerList()
	{
		return GlobalConfig.getPeerList();
	}

	public static void destroy()
	{
		instance.idestroy();
	}

	public static void init()
	{
		instance.iinit();
	}

	public static void publish(NetPublish np)
	{
		instance.ipublish(np);
	}

	public static void removeAsyncConsumer(MessageListener listener)
	{
		if (listener != null)
		{
			if (listener.getSourceDestinationType() == DestinationType.TOPIC)
			{
				TopicProcessorList.removeListener(listener);

			}
			else if (listener.getSourceDestinationType() == DestinationType.QUEUE)
			{
				QueueProcessorList.removeListener(listener);
			}
		}
		else
		{
			log.warn("Can not remove null listener");
		}
	}

	static
	{
		EXPIRATION_TIME = GcsInfo.getMessageStorageTime();
	}

	private Gcs()
	{
	}

	private void connectToAllPeers()
	{
		log.info("connectToAllPeers()");
		List<Peer> peerList = GlobalConfig.getPeerList();

		String peers = peerList.toString();

		log.info("Peers: {}", peers);

		for (Peer peer : peerList)
		{
			SocketAddress addr = new InetSocketAddress(peer.getHost(), peer.getPort());
			connect(addr);
		}
	}

	private void iackMessage(String queueName, final String msgId)
	{
		if (!QueueProcessorList.hasQueue(queueName))
		{
			log.warn(String.format("Trying to acknowledge a message whose queue doesn't exists. Queue: '%s', MsgId: '%s' ", queueName, msgId));
			return;
		}

		QueueProcessor queueProcessor = QueueProcessorList.get(queueName);
		if (queueProcessor != null)
		{
			queueProcessor.ack(msgId);
		}
	}

	private void iaddQueueConsumer(String queueName, MessageListener listener)
	{
		if (listener != null)
		{
			QueueProcessor qp = QueueProcessorList.get(queueName);
			if (qp != null)
			{
				qp.add(listener);
			}
		}
	}

	private void iaddTopicConsumer(String subscriptionName, MessageListener listener)
	{
		if (listener != null)
		{
			TopicProcessor topicProcessor = TopicProcessorList.get(subscriptionName);
			if (topicProcessor != null)
			{
				topicProcessor.add(listener, true);
			}
		}
	}

	private boolean ienqueue(NetMessage nmsg, String queueName)
	{
		QueueProcessor qp = QueueProcessorList.get(queueName);
		if (qp != null)
		{
			qp.getQueueStatistics().newQueueMessageReceived();
			qp.store(nmsg, GlobalConfig.preferLocalConsumers());
			return true;
		}

		return false;
	}

	private void iinit()
	{
		if (GlobalConfig.supportVirtualQueues())
		{
			String[] virtual_queues = VirtualQueueStorage.getVirtualQueueNames();

			for (String vqueue : virtual_queues)
			{
				log.debug("Add VirtualQueue '{}' from storage", vqueue);
				iaddQueueConsumer(vqueue, null);
			}
		}
		else
		{
			log.info("Virtual Queues not supported.");
		}

		String[] queues = BDBEnviroment.getQueueNames();

		for (String queueName : queues)
		{
			QueueProcessorList.get(queueName);
		}

		log.info("{} starting.", SERVICE_NAME);
		try
		{
			startAcceptor(GcsInfo.getAgentPort());
			startConnector();

			GcsExecutor.scheduleWithFixedDelay(new QueueCounter(), 20, 20, TimeUnit.SECONDS);
			GcsExecutor.scheduleWithFixedDelay(new GlobalConfigMonitor(), 30, 30, TimeUnit.SECONDS);
			GcsExecutor.scheduleWithFixedDelay(new GlobalStatisticsPublisher(), 60, 60, TimeUnit.SECONDS);

			GcsExecutor.scheduleWithFixedDelay(new QueueLister(), 5, 5, TimeUnit.MINUTES);

			GcsExecutor.scheduleWithFixedDelay(new ExpiredMessagesDeleter(), 10, 10, TimeUnit.MINUTES);

			GcsExecutor.scheduleWithFixedDelay(new QueueWatchDog(), 2, 2, TimeUnit.MINUTES);

			GcsExecutor.scheduleWithFixedDelay(new PingPeers(), 5, 5, TimeUnit.MINUTES);

			GcsExecutor.scheduleWithFixedDelay(new StaleQueueCleaner(), GlobalConfig.getQueueMaxStaleAge(), GlobalConfig.getQueueMaxStaleAge(), TimeUnit.MILLISECONDS);
		}
		catch (Throwable t)
		{
			Shutdown.now(t);
		}

		// Sleep.time(GcsInfo.getInitialDelay());

		connectToAllPeers();

		log.info("{} initialized.", SERVICE_NAME);
	}

	private void idestroy()
	{
		try
		{
			BDBEnviroment.sync();
		}
		catch (Throwable te)
		{
			log.error(te.getMessage(), te);
		}
	}

	private void ipublish(final NetPublish np)
	{
		TopicProcessorList.notify(np, false);
	}

	private void startAcceptor(int portNumber) throws IOException
	{
		ThreadPoolExecutor tpe_io = CustomExecutors.newCachedThreadPool("gcs-io-1");
		ThreadPoolExecutor tpe_workers = CustomExecutors.newCachedThreadPool("gcs-worker-1");

		ChannelFactory factory = new NioServerSocketChannelFactory(tpe_io, tpe_workers);
		ServerBootstrap bootstrap = new ServerBootstrap(factory);

		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);
		bootstrap.setOption("child.receiveBufferSize", 128 * 1024);
		bootstrap.setOption("child.sendBufferSize", 128 * 1024);
		bootstrap.setOption("reuseAddress", true);
		bootstrap.setOption("backlog", 1024);

		ChannelPipelineFactory serverPipelineFactory = new ChannelPipelineFactory()
		{
			@Override
			public ChannelPipeline getPipeline() throws Exception
			{
				ChannelPipeline pipeline = Channels.pipeline();

				pipeline.addLast("broker-encoder", new GcsEncoder());

				pipeline.addLast("broker-decoder", new GcsDecoder());

				pipeline.addLast("broker-handler", new GcsAcceptorProtocolHandler());

				return pipeline;
			}
		};

		bootstrap.setPipelineFactory(serverPipelineFactory);

		InetSocketAddress inet = new InetSocketAddress("0.0.0.0", portNumber);
		bootstrap.bind(inet);
		log.info("SAPO-BROKER Listening on: '{}'.", inet.toString());
		log.info("{} listening on: '{}'.", SERVICE_NAME, inet.toString());
	}

	private void startConnector()
	{
		log.info("Starting Local Connector - step 0");

		ThreadPoolExecutor tpe_io = CustomExecutors.newCachedThreadPool("gcs-io-2");

		log.info("Starting Local Connector - step 1");

		ThreadPoolExecutor tpe_workers = CustomExecutors.newCachedThreadPool("gcs-worker-2");

		log.info("Starting Local Connector - step 2");

		ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(tpe_io, tpe_workers));

		log.info("Starting Local Connector - step 3");

		ChannelPipelineFactory connectorPipelineFactory = new ChannelPipelineFactory()
		{
			@Override
			public ChannelPipeline getPipeline() throws Exception
			{
				ChannelPipeline pipeline = Channels.pipeline();

				pipeline.addLast("broker-encoder", new GcsEncoder());

				pipeline.addLast("broker-decoder", new GcsDecoder());

				pipeline.addLast("broker-handler", new GcsRemoteProtocolHandler());

				return pipeline;
			}
		};
		log.info("Starting Local Connector - step 4");

		bootstrap.setPipelineFactory(connectorPipelineFactory);

		log.info("Starting Local Connector - step 5");

		// try
		// {
		// bootstrap.setOption("localAddress", new InetSocketAddress(Inet4Address.getByName(GcsInfo.getAgentHost()), 0));
		// }
		// catch (UnknownHostException e)
		// {
		// log.error(String.format("Failed to bind to local host address. Address: '%s'.", GcsInfo.getAgentHost()), e);
		// }

		bootstrap.setOption("child.keepAlive", true);
		bootstrap.setOption("child.receiveBufferSize", 128 * 1024);
		bootstrap.setOption("child.sendBufferSize", 128 * 1024);
		bootstrap.setOption("connectTimeoutMillis", 5000);

		log.info("Starting Local Connector - step 6");

		this.connector = bootstrap;
	}

	public synchronized static void deleteQueue(String queueName, boolean safe)
	{
		QueueProcessorList.remove(queueName, safe);
	}

	public synchronized static void deleteQueue(String queueName)
	{
		QueueProcessorList.remove(queueName);
	}

	public static void remoteSessionClosed(Channel channel)
	{
		synchronized (instance.agentsConnection)
		{
			instance.agentsConnection.remove(channel);
		}
	}

	public static NetMessage buildNotification(NetPublish np, String subscriptionName)
	{
		String msg_id = MessageId.getMessageId();

		if (StringUtils.isBlank(np.getMessage().getMessageId()))
		{
			np.getMessage().setMessageId(msg_id);
		}

		long now = System.currentTimeMillis();
		if (np.getMessage().getTimestamp() == -1)
		{
			np.getMessage().setTimestamp(now);
		}

		if (np.getMessage().getExpiration() == -1)
		{
			String deliveryTime = np.getMessage().getHeaders().get(Headers.DEFERRED_DELIVERY);
			if (StringUtils.isBlank(deliveryTime))
			{
				np.getMessage().setExpiration(now + EXPIRATION_TIME);
			}
			else
			{
				try
				{
					long value = Long.parseLong(deliveryTime);
					np.getMessage().setExpiration(value + EXPIRATION_TIME);
				}
				catch (NumberFormatException nfe)
				{
					// This shouldn't happen because deliveryTime has been validated
					log.error(String.format("'EXPIRATION_TIME' is invalid '%s'", deliveryTime), nfe);
					throw new RuntimeException(nfe);
				}
			}

		}

		NetNotification notification = new NetNotification(np.getDestination(), np.getDestinationType(), np.getMessage(), subscriptionName);

		NetAction action = new NetAction(NetAction.ActionType.NOTIFICATION);
		action.setNotificationMessage(notification);

		NetMessage message = new NetMessage(action);

		if (np.getMessage().getHeaders() != null)
		{
			message.getHeaders().putAll(np.getMessage().getHeaders());
		}

		// if (np.getDestinationType() == DestinationType.TOPIC)
		// {
		// message.getHeaders().put("TYPE", "COM_TOPIC");
		// }
		// else if (np.getDestinationType() == DestinationType.QUEUE)
		// {
		// message.getHeaders().put("TYPE", "COM_QUEUE");
		// }

		return message;
	}

	public static void broadcastMaxQueueSizeReached()
	{
		broadcastMaxSizeFault(String.format("The maximum number of queues (%s) has been reached.", GcsInfo.getMaxQueues()));
	}

	public static void broadcastMaxDistinctSubscriptionsReached()
	{
		broadcastMaxSizeFault(String.format("The maximum number of distinct subscriptions (%s) has been reached.", GcsInfo.getMaxDistinctSubscriptions()));
	}

	private static void broadcastMaxSizeFault(String message)
	{
		String topic = String.format("/system/faults/#%s#", GcsInfo.getAgentName());

		// Soap fault message
		final String soapMessageTemplate = "<soap:Envelope xmlns:soap='http://www.w3.org/2003/05/soap-envelope' xmlns:wsa='http://www.w3.org/2005/08/addressing' xmlns:mq='http://services.sapo.pt/broker'><soap:Header><wsa:From><wsa:Address>%s</wsa:Address></wsa:From></soap:Header><soap:Body><soap:Fault><soap:Code><soap:Value>soap:Receiver</soap:Value></soap:Code><soap:Reason><soap:Text>%s</soap:Text></soap:Reason><soap:Detail>%s</soap:Detail></soap:Fault></soap:Body></soap:Envelope>";

		String faultMessage = String.format(soapMessageTemplate, GcsInfo.getAgentName(), "Limit reached", message);

		NetPublish np = new NetPublish(topic, DestinationType.TOPIC, new NetBrokerMessage(faultMessage));

		Gcs.publish(np);
	}
}