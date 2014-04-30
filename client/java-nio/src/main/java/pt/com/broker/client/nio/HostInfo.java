package pt.com.broker.client.nio;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.net.InetSocketAddress;

/**
 * Immutable class that represents an Agent host.
 */

public final class HostInfo
{
    public static final int DEFAULT_CONNECT_TIMEOUT = 15 * 1000; // 15 seconds
    public static final int DEFAULT_READ_TIMEOUT = 0; // forever
    public static final int DEFAULT_RECONNECT_LIMIT = 10; // 10 tentativas

    private String hostname;
    private int port;
    private final int udpPort;
    private final int connectTimeout;
    private final int readTimeout;

    private int reconnectLimit = DEFAULT_RECONNECT_LIMIT;

    private ChannelFuture channelFuture;

    /**
     * Creates a HostInfo instance.
     *
     * @param hostname
     *            The name of the host (e.g. broker.localdomain.company.com or 10.12.10.120).
     * @param port
     *            Connection port.
     */
    public HostInfo(String hostname, int port)
    {
        this(hostname, port, -1, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    /**
     * Creates a HostInfo instance.
     *
     * @param hostname
     *            The name of the host (e.g. broker.localdomain.company.com or 10.12.10.120).
     * @param port
     *            Connection port.
     * @param udpPort
     *            UDP port
     */
    public HostInfo(String hostname, int port, int udpPort)
    {
        this(hostname, port, udpPort, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    /**
     * Creates a HostInfo instance.
     *
     * @param hostname
     *            The name of the host (e.g. broker.localdomain.company.com or 10.12.10.120).
     * @param port
     *            Connection port.
     * @param udpPort
     *            UDP port
     * @param connectTimeout
     *            Connection Timeout
     * @param readTimeout
     *            Read Timeout
     */
    public HostInfo(String hostname, int port, int udpPort, int connectTimeout, int readTimeout)
    {
        this.hostname = hostname;
        this.port = port;
        this.udpPort = udpPort;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    public String getHostname()
    {
        return hostname;
    }

    public int getPort()
    {
        return port;
    }

    public int getUdpPort()
    {
        return udpPort;
    }

    public int getConnectTimeout()
    {
        return connectTimeout;
    }

    public int getReadTimeout()
    {
        return readTimeout;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!obj.getClass().equals(this.getClass()))
            return false;
        HostInfo other = (HostInfo) obj;
        if (!hostname.equals(other.hostname))
            return false;
        if (port != other.port)
            return false;
        if (udpPort != other.udpPort)
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return String.format("HostInfo [hostname=%s, port=%s, udpPort=%s, connectTimeout=%s, readTimeout=%s]", hostname, port, udpPort, connectTimeout, readTimeout);
    }


    public InetSocketAddress getSocketAddress(){
        InetSocketAddress socketAddress = new InetSocketAddress(getHostname(),getPort());

        return socketAddress;
    }

    public ChannelFuture getChannelFuture() {
        return channelFuture;
    }

    public void setChannelFuture(ChannelFuture channelFuture) {
        this.channelFuture = channelFuture;
    }

    public boolean isActive(){

        return this.getChannelFuture() != null && (getChannel().isActive() ||  getChannel().isOpen() );
    }

    public Channel getChannel(){
        return this.getChannelFuture().channel();
    }

    public synchronized int getReconnectLimit() {
        return reconnectLimit;
    }

    public  synchronized void setReconnectLimit(int reconnectLimit) {
        this.reconnectLimit = reconnectLimit;
    }

    public  synchronized void resetReconnectLimit(){
        setReconnectLimit(DEFAULT_RECONNECT_LIMIT);
    }

    public synchronized void reconnectAttempt(){
          reconnectLimit--;
    }
}