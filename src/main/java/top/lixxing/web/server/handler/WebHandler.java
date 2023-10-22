package top.lixxing.web.server.handler;

import com.sun.net.httpserver.HttpExchange;
import top.lixxing.web.server.config.Config;
import top.lixxing.web.server.response.HttpResponse;

import java.io.IOException;
import java.util.Properties;


public interface WebHandler {

	String BOUNDARY = "boundary=";

	Config config = Config.getInstance();
	Properties properties = config.getProperties();
	String url();
	HttpResponse doRequest(HttpExchange exchange) throws IOException;
}
