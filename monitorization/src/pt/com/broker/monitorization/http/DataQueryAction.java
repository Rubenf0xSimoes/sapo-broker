package pt.com.broker.monitorization.http;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.caudexorigo.http.netty.HttpAction;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.monitorization.db.FaultsDB;
import pt.com.broker.monitorization.db.StaticQueries;
import pt.com.broker.monitorization.db.StatisticsDB;
import pt.com.broker.monitorization.db.FaultsDB.FaultItem;
import pt.com.broker.monitorization.db.FaultsDB.GroupItem;
import pt.com.broker.monitorization.db.StatisticsDB.StatisticsItem;
import pt.com.broker.monitorization.db.queries.AgentInformationRouter;
import pt.com.broker.monitorization.db.queries.FaultsQueryGenerator;
import pt.com.broker.monitorization.db.queries.IntervalQuery;
import pt.com.broker.monitorization.db.queries.LastResultQuery;
import pt.com.broker.monitorization.db.queries.QueryGenerator;
import pt.com.broker.monitorization.db.queries.QueueInformationRouter;

public class DataQueryAction extends HttpAction
{

	private static final Logger log = LoggerFactory.getLogger(DataQueryAction.class);

	private final String pathPrefix;

	private final static String REGULAR_QUERY = "query";
	private final static String LAST_VALUE_QUERY = "last";
	private final static String FAULT_QUERY = "fault";
	private final static String GROUPT_FAULT_QUERY = "groupfault";

	private final static String STATIC_QUEURY = "static";
	private final static String QUEUE_QUERY = "queue";
	private final static String AGENT_QUERY = "agent";
	
	public DataQueryAction(String queryPrefix)
	{
		pathPrefix = queryPrefix;
	}

	public void writeResponse(ChannelHandlerContext context, HttpRequest request, HttpResponse response)
	{
		String path = request.getUri();

		path = path.substring(pathPrefix.length()).toLowerCase();

		Map<String,List<String>> params = getParams(request);
		
		int index = path.indexOf('?');
		
		String queryType = (index > 0) ? path.substring(0, index) : path;
		
		QueryGenerator qr = null;
		String sqlQuery = null;
		if(queryType.equals(REGULAR_QUERY))
		{
			qr = IntervalQuery.getInstance(params);
			sqlQuery = qr.getSqlQuery();
		}
		else if (queryType.equals(LAST_VALUE_QUERY))
		{
			qr = LastResultQuery.getInstance(params);
			sqlQuery = qr.getSqlQuery();
		}
		else if (queryType.equals(FAULT_QUERY) || queryType.equals(GROUPT_FAULT_QUERY) )
		{
			qr = FaultsQueryGenerator.getInstance(params);
			sqlQuery = qr.getSqlQuery();
		}
		else if(queryType.equals (STATIC_QUEURY) || queryType.equals (QUEUE_QUERY) || queryType.equals (AGENT_QUERY) )
		{
			
		}
		else
		{
			throw new IllegalArgumentException(String.format("Use /%s?... or /%s?...", REGULAR_QUERY, LAST_VALUE_QUERY));
		}
				
		if(log.isDebugEnabled())
		{
			log.debug(String.format("URI: %s\nSQL: %s", path, sqlQuery ));
		}
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("[");
		
		
		if (queryType.equals(FAULT_QUERY))
		{
			List<FaultItem> items = FaultsDB.getItems(sqlQuery);
			if(items.size() > 0)
			{
				sb.append(items.get(0).toJson());
			}
			for(int i = 1; i < items.size(); ++i )
			{
				sb.append(", ");
				sb.append(items.get(i).toJson());
			}
		}
		else if (queryType.equals(GROUPT_FAULT_QUERY))
		{
			List<GroupItem> items = FaultsDB.getGroupedItems(sqlQuery);
			if(items.size() > 0)
			{
				sb.append(items.get(0).toJson());
			}
			for(int i = 1; i < items.size(); ++i )
			{
				sb.append(", ");
				sb.append(items.get(i).toJson());
			}
		}
		else if(queryType.equals(STATIC_QUEURY))
		{
			String queuryId = path.substring(index + 1);
			sb.append(StaticQueries.getData(queuryId, params));
		}
		else if(queryType.equals (QUEUE_QUERY))
		{
			sb.append(QueueInformationRouter.getQueueData(params));
		}
		else if(queryType.equals (AGENT_QUERY))
		{
			sb.append(AgentInformationRouter.getAgentData(params));
		}
		else
		{

			List<StatisticsItem> items = StatisticsDB.getItems(sqlQuery);
			if(items.size() > 0)
			{
				sb.append(items.get(0).toJson());
			}
			for(int i = 1; i < items.size(); ++i )
			{
				sb.append(", ");
				sb.append(items.get(i).toJson());
			}
		}
		
		sb.append("]");
		
		ChannelBuffer buffer = ChannelBuffers.copiedBuffer(ChannelBuffers.BIG_ENDIAN, sb.toString(), Charset.forName("utf-8"));

		response.addHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json");
		response.addHeader(HttpHeaders.Names.CONTENT_ENCODING, "UTF-8");
		response.addHeader(HttpHeaders.Names.CONTENT_LENGTH, buffer.writerIndex());

		response.setContent(buffer);
	}

	private static Map<String, List<String>> getParams(HttpRequest request)
	{
		return new QueryStringDecoder(request.getUri()).getParameters();
	}
}
