package pt.com.broker.client.sample;

import java.util.concurrent.atomic.AtomicInteger;

import org.caudexorigo.cli.CliFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.CliArgs;
import pt.com.broker.client.SslBrokerClient;
import pt.com.broker.client.messaging.BrokerListener;
import pt.com.common.security.ClientAuthInfo;
import pt.com.types.NetNotification;
import pt.com.types.NetProtocolType;
import pt.com.types.NetSubscribe;
import pt.com.types.NetAction.DestinationType;

public class AuthenticatedDBConsumer implements BrokerListener
{

	private static final Logger log = LoggerFactory.getLogger(AuthenticatedConsumer.class);
	private final AtomicInteger counter = new AtomicInteger(0);

	private String host;
	private int port;
	private DestinationType dtype;
	private String dname;

	private String stsLocation;
	private String username;
	private String password;
	
	private String keystoreLocation;
	private String keystorePassword;

	
	public static void main(String[] args) throws Throwable
	{
		final CliArgs cargs = CliFactory.parseArguments(CliArgs.class, args);

		AuthenticatedDBConsumer consumer = new AuthenticatedDBConsumer();

		consumer.host = cargs.getHost();
		consumer.port = cargs.getPort();
		consumer.dtype = DestinationType.valueOf(cargs.getDestinationType());
		consumer.dname = cargs.getDestination();

		consumer.username = cargs.getUsername();
		consumer.password = cargs.getUserPassword();
		
		consumer.keystoreLocation = cargs.getKeystoreLocation();
		consumer.keystorePassword = cargs.getKeystorePassword();


		
		SslBrokerClient bk = new SslBrokerClient(consumer.host, consumer.port, "tcp://mycompany.com/mysniffer", NetProtocolType.PROTOCOL_BUFFER, consumer.keystoreLocation, consumer.keystorePassword.toCharArray());

		ClientAuthInfo clientAuthInfo = new ClientAuthInfo(consumer.username, consumer.password);
		clientAuthInfo.setUserAuthenticationType("BrokerRolesDB");

		bk.setAuthenticationCredentials(clientAuthInfo);
		try
		{
			bk.authenticateClient();
		}
		catch (Throwable t)
		{
			System.out.println("Unable to authenticate client...");
			System.out.println(t);
		}

		// TODO: create callback object with time out
		System.out.println("giving time to authenticate (3s)...");
		Thread.sleep(3000);
		System.out.println("subscribing");
		NetSubscribe subscribe = new NetSubscribe(consumer.dname, consumer.dtype);

		bk.addAsyncConsumer(subscribe, consumer, null);

		System.out.println("listening...");
	}

	@Override
	public boolean isAutoAck()
	{
		if (dtype == DestinationType.TOPIC)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	@Override
	public void onMessage(NetNotification notification)
	{
		log.info(String.format("%s -> Received Message Length: %s (%s)", counter.incrementAndGet(), notification.getMessage().getPayload().length, new String(notification.getMessage().getPayload())));
	}
}
