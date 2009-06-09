package pt.com.broker.client;

import org.caudexorigo.cli.Option;

/**
 *  Auxiliary interface that maps command line arguments.
 *
 */

public interface CliArgs
{
	/**
	 * 
	 *  @return Host name.
	 */
	@Option(shortName = "h", defaultValue = "localhost")
	String getHost();

	/**
	 * 
	 *  @return Host port.
	 */
	@Option(shortName = "p", defaultValue = "3323")
	int getPort();

	/**
	 * 
	 *  @return Destination name (e.g. "/test").
	 */
	@Option(shortName = "n", defaultValue = "/test")
	String getDestination();

	/**
	 * 
	 *  @return Destination Type (TOPIC, QUEUE, VIRTUAL_QUEUE).
	 */
	@Option(shortName = "d", defaultValue = "TOPIC")
	String getDestinationType();

	/**
	 * 
	 *  @return Keystore location.
	 */
	@Option(shortName = "L", defaultValue = ".")
	String getKeystoreLocation();

	/**
	 * 
	 *  @return Keystore password.
	 */
	@Option(shortName = "W", defaultValue = "")
	String getKeystorePassword();

	/**
	 * 
	 *  @return STS location (e.g. https://services.sapo.pt/STS/).
	 */
	@Option(shortName = "S", defaultValue = ".")
	String getSTSLocation();

	/**
	 * 
	 *  @return User name.
	 */
	@Option(shortName = "U", defaultValue = "")
	String getUsername();

	/**
	 * 
	 *  @return User password.
	 */
	@Option(shortName = "P", defaultValue = "")
	String getUserPassword();

	/**
	 * 
	 *  @return Message length. Useful for generate random messages with known size.
	 */
	@Option(shortName = "l", defaultValue = "1000")
	int getMessageLength();

}