package pt.com.gcs.messaging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.caudexorigo.ErrorAnalyser;
import org.caudexorigo.ds.Cache;
import org.caudexorigo.ds.CacheFiller;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.CriticalErrors;
import pt.com.broker.types.ListenerChannel;
import pt.com.broker.types.MessageListener;
import pt.com.gcs.conf.GcsInfo;

/**
 * QueueProcessorList contains references for all active QueueProcessor objects.
 */

public class QueueProcessorList
{

	private static final Logger log = LoggerFactory.getLogger(QueueProcessorList.class);

	private static final QueueProcessorList instance = new QueueProcessorList();

	public static class MaximumQueuesAllowedReachedException extends RuntimeException
	{
	}

	private static final CacheFiller<String, QueueProcessor> qp_cf = new CacheFiller<String, QueueProcessor>()
	{
		public QueueProcessor populate(String destinationName)
		{
			try
			{
				System.out.println("GcsInfo.getMaxQueues(): " + GcsInfo.getMaxQueues());
				System.out.println("instance.qpCache.size(): " + instance.qpCache.size());
				
				if (instance.qpCache.size() > GcsInfo.getMaxQueues())
				{
					throw new MaximumQueuesAllowedReachedException();
				}
				log.info("Populate QueueProcessorList with queue: '{}'", destinationName);
				QueueProcessor qp = new QueueProcessor(destinationName);
				return qp;
			}
			catch (Throwable e)
			{
				throw new RuntimeException(e);
			}
		}
	};

	public static void broadcast(final String action, final Channel channel)
	{
		instance.i_broadcast(action, channel);
	}

	public static QueueProcessor get(String destinationName) throws MaximumQueuesAllowedReachedException
	{
		return instance.i_get(destinationName);
	}

	protected static void remove(String queueName)
	{
		instance.i_remove(queueName);
	}

	public static void removeListener(MessageListener listener)
	{
		instance.i_removeListener(listener);
	}

	public static void removeSession(Channel channel)
	{
		instance.i_removeSession(channel);
	}

	public static Collection<QueueProcessor> values()
	{
		return instance.i_values();
	}

	// key: destinationName
	private Cache<String, QueueProcessor> qpCache = new Cache<String, QueueProcessor>();

	private QueueProcessorList()
	{
	}

	private void i_broadcast(final String action, final Channel channel)
	{
		try
		{
			for (QueueProcessor qp : qpCache.values())
			{
				if (qp.localListeners().size() > 0)
				{
					qp.broadCastQueueInfo(action, channel);
				}
			}
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
	}

	private QueueProcessor i_get(String destinationName) throws MaximumQueuesAllowedReachedException
	{
		log.debug("Get Queue for: {}", destinationName);

		try
		{
			return qpCache.get(destinationName, qp_cf);
		}
		catch (InterruptedException ie)
		{
			Thread.currentThread().interrupt();
			throw new RuntimeException(ie);
		}
		catch (Throwable t)
		{
			log.error(String.format("Failed to get QueueProcessor for queue '%s'. Message: %s", destinationName, t.getMessage()), t);

			Throwable rootCause = ErrorAnalyser.findRootCause(t);
			CriticalErrors.exitIfCritical(rootCause);
			if (rootCause instanceof MaximumQueuesAllowedReachedException)
			{
				throw (MaximumQueuesAllowedReachedException) rootCause;
			}
		}
		return null;
	}

	private synchronized void i_remove(String queueName)
	{
		try
		{

			if (!qpCache.containsKey(queueName))
			{
				throw new IllegalArgumentException(String.format("Queue named '%s' doesn't exist.", queueName));
			}

			QueueProcessor qp;
			try
			{
				qp = get(queueName);
			}
			catch (MaximumQueuesAllowedReachedException e)
			{
				// This should never happens
				log.error("Trying to remove an inexistent queue.");
				Gcs.broadcastMaxQueueSizeReached();
				return;
			}

			if (qp.hasRecipient())
			{
				String m = String.format("Queue '%s' has active consumers.", queueName);
				throw new IllegalStateException(m);
			}

			qpCache.remove(queueName);
			qp.clearStorage();

			log.info("Destination '{}' was deleted", queueName);

		}
		catch (InterruptedException ie)
		{
			Thread.currentThread().interrupt();
			throw new RuntimeException(ie);
		}
	}

	private void i_removeListener(MessageListener listener)
	{
		try
		{
			for (QueueProcessor qp : qpCache.values())
			{
				if (qp.getQueueName().equals(listener.getsubscriptionKey()))
				{
					qp.remove(listener);
					break;
				}
			}
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
	}

	private void i_removeSession(Channel channel)
	{
		try
		{
			ListenerChannel lc = new ListenerChannel(channel);
			List<QueueProcessor> toDeleteProcessors = new ArrayList<QueueProcessor>();

			for (QueueProcessor qp : qpCache.values())
			{
				List<MessageListener> toDelete = new ArrayList<MessageListener>();

				for (MessageListener ml : qp.localListeners())
				{
					if (ml.getChannel().equals(lc))
					{
						toDelete.add(ml);
					}
				}

				for (MessageListener ml : qp.remoteListeners())
				{
					if (ml.getChannel().equals(lc))
					{
						toDelete.add(ml);
					}
				}

				for (MessageListener dml : toDelete)
				{
					qp.remove(dml);
				}

				toDelete.clear();

				if (qp.size() == 0 && qp.getQueuedMessagesCount() == 0)
				{
					toDeleteProcessors.add(qp);
				}
			}

			for (QueueProcessor qpd : toDeleteProcessors)
			{
				log.info("Remove QueueProcessor for '{}' because it has no consumers and no messages", qpd.getQueueName());
				qpCache.removeValue(qpd);
			}
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
		catch (Throwable t)
		{
			throw new RuntimeException(t);
		}
	}

	private Collection<QueueProcessor> i_values()
	{
		try
		{
			return qpCache.values();
		}
		catch (InterruptedException ie)
		{
			Thread.currentThread().interrupt();
			throw new RuntimeException(ie);
		}
	}
}
