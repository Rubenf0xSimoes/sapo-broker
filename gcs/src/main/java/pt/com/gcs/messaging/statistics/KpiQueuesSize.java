package pt.com.gcs.messaging.statistics;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.sapo.socialbus.common.kpi.data.Event;

public class KpiQueuesSize implements Runnable
{

	private static final Logger log = LoggerFactory.getLogger(KpiQueuesSize.class);

	@Override
	public void run()
	{
		try
		{
			List<Event> events = KpiStatistics.getQueuesSize();
			KpiStatistics.publishKpiEvents(events);
		}
		catch (Exception e)
		{
			log.error("Unexpected exception caught.", e);
		}
	}

}
