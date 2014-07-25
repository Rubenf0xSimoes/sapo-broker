package pt.com.gcs.messaging;

import org.caudexorigo.ds.Cache;
import org.caudexorigo.ds.CacheFiller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

class PatternCache
{

	private static final PatternCache instance = new PatternCache();

	private static final Logger log = LoggerFactory.getLogger(PatternCache.class);

	// key: destinationName
	private Cache<String, Pattern> pCache = new Cache<String, Pattern>();

	private static final CacheFiller<String, Pattern> p_cf = new CacheFiller<String, Pattern>()
	{
		public Pattern populate(String regex)
		{
			try
			{
				log.debug("Populate PatternCache");
				Pattern p = Pattern.compile(regex);
				return p;
			}
			catch (Throwable e)
			{
				throw new RuntimeException(e);
			}
		}
	};

	private Pattern i_get(String regex)
	{
		try
		{
			return pCache.get(regex, p_cf);
		}
		catch (InterruptedException ie)
		{
			Thread.currentThread().interrupt();
			throw new RuntimeException(ie);
		}
	}

	protected static Pattern get(String regex)
	{
		return instance.i_get(regex);
	}

}
