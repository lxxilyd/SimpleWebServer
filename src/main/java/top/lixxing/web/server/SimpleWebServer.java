package top.lixxing.web.server;

import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.lixxing.web.server.handler.WebHandler;
import top.lixxing.web.server.config.Config;
import top.lixxing.web.server.handler.DispatcherHandler;
import top.lixxing.web.server.utils.ClassScanUtils;
import top.lixxing.web.server.utils.filter.AssignableScanFilter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SimpleWebServer {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public void run() throws IOException, InstantiationException, IllegalAccessException {
        Config config = Config.getInstance();
        Properties properties = config.getProperties();
        String port = properties.getProperty(Config.SERVER_PORT, "10025");
        String workThread = properties.getProperty(Config.SERVER_WORK_THREAD, "3");
		int threadCount = Integer.parseInt(workThread);
		Executor executor = new ThreadPoolExecutor(threadCount, threadCount, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(threadCount));
        HttpServer server = HttpServer.create();

        server.bind(new InetSocketAddress(Integer.parseInt(port)), 100);
        server.setExecutor(executor);

        server.createContext("/", new DispatcherHandler());

        server.start();
        logger.info("SimpleHttpServer start in port {} ...", port);
    }

    private List<WebHandler> loadHandler() throws IOException, IllegalAccessException, InstantiationException {
        List<Class<?>> classes =  ClassScanUtils.scanAllWithFilter("top", new AssignableScanFilter(WebHandler.class));

        List<WebHandler> handlers = new ArrayList<>();
        for (Class<?> aClass : classes) {
            if (aClass.isInterface()) {
                continue;
            }
            WebHandler baseWebHandler = (WebHandler) aClass.newInstance();
            handlers.add(baseWebHandler);
        }
        return handlers;
    }
}
