package top.lixxing.web.server.handler;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import top.lixxing.web.server.config.Config;
import top.lixxing.web.server.logger.LoggerFactory;
import top.lixxing.web.server.response.HttpResponse;
import top.lixxing.web.server.utils.GzipUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


public interface BaseWebHandler extends HttpHandler {

	String SERVER_NAME = "SimpleHttpServer/1.0";
	String BOUNDARY = "boundary=";

	Config config = Config.getInstance();
	Properties properties = config.getProperties();
	boolean gzip = Boolean.valueOf(properties.getProperty(Config.SERVER_GZIP, "false"));
	Logger logger = LoggerFactory.getLogger(BaseWebHandler.class);

	@Override
	default void handle(HttpExchange exchange) throws IOException {
		HttpExchangeHolder.remove();
		HttpExchangeHolder.put(exchange);
		HttpResponse response = doRequest(exchange);
		doResponse(response);
	}

	String url();

	HttpResponse doRequest(HttpExchange exchange) throws IOException;

	default void doResponse(HttpResponse response) {
		doResponse(response.getStatus(), response.getData(), response.getContentType());
	}

	default void doResponse(int status, String message, String contentType) {
		doResponse(status, message.getBytes(), contentType);
	}

	default void doResponse(int status, byte[] data, String contentType) {
		HttpExchange exchange = HttpExchangeHolder.get();
		Headers responseHeaders = exchange.getResponseHeaders();
		responseHeaders.putIfAbsent("server", Collections.singletonList(SERVER_NAME));
		responseHeaders.remove("Content-Length");
		responseHeaders.remove("Transfer-Encoding");
		responseHeaders.putIfAbsent("Connection", Collections.singletonList("keep-alive"));
		try(OutputStream outputStream = exchange.getResponseBody()) {
			responseHeaders.putIfAbsent("Content-Type", Collections.singletonList(contentType));
			if (gzip) {
				if (!responseHeaders.containsKey("Content-Encoding")) {
					responseHeaders.add("Content-Encoding", "gzip");
					data = GzipUtils.gzip(data);
				}
			}
			exchange.sendResponseHeaders(status, data.length);
			outputStream.write(data);
		} catch (IOException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			exchange.close();
		}
	}
}
