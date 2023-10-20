package top.lixxing.web.server;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.lixxing.web.server.config.bean.Server;

import java.io.IOException;
import java.io.InputStream;

public class Startup {

	private static final Logger logger = LoggerFactory.getLogger(Startup.class);

	public static void main(String[] args) throws IOException {

		SimpleWebServer simpleWebServer = new SimpleWebServer();

		try {
			simpleWebServer.run();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		InputStream inputStream = Startup.class.getClassLoader().getResourceAsStream("conf/config.json");

		ObjectMapper objectMapper = new ObjectMapper();

		Server server = objectMapper.readValue(inputStream, new TypeReference<Server>() {
		});

	}
}
