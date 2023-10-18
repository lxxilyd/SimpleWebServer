package top.lixxing.web.server.logger;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class LoggerFactory {


	private static final LogManager logManager = LogManager.getLogManager();
	private static final String logConfig= "conf/logging.properties";

	static {
		try {
			InputStream inputStream = new FileInputStream(logConfig);
			logManager.readConfiguration(inputStream);
		} catch (Exception e) {
		}
	}

	public static Logger getLogger(String name) {
		Logger logger = Logger.getLogger(name);
		logManager.addLogger(logger);
		logger.setLevel(Level.ALL);
		return logger;
	}

	public static Logger getLogger(Class<?> logClass) {
		return getLogger(logClass.getName());
	}
}
