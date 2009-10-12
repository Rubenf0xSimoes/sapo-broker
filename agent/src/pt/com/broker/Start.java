package pt.com.broker;

import org.apache.mina.util.ExceptionMonitor;
import org.caudexorigo.Shutdown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.auth.ProvidersLoader;
import pt.com.broker.core.BrokerInfo;
import pt.com.broker.core.BrokerSSLServer;
import pt.com.broker.core.BrokerServer;
import pt.com.broker.core.BrokerUdpServer;
import pt.com.broker.core.ErrorHandler;
import pt.com.broker.core.FilePublisher;
import pt.com.broker.http.BrokerHttpService;
import pt.com.gcs.conf.GcsInfo;
import pt.com.gcs.messaging.Gcs;

/**
 * Main class for Sapo-Broker agents.
 * 
 */

public class Start
{
	private static final Logger log = LoggerFactory.getLogger(Start.class);

	public static void main(String[] args) throws Exception
	{
		start();
	}

	public static void start()
	{
		System.setProperty("file.encoding", "UTF-8");

		try
		{
			// Verify if the Aalto parser is in the classpath
			Class.forName("com.fasterxml.aalto.stax.InputFactoryImpl").newInstance();
			Class.forName("com.fasterxml.aalto.stax.OutputFactoryImpl").newInstance();
			Class.forName("com.fasterxml.aalto.stax.EventFactoryImpl").newInstance();

			// If we made it here without errors set Aalto as our StaX parser
			System.setProperty("javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
			System.setProperty("javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
			System.setProperty("javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");
		}
		catch (Throwable t)
		{
			log.warn("Aalto was not found in the classpath, will fallback to use the native parser");
		}

		ErrorHandler errorHandler = new ErrorHandler();
		
		ExceptionMonitor.setInstance(errorHandler);

		try
		{
			log.info("SAPO-BROKER starting - Version: {}", BrokerInfo.VERSION);

			Gcs.init();
			ProvidersLoader.init();

			int broker_port = GcsInfo.getBrokerPort();
			int broker_legacy_port = GcsInfo.getBrokerLegacyPort();
			BrokerServer broker_srv = new BrokerServer(broker_port, broker_legacy_port);
			broker_srv.start();

			int http_port = GcsInfo.getBrokerHttpPort();
			BrokerHttpService http_srv = new BrokerHttpService(http_port);
			http_srv.start();

			if (GcsInfo.createSSLInterface())
			{
				int ssl_port = GcsInfo.getBrokerSSLPort();
				BrokerSSLServer ssl_svr = new BrokerSSLServer(ssl_port);
				ssl_svr.start();
			}

			int udp_legacy_port = GcsInfo.getBrokerUdpPort();
			int udp_bin_port = broker_port;
			BrokerUdpServer udp_srv = new BrokerUdpServer(udp_legacy_port, udp_bin_port);
			udp_srv.start();

			FilePublisher.init();

			Thread sync_hook = new Thread()
			{
				public void run()
				{
					try
					{
						log.info("Disconnect broker socket acceptor");
						Gcs.destroy();
						log.info("Shutdown hook thread ended!");
					}
					catch (Throwable te)
					{
						log.error(te.getMessage(), te);
					}
				}
			};

			Runtime.getRuntime().addShutdownHook(sync_hook);

			// BrokerExecutor.execute(udp_srv_runner);

		}
		catch (Throwable e)
		{
			log.error(e.getMessage(), e);
			Shutdown.now();
		}

	}
}
