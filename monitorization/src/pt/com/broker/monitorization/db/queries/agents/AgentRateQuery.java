package pt.com.broker.monitorization.db.queries.agents;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.caudexorigo.jdbc.Db;

import pt.com.broker.monitorization.db.queries.StaticQuery;
import pt.com.broker.monitorization.http.QueryStringParameters;

public abstract class AgentRateQuery extends StaticQuery
{

	private final String QUERY_ALL;
	private final String QUERY_LAST;

	public AgentRateQuery(String queryAll, String queryLast)
	{
		super();
		QUERY_ALL = queryAll;
		QUERY_LAST = queryLast;
	}

	public ResultSet getResultSet(Db db, Map<String,List<String>> params)
	{
		String windowParam = QueryStringParameters.getWindowParam(params);
		
		String agentName = QueryStringParameters.getAgentNameParam(params);
		if(agentName == null)
		{
			return null;
		}
		
		if(windowParam != null)
		{
			if(windowParam.equals(QueryStringParameters.WINDOW_PARAM_ALL))
			{
				return db.runRetrievalPreparedStatement(QUERY_ALL, agentName);
			}
			else if(windowParam.equals(QueryStringParameters.WINDOW_PARAM_LAST))
			{
				return db.runRetrievalPreparedStatement(QUERY_LAST, agentName);
			}
			return null;
		}
		return db.runRetrievalPreparedStatement(QUERY_ALL, agentName);
	}
}