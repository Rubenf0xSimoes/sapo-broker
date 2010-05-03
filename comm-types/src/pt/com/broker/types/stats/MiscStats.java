package pt.com.broker.types.stats;

import java.util.concurrent.atomic.AtomicLong;

public class MiscStats
{
	// TCP connections
	private static final AtomicLong tcpConnections = new AtomicLong(0);

	public static void newTcpConnection()
	{
		tcpConnections.incrementAndGet();
	}

	public static void tcpConnectionClosed()
	{
		tcpConnections.decrementAndGet();
	}

	public static long getTcpConnectionsAndReset()
	{
		return tcpConnections.getAndSet(0);
	}

	// TCP legacy connections
	private static final AtomicLong tcpLegacyConnections = new AtomicLong(0);

	public static void newTcpLegacyConnection()
	{
		tcpLegacyConnections.incrementAndGet();
	}

	public static void tcpLegacyConnectionClosed()
	{
		tcpLegacyConnections.decrementAndGet();
	}

	public static long getTcpLegacyConnectionsAndReset()
	{
		return tcpLegacyConnections.getAndSet(0);
	}

	// SSL connections
	private static final AtomicLong sslConnections = new AtomicLong(0);

	public static void newSslConnection()
	{
		sslConnections.incrementAndGet();
	}

	public static void sslConnectionClosed()
	{
		sslConnections.decrementAndGet();
	}

	public static long getSslConnectionsAndReset()
	{
		return sslConnections.getAndSet(0);
	}

	// Access denied
	private static final AtomicLong accessesDenied = new AtomicLong(0);

	public static void newAccessDenied()
	{
		accessesDenied.incrementAndGet();
	}

	public static long getAccessesDeniedAndReset()
	{
		return accessesDenied.getAndSet(0);
	}

	// Invalid messages
	private static final AtomicLong invalidMessages = new AtomicLong(0);

	public static void newInvalidMessage()
	{
		invalidMessages.incrementAndGet();
	}

	public static long getInvalidMessagesAndReset()
	{
		return accessesDenied.getAndSet(0);
	}

	// Failed system message failed
	private static final AtomicLong sysMessagesFailed = new AtomicLong(0);

	public static void newSystemMessageFailed()
	{
		sysMessagesFailed.incrementAndGet();
	}

	public static long getSystemMessagesFailuresAndReset()
	{
		return sysMessagesFailed.getAndSet(0);
	}

	// Fault
	private static final AtomicLong faults = new AtomicLong(0);

	public static void newFault()
	{
		faults.incrementAndGet();
	}

	public static long getFaultsAndReset()
	{
		return faults.getAndSet(0);
	}

}