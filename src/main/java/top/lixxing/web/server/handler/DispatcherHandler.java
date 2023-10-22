package top.lixxing.web.server.handler;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.lixxing.web.server.config.Config;
import top.lixxing.web.server.response.HttpResponse;
import top.lixxing.web.server.utils.AntPathMatcher;
import top.lixxing.web.server.utils.ClassScanUtils;
import top.lixxing.web.server.utils.GzipUtils;
import top.lixxing.web.server.utils.PathMatcher;
import top.lixxing.web.server.utils.filter.AssignableScanFilter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public final class DispatcherHandler implements HttpHandler {

	private static final List<WebHandler> ALL_HANDLERS = new ArrayList<>();
	private static final Map<String, WebHandler> HANDLER_CACHE = new HashMap<>();

	private static final String SERVER_NAME = "SimpleHttpServer/1.0";

	private final Config config = Config.getInstance();
	private final Properties properties = config.getProperties();
	private final boolean gzip = Boolean.parseBoolean(properties.getProperty(Config.SERVER_GZIP, "false"));
	private final Logger logger = LoggerFactory.getLogger(DispatcherHandler.class);

	private final PathMatcher pathMatcher = new AntPathMatcher();

	static {
		try {
			List<Class<?>> classes = ClassScanUtils.scanAllWithFilter("top", new AssignableScanFilter(WebHandler.class));

			for (Class<?> aClass : classes) {
				if (aClass.isInterface()) {
					continue;
				}
				WebHandler baseWebHandler = (WebHandler) aClass.newInstance();
				ALL_HANDLERS.add(baseWebHandler);
			}
			ALL_HANDLERS.sort(PathMatcher.getHandlerComparator("/"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		HttpExchangeHolder.put(httpExchange);
		doAuth(httpExchange);
		String requestUrl = httpExchange.getRequestURI().getPath();
		WebHandler webHandler = HANDLER_CACHE.get(requestUrl);
		if (webHandler == null) {
			webHandler = ALL_HANDLERS.stream().filter(h -> pathMatcher.match(h.url(), requestUrl)).findFirst().orElse(new IndexHandler());
			HANDLER_CACHE.put(requestUrl, webHandler);
		}
		doResponse(webHandler.doRequest(httpExchange));
	}

	private void doAuth(HttpExchange exchange) {
		String authPath = properties.getProperty(Config.AUTH_PATH);
		String requestPath = exchange.getRequestURI().getPath();
		if (authPath == null || !pathMatcher.match(authPath, requestPath)) {
			return;
		}
		Headers requestHeaders = exchange.getRequestHeaders();
		String authHeader = properties.getProperty(Config.AUTH_HEADER, "x-token");
		String authToken = properties.getProperty(Config.AUTH_TOKEN, "123456");
		String requestToken = requestHeaders.getFirst(authHeader);
		if (requestToken == null || requestToken.length() == 0 || !authToken.equals(requestToken)) {
			doResponse(HttpResponse.UN_AUTHED);
		}
	}

	private void doResponse(HttpResponse response) {
		doResponse(response.getStatus(), response.getData(), response.getContentType());
	}

	private void doResponse(int status, String message, String contentType) {
		doResponse(status, message.getBytes(), contentType);
	}

	private void doResponse(int status, byte[] data, String contentType) {
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
			outputStream.flush();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			exchange.close();
		}
	}
}
