package top.lixxing.web.server.handler;

import com.sun.net.httpserver.HttpExchange;

public class HttpExchangeHolder {

	private static final ThreadLocal<HttpExchange> local = new ThreadLocal<>();

	public static HttpExchange get() {
		return local.get();
	}

	public static void put(HttpExchange exchange) {
		local.set(exchange);
	}

	public static void remove() {
		local.remove();
	}
}
