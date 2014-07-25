package pt.com.broker.jsbridge;

import org.caudexorigo.http.netty.HttpAction;
import org.caudexorigo.http.netty.RequestRouter;
import org.caudexorigo.http.netty.StaticFileAction;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpRequest;

import java.net.URI;

public class JSBridgeRouter implements RequestRouter
{

	private final HttpAction static_file;

	public JSBridgeRouter(URI root_uri)
	{
		super();
		static_file = new StaticFileAction(root_uri);
	}

        /* TODO TEMP CHANGE brsantos */
	//@Override
	public HttpAction map(HttpRequest req)
	{
		return static_file;
	}
        
        /* TODO TEMP CHANGE brsantos */
        //@Override
        public HttpAction map(ChannelHandlerContext chc, HttpRequest hr) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

}