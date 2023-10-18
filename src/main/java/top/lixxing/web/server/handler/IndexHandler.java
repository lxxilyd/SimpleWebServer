package top.lixxing.web.server.handler;

import com.sun.net.httpserver.HttpExchange;
import top.lixxing.web.server.config.Config;
import top.lixxing.web.server.response.HttpResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class IndexHandler implements BaseWebHandler {

	private final String webPath = properties.getProperty(Config.WEB_PATH, "web");
	private final String webIndex = properties.getProperty(Config.WEB_INDEX, "index.html");

	@Override
	public String url() {
		return "/";
	}

	@Override
	public HttpResponse doRequest(HttpExchange exchange) throws IOException {
		URI requestURI = exchange.getRequestURI();
		String filename = webPath + requestURI.getPath();
		if (requestURI.getPath().equals("/")){
			filename += webIndex;
		}
		return readFile(filename);
	}

	private HttpResponse readFile(String filename) throws IOException {
		File file = new File(filename);
		if (!file.exists()) {
			return HttpResponse.NOT_FOUND;
		}
		String contentType = config.getContentType(file);
		InputStream inputStream = new FileInputStream(file);
		int available = inputStream.available();
		byte[] data = new byte[available];
		inputStream.read(data);
		return HttpResponse.ok(data, contentType);
	}
}
