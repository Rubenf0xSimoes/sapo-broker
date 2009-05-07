package pt.com.broker.client.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.caudexorigo.ErrorAnalyser;
import org.caudexorigo.concurrent.CustomExecutors;

import pt.com.broker.client.NetworkConnector;
import pt.com.broker.client.SslNetworkConnector;

public abstract class ProtocolHandler<T>
{
	private final AtomicBoolean _isReconnecting = new AtomicBoolean(false);

	private final ExecutorService exec = CustomExecutors.newThreadPool(4, "protocol-handler");

	private final ScheduledExecutorService shed_exec = CustomExecutors.newScheduledThreadPool(1, "sched-protocol-handler");

	protected AtomicBoolean closed = new AtomicBoolean(false);

	public abstract T decode(DataInputStream in) throws IOException;

	public abstract void encode(T message, DataOutputStream out) throws IOException;

	public abstract void onConnectionClose();

	public abstract void onConnectionOpen();

	public abstract void onError(Throwable error);

	protected abstract void onIOFailure(long connectionVersion);

	public abstract NetworkConnector getConnector();

	public abstract SslNetworkConnector getSslConnector();

	protected abstract void handleReceivedMessage(T request);

	private final Runnable reader = new Runnable()
	{
		public void run()
		{
			NetworkConnector connector = getConnector();
			DataInputStream in = connector.getInput();

			boolean continueReading = true;

			while (continueReading)
			{

				try
				{
					T message = doDecode(in);
					handleReceivedMessage(message);
				}
				catch (Throwable error)
				{
					final Throwable rootCause = ErrorAnalyser.findRootCause(error);
					if (rootCause instanceof IOException)
					{
						if (!connector.isClosed())
							onIOFailure(connector.getConnectionVersion());
						continueReading = false;
					}
					else
					{
						try
						{
							onError(rootCause);
						}
						catch (Throwable t)
						{
							// ignore
						}
					}

				}
			}
		}
	};

	private final Runnable sslReader = new Runnable()
	{
		public void run()
		{
			SslNetworkConnector connector = getSslConnector();
			DataInputStream in = connector.getInput();

			boolean continueReading = true;

			while (continueReading)
			{
				try
				{
					T message = doDecode(in);
					handleReceivedMessage(message);
				}
				catch (Throwable error)
				{
					final Throwable rootCause = ErrorAnalyser.findRootCause(error);
					if (rootCause instanceof IOException)
					{
						if (!connector.isClosed())
							onIOFailure(connector.getConnectionVersion());
						continueReading = false;
					}
					else
					{
						try
						{
							onError(rootCause);
						}
						catch (Throwable t)
						{
							// ignore
						}
					}

				}
			}

		}
	};

	private Throwable resetConnection(final NetworkConnector connector, Throwable error)
	{
		final Throwable rootCause = ErrorAnalyser.findRootCause(error);
		if (rootCause instanceof IOException)
		{
			onIOFailure(connector.getConnectionVersion());
		}
		return rootCause;
	}

	private Throwable resetSslConnection(final SslNetworkConnector connector, Throwable error)
	{
		final Throwable rootCause = ErrorAnalyser.findRootCause(error);
		if (rootCause instanceof IOException)
		{
			onIOFailure(connector.getConnectionVersion());
		}
		return rootCause;
	}

	private T doDecode(DataInputStream in) throws IOException
	{
		synchronized (in)
		{
			return decode(in);
		}
	}

	public void doEncode(T message, DataOutputStream out) throws IOException
	{
		synchronized (out)
		{
			encode(message, out);
		}
	}

	public void sendMessageOverSsl(final T message) throws Throwable
	{
		final SslNetworkConnector connector = getSslConnector();
		if (connector == null)
			throw new RuntimeException("SslNetworkConnector unavailable");
		try
		{
			DataOutputStream out = connector.getOutput();
			doEncode(message, out);
		}
		catch (Throwable error)
		{
			Throwable rootCause = resetSslConnection(connector, error);
			throw rootCause;
		}
	}

	public void sendMessage(final T message) throws Throwable
	{
		final NetworkConnector connector = getConnector();
		try
		{
			DataOutputStream out = connector.getOutput();
			doEncode(message, out);
		}
		catch (Throwable error)
		{
			Throwable rootCause = resetConnection(connector, error);
			throw rootCause;
		}
	}

	public final void start() throws Throwable
	{
		if (getSslConnector() != null)
			exec.execute(sslReader);

		exec.execute(reader);
	}

	public final void stop()
	{
		closed.set(true);

		getConnector().close();
		if (getSslConnector() != null)
			getSslConnector().close();

		try
		{
			exec.shutdown();
			shed_exec.shutdown();
		}
		catch (Throwable e)
		{
			// ignore
		}
	}
}
