package top.lixxing.web.server;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Startup {

	private static final Logger logger = LoggerFactory.getLogger(Startup.class);

	public static void main(String[] args) {

		SimpleWebServer simpleWebServer = new SimpleWebServer();

		try {
			simpleWebServer.run();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}
