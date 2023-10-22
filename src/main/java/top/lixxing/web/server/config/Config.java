package top.lixxing.web.server.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.LogManager;

public class Config {

    private final Logger logger = LoggerFactory.getLogger(Config.class);
    private final Properties properties = new Properties();
    private final MimetypesFileTypeMap typeMap = new MimetypesFileTypeMap();
    private static final LogManager logManager = LogManager.getLogManager();
    private static final String logConfig= "conf/logging.properties";

    private static Config config;
    public static final String CONFIG_NAME = "conf/http.properties";
    public static final String SERVER_NAME = "SimpleHttpServer";
    public static final String SERVER_PORT = "server.port";
    public static final String SERVER_GZIP = "server.gzip";
    public static final String SERVER_WORK_THREAD = "server.work.thread";
    public static final String SERVER_URL = "server.upload.url";
    public static final String UPLOAD_PATH = "server.upload.path";
    public static final String WEB_PATH = "server.web.path";
    public static final String WEB_INDEX = "server.web.index";
    public static final String WEB_TRY_FILE = "server.web.try.file";
    public static final String PROXY_URL = "server.proxy.url";
    public static final String PROXY_TARGET = "server.proxy.target";
    public static final String PROXY_REMOVE_URL = "server.proxy.remove.url";
    public static final String AUTH_PATH = "server.auth.path";
    public static final String AUTH_HEADER = "server.auth.header";
    public static final String AUTH_TOKEN = "server.auth.token";

    private Config() {
        loadProperties();
    }

    static {
        try {
            InputStream inputStream = new FileInputStream(logConfig);
            logManager.readConfiguration(inputStream);
        } catch (Exception e) {
        }
    }

    public static Config getInstance() {
        if (config == null) {
            synchronized (Config.class) {
                if (config == null) {
                    config = new Config();
                }
            }
        }
        return config;
    }

    private void loadProperties() {

        try {
            properties.load(new FileReader(CONFIG_NAME));
        } catch (Exception e) {
            logger.warn("配置文件不存在,请检查类路径下的" + CONFIG_NAME + "文件");
        }
    }

    public Properties getProperties() {
        return properties;
    }

    public String getContentType(File file) {
        return typeMap.getContentType(file);
    }

    public String getContentType(String filename) {
        return typeMap.getContentType(filename);
    }
}
