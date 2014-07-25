package pt.com.gcs.messaging;

import org.caudexorigo.Shutdown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DestinationMatcher uses regular expressions to determine if a given subscription matches a given topic name.
 * 
 */

public class DestinationMatcher
{
	private static Logger log = LoggerFactory.getLogger(DestinationMatcher.class);

	public static boolean match(String subscriptionName, String topicName)
	{
		try
		{
			Pattern p = PatternCache.get(subscriptionName);
			Matcher m = p.matcher(topicName);
			return m.matches();
		}
		catch (Throwable t)
		{
			// String message = String.format("match-> subscriptionName: '%s'; topicName: '%s'", subscriptionName, topicName)
			Shutdown.now(t); // TODO: Something very wrong happens when this method fails, for now just exit. A proper fix should be done ASAP!
			return false;
		}
	}
}