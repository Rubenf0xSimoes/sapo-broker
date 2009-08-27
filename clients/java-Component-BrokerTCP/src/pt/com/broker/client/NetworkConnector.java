package pt.com.broker.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NetworkConnector is an implementation of BaseNetworkConnector for plain TCP connections.
 * 
 */
public class NetworkConnector extends BaseNetworkConnector
{
	private static final Logger log = LoggerFactory.getLogger(NetworkConnector.class);

	public NetworkConnector(HostInfo hostInfo)
	{
		super(hostInfo);
	}

	public synchronized void connect(HostInfo host, long connectionVersion) throws Throwable
	{
		log.warn("Trying to connect");
		this.setConnectionVersion(connectionVersion);
		this.hostInfo = host;
		client = new Socket();
		client.connect(new InetSocketAddress(host.getHostname(), host.getPort()), 15 * 1000);
		rawOutput = new DataOutputStream(getSocket().getOutputStream());
		rawInput = new DataInputStream(getSocket().getInputStream());
		socketAddress = getSocket().getRemoteSocketAddress();
		socketAddressLiteral = socketAddress.toString();
		log.info("Connection established: " + socketAddressLiteral);
		closed = false;
	}
}
