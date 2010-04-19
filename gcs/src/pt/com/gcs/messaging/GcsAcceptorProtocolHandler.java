package pt.com.gcs.messaging;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.caudexorigo.ErrorAnalyser;
import org.caudexorigo.text.StringUtils;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.ChannelAttributes;
import pt.com.broker.types.CriticalErrors;
import pt.com.broker.types.ListenerChannel;
import pt.com.broker.types.MessageListener;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.gcs.conf.GcsInfo;
import pt.com.gcs.conf.GlobalConfig;
import pt.com.gcs.messaging.GlobalConfigMonitor.GlobalConfigModifiedListener;
import pt.com.gcs.net.Peer;

/**
 * GcsAcceptorProtocolHandler is an NETTY SimpleChannelHandler. It handles remote subscription messages and acknowledges from other agents.
 */

@Sharable
class GcsAcceptorProtocolHandler extends SimpleChannelHandler
{
	private static Logger log = LoggerFactory.getLogger(GcsAcceptorProtocolHandler.class);
	private static final Charset UTF8 = Charset.forName("UTF-8");

	private static List<InetSocketAddress> peersAddressList;

	static
	{
		createPeersList();
		GlobalConfigMonitor.addGlobalConfigModifiedListener(new GlobalConfigModifiedListener()
		{
			@Override
			public void globalConfigModified()
			{
				globalConfigReloaded();
			}
		});
	}

	private static void createPeersList()
	{
		List<Peer> peerList = GlobalConfig.getPeerList();
		peersAddressList = new ArrayList<InetSocketAddress>(peerList.size());
		for (Peer peer : peerList)
		{
			InetSocketAddress addr = new InetSocketAddress(peer.getHost(), peer.getPort());
			peersAddressList.add(addr);
		}
	}

