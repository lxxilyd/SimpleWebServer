package top.lixxing.web.server.config;

import top.lixxing.web.server.logger.LoggerFactory;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileReader;
import java.util.Properties;
import java.util.logging.Logger;

public class Config {

    private final Logger logger = LoggerFactory.getLogger(Config.class);
    private final Properties properties = new Properties();
    private final MimetypesFileTypeMap typeMap = new MimetypesFileTypeMap();

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
    public static final String PROXY_URL = "server.proxy.url";
    public static final String PROXY_TARGET = "server.proxy.target";
    public static final String PROXY_REMOVE_URL = "server.proxy.remove.url";

    private Config() {
        loadProperties();
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
            logger.warning("配置文件不存在,请检查类路径下的" + CONFIG_NAME + "文件");
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
