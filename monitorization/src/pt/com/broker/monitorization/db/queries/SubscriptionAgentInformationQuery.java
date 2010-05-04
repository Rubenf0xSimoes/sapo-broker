package pt.com.broker.monitorization.db.queries;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.caudexorigo.jdbc.Db;
import org.caudexorigo.jdbc.DbPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubscriptionAgentInformationQuery
{
	private static final Logger log = LoggerFactory.getLogger(SubscriptionAgentInformationQuery.class);

	private static final String QUERY = "SELECT  subscriptions.subject AS subscription , last_event_for_subject_predicate_agent(subscriptions.subject, 'subscriptions', ?, now(), '00:10') AS subscription_count FROM (SELECT DISTINCT subject FROM raw_data WHERE agent_name = ? and predicate = 'subscriptions' AND event_time > (now() - '00:10'::time)) AS subscriptions Order BY 1 DESC";

	private final static String AGENTNAME_PARAM = "agentname";

	public String getJsonData(Map<String, List<String>> params)
	{
		Db db = null;

		StringBuilder sb = new StringBuilder();

		try
		{
			db = DbPool.obtain();

			ResultSet queryResult = getResultSet(db, params);
			if (queryResult == null)
				return "";

			boolean first = true;

			while (queryResult.next())
			{
				if (first)
				{
					first = false;
				}
				else
				{
					sb.append(",");
				}
				int idx = 1;
				sb.append("{");
				sb.append("\"subscriptionName\":\"");
				sb.append(queryResult.getString(idx++));
				sb.append("\",");

				sb.append("\"subscriptions\":\"");
				sb.append(queryResult.getDouble(idx++));
				sb.append("\",");

				sb.append("}");
			}
		}
		catch (Throwable t)
		{
			log.error("Failed to get queue general info.", t);
		}
		finally
		{
			DbPool.release(db);
		}

		return sb.toString();
	}

	protected ResultSet getResultSet(Db db, Map<String, List<String>> params)
	{
		String agentName = getAgentName(params);
		if (agentName == null)
		{
			return null;
		}
		return db.runRetrievalPreparedStatement(QUERY, agentName, agentName);
	}

	public static String getAgentName(Map<String, List<String>> params)
	{
		List<String> list = params.get(AGENTNAME_PARAM);
		if ((list != null) && (list.size() == 1))
		{
			return list.get(0);
		}
		return null;
	}
}
