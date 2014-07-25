package pt.com.broker.monitorization.db;

import org.caudexorigo.ErrorAnalyser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/* TODO TEMP CHANGE brsantos */
//import org.caudexorigo.jdbc.DbExecutor;

public class StatisticsDB
{
	private static Logger log = LoggerFactory.getLogger(StatisticsDB.class);

	/*
	 * CREATE TABLE IF NOT EXISTS statistics(agentname VARCHAR(255) NOT NULL, time TIMESTAMP NOT NULL, subject VARCHAR(256) NOT NULL, predicate VARCHAR(255) NOT NULL, value DOUBLE NOT NULL);
	 */

	public static void add(String agent, Date sampleDate, String subject, String predicate, double value)
	{
		if (log.isDebugEnabled())
		{
			log.debug(String.format("StatisticsCollector.processItem(%s, %s, %s, %s, %s)", agent, sampleDate, subject, predicate, value));
		}

		try
		{
			/* TODO TEMP CHANGE brsantos */
//			DbExecutor.runActionPreparedStatement("INSERT INTO raw_data (agent_name, event_time, subject, predicate, object_value) VALUES (?, ?, ?, ?, ?)", agent, sampleDate, subject, predicate, value);
		}
		catch (Throwable t)
		{
			Throwable r = ErrorAnalyser.findRootCause(t);
			log.error("Failed to insert new information item.", r);
		}
	}
}
