package top.lixxing.web.server.handler;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.lixxing.web.server.config.Config;
import top.lixxing.web.server.response.HttpResponse;
import top.lixxing.web.server.utils.ClassScanUtils;
import top.lixxing.web.server.utils.GzipUtils;
import top.lixxing.web.server.utils.filter.AssignableScanFilter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class DispatcherHandler implements HttpHandler {

	private static final List<BaseWebHandler> handlers = new ArrayList<>();

	String SERVER_NAME = "SimpleHttpServer/1.0";
	String BOUNDARY = "boundary=";

	Config config = Config.getInstance();
	Properties properties = config.getProperties();
	boolean gzip = Boolean.valueOf(properties.getProperty(Config.SERVER_GZIP, "false"));
	Logger logger = LoggerFactory.getLogger(BaseWebHandler.class);

	static {
		try {
			List<Class<?>> classes = ClassScanUtils.scanAllWithFilter("", new AssignableScanFilter(BaseWebHandler.class));

			for (Class<?> aClass : classes) {
				if (aClass.isInterface()) {
					continue;
				}
				BaseWebHandler baseWebHandler = (BaseWebHandler) aClass.newInstance();
				handlers.add(baseWebHandler);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		HttpExchangeHolder.put(httpExchange);
		String requestUrl = httpExchange.getRequestURI().getPath();
		for (BaseWebHandler handler : handlers) {
			if (handler.url().equals(requestUrl)) {
				doResponse(handler.doRequest(httpExchange));
				return;
			}
		}
		doResponse(HttpResponse.NOT_FOUND);
	}

	public void doResponse(HttpResponse response) {
		doResponse(response.getStatus(), response.getData(), response.getContentType());
	}

	public void doResponse(int status, String message, String contentType) {
		doResponse(status, message.getBytes(), contentType);
	}

	public void doResponse(int status, byte[] data, String contentType) {
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
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			exchange.close();
		}
	}
}
