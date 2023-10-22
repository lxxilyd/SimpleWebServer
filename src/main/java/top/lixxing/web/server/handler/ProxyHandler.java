package top.lixxing.web.server.handler;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.lixxing.web.server.config.Config;
import top.lixxing.web.server.response.HttpResponse;
import top.lixxing.web.server.utils.HttpRequester;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class ProxyHandler implements WebHandler {

    private final String proxyUrl = properties.getProperty(Config.PROXY_URL, "/proxy");
    private final String proxyTarget = properties.getProperty(Config.PROXY_TARGET, "http://localhost");
    private final boolean removeUrl = Boolean.valueOf(properties.getProperty(Config.PROXY_REMOVE_URL, "false"));
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String url() {
        return proxyUrl;
    }

    @Override
    public HttpResponse doRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String targetUrl = proxyTarget + (removeUrl ? path.replace(proxyUrl, "") : path);
        return doProxy(targetUrl, exchange.getRequestMethod());
    }

    private HttpResponse doProxy(String targetUrl, String method) {
        HttpExchange exchange = HttpExchangeHolder.get();
        Headers requestHeaders = exchange.getRequestHeaders();
        InputStream requestBody = exchange.getRequestBody();
        Headers responseHeaders = exchange.getResponseHeaders();


        HttpRequester requester = HttpRequester.request(targetUrl, method);
        requestHeaders.forEach((k, v) -> {
            for (String value : v) {
                requester.header(k, value);
            }
        });

        try {
            HttpRequester.Response response = requester.requestInputStream(requestBody).response();
            byte[] data = response.getData();
            Map<String, List<String>> headers = response.getHeaders();
            headers.forEach((k, v) -> {
                if (k != null && !"null".equals(k)) {
                    responseHeaders.putIfAbsent(k, v);
                }
            });
            return new HttpResponse(response.getCode(), data, headers.get("Content-Type").get(0));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return HttpResponse.BAD_GATEWAY;
        }
    }
}