	public static void globalConfigReloaded()
	{
		createPeersList();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
	{
		Throwable rootCause = ErrorAnalyser.findRootCause(e.getCause());
		CriticalErrors.exitIfCritical(rootCause);
		log.error("Exception Caught:'{}', '{}'", ctx.getChannel().getRemoteAddress().toString(), rootCause.getMessage());
		if (ctx.getChannel().isWritable())
		{
			log.error("STACKTRACE", rootCause);
		}
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception
	{
		final NetMessage m = (NetMessage) e.getMessage();
		String mtype = m.getHeaders().get("TYPE");

		NetNotification nnot = m.getAction().getNotificationMessage();
		NetBrokerMessage brkMsg = nnot.getMessage();

		String msgContent = new String(brkMsg.getPayload(), "UTF-8");

		if (log.isDebugEnabled())
		{
			log.debug("Message Received from: '{}', Type: '{}'", ctx.getChannel().getRemoteAddress(), nnot.getDestination());
		}

		if (mtype.equals("ACK"))
		{
			Gcs.ackMessage(nnot.getDestination(), brkMsg.getMessageId());
			return;
		}
		else if (mtype.equals("HELLO"))
		{
			validatePeer(ctx, msgContent);
			boolean isValid = ((Boolean) ChannelAttributes.get(ChannelAttributes.getChannelId(ctx), "GcsAcceptorProtocolHandler.ISVALID")).booleanValue();
			if (!isValid)
			{
				String paddr = String.valueOf(ChannelAttributes.get(ChannelAttributes.getChannelId(ctx), "GcsAcceptorProtocolHandler.PEER_ADDRESS"));
				log.warn("A peer from \"{}\" tried to connect but it does not appear in the world map.", paddr);
				ctx.getChannel().close();
			}
			else
			{
				log.debug("Peer is valid!");
				return;
			}
			return;
		}
		else if (mtype.equals("SYSTEM_TOPIC") || mtype.equals("SYSTEM_QUEUE"))
		{

			final String action = extract(msgContent, "<action>", "</action>");
			final String src_name = extract(msgContent, "<source-name>", "</source-name>");

			final String subscriptionKey = extract(msgContent, "<destination>", "</destination>");

			if (StringUtils.isBlank(subscriptionKey))
			{
				String errorMessage = String.format("Sytem Queue or Topic message has a blank destination field. Message content: %s", msgContent);
				log.error("Sytem Queue or Topic message has a blank destination field");
				throw new RuntimeException(errorMessage);
			}

			if (log.isInfoEnabled())
			{
				String lmsg = String.format("Action: '%s' Consumer; Subscription: '%s'; Source: '%s'", action, subscriptionKey, src_name);
				log.info(lmsg);
			}

			if (mtype.equals("SYSTEM_TOPIC"))
			{
				MessageListener remoteListener = new RemoteListener(new ListenerChannel(ctx.getChannel()), subscriptionKey, DestinationType.TOPIC, DestinationType.TOPIC);

				TopicProcessor tp = TopicProcessorList.get(subscriptionKey);

				if (tp == null)
				{
					log.error("Failed to obtain a TopicProcessor instance for topic '{}'.", subscriptionKey);
					return;
				}

				if (action.equals("CREATE"))
				{
					tp.add(remoteListener, false);
				}
				else if (action.equals("DELETE"))
				{
					tp.remove(remoteListener);
				}

			}
			else if (mtype.equals("SYSTEM_QUEUE"))
			{
				MessageListener remoteListener = new RemoteListener(new ListenerChannel(ctx.getChannel()), subscriptionKey, DestinationType.QUEUE, DestinationType.QUEUE);

				QueueProcessor qp = QueueProcessorList.get(subscriptionKey);
				if (qp == null)
				{
					log.error("Failed to obtain a QueueProcessor instance for queue '{}'.", subscriptionKey);
					return;
				}

				if (action.equals("CREATE"))
				{
					qp.add(remoteListener);
				}
				else if (action.equals("DELETE"))
				{
					qp.remove(remoteListener);
				}
			}
			acknowledgeSystemMessage(brkMsg.getMessageId(), ctx);
		}
		else
		{
			log.warn("Unkwown message type. Don't know how to handle message");
		}
	}

	private void acknowledgeSystemMessage(String messageId, ChannelHandlerContext ctx)
	{
		Channel channel = ctx.getChannel();

		String ptemplate = "<sysmessage><action>%s</action><source-name>%s</source-name><source-ip>%s</source-ip><message-id>%s</message-id></sysmessage>";
		String payload = String.format(ptemplate, "SYSTEM_ACKNOWLEDGE", GcsInfo.getAgentName(), channel.getLocalAddress().toString(), messageId);

		NetBrokerMessage brkMsg = new NetBrokerMessage(payload.getBytes(UTF8));

		NetNotification notification = new NetNotification("/system/peer", DestinationType.TOPIC, brkMsg, "/system/peer");

		NetAction naction = new NetAction(NetAction.ActionType.NOTIFICATION);
		naction.setNotificationMessage(notification);

		NetMessage nmsg = new NetMessage(naction);
		nmsg.getHeaders().put("TYPE", "SYSTEM_ACK");

		channel.write(nmsg);
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
	{
		super.channelClosed(ctx, e);
		log.info("Session Closed: '{}'", ctx.getChannel().getRemoteAddress());

		TopicProcessorList.removeSession(ctx.getChannel());
		QueueProcessorList.removeSession(ctx.getChannel());

		ChannelAttributes.remove(ChannelAttributes.getChannelId(ctx));
	}

	private boolean validPeerAddress(ChannelHandlerContext ctx)
	{
		InetSocketAddress remotePeer = (InetSocketAddress) ctx.getChannel().getRemoteAddress();
		InetAddress address = remotePeer.getAddress();

		for (InetSocketAddress addr : peersAddressList)
		{
			if (address.equals(addr.getAddress()))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
	{
		super.channelConnected(ctx, e);
		log.info("Session Opened: '{}'", ctx.getChannel().getRemoteAddress());

		if (!validPeerAddress(ctx))
		{
			ctx.getChannel().close();
			log.warn("GCS: connection refused");
			return;
		}
		if (log.isDebugEnabled())
		{
			log.debug("Session Created: '{}'", ctx.getChannel().getRemoteAddress());
		}
	}

	private void validatePeer(ChannelHandlerContext ctx, String helloMessage)
	{
		log.debug("\"Hello\" message received: '{}'", helloMessage);
		try
		{
			String peerName = StringUtils.substringBefore(helloMessage, "@");
			String peerAddr = StringUtils.substringAfter(helloMessage, "@");
			String peerHost = StringUtils.substringBefore(peerAddr, ":");
			int peerPort = Integer.parseInt(StringUtils.substringAfter(peerAddr, ":"));

			ChannelAttributes.set(ChannelAttributes.getChannelId(ctx), "GcsAcceptorProtocolHandler.PEER_ADDRESS", peerAddr);

			Peer peer = new Peer(peerName, peerHost, peerPort);
			if (Gcs.getPeerList().contains(peer))
			{
				log.debug("Peer '{}' exists in the world map'", peer.toString());
				ChannelAttributes.set(ChannelAttributes.getChannelId(ctx), "GcsAcceptorProtocolHandler.ISVALID", true);
				return;
			}
		}
		catch (Throwable t)
		{
			ChannelAttributes.set(ChannelAttributes.getChannelId(ctx), "GcsAcceptorProtocolHandler.PEER_ADDRESS", "Unknown address");

			log.error(t.getMessage(), t);
		}
		ChannelAttributes.set(ChannelAttributes.getChannelId(ctx), "GcsAcceptorProtocolHandler.ISVALID", false);
	}

	private String extract(String ins, String prefix, String sufix)
	{
		if (StringUtils.isBlank(ins))
		{
			return "";
		}

		int s = ins.indexOf(prefix) + prefix.length();
		int e = ins.indexOf(sufix);
		return ins.substring(s, e);
	}
}
