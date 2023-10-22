package top.lixxing.web.server.handler;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.lixxing.web.server.config.Config;
import top.lixxing.web.server.response.HttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class UploadHandler implements WebHandler {

    private final Logger logger = LoggerFactory.getLogger(UploadHandler.class);

    private final String savePath = properties.getProperty(Config.UPLOAD_PATH, "upload");
    private final String url = properties.getProperty(Config.SERVER_URL, "/upload");

    @Override
    public String url() {
        return url;
    }

    @Override
    public HttpResponse doRequest(HttpExchange exchange) throws IOException {

        String method = exchange.getRequestMethod();
        if (!exchange.getRequestURI().getPath().equals(url)) {
            return HttpResponse.NOT_FOUND;
        }
        if (!"POST".equals(method)) {
            String msg = "Method (" + method + ") is Not Support";
            logger.warn(msg);
            return HttpResponse.methodNotAllowed(msg);
        }
        Map<String, byte[]> dataMap = parseData(exchange);
        for (Map.Entry<String, byte[]> dataEntry : dataMap.entrySet()) {
            saveFile(dataEntry.getValue(), dataEntry.getKey());
        }
        return HttpResponse.OK;
    }

    private Map<String, byte[]> parseData(HttpExchange exchange) throws IOException {
        InputStream requestBody = exchange.getRequestBody();
        Headers requestHeaders = exchange.getRequestHeaders();
        String contentType = requestHeaders.get("Content-Type").get(0);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int data;
        while ((data = requestBody.read()) != -1) {
            byteArrayOutputStream.write(data);
        }
        byte[] bytes = byteArrayOutputStream.toByteArray();
        // 使用ISO-8859-1编码防止字符串转回byte[]发生变化
        String content = new String(bytes, "ISO8859-1");

        Map<String, byte[]> dataMap = new LinkedHashMap<>();
        if (contentType.contains(BOUNDARY)) {
            String boundary = contentType.substring(contentType.lastIndexOf(BOUNDARY) + BOUNDARY.length());
            String boundaryStart = "--" + boundary;
            String boundaryEnd = "--" + boundary + "--";
            content = content.substring(0, content.lastIndexOf(boundaryEnd) + boundaryEnd.length());
            content = content.replace(boundaryEnd, "");
            String[] boundaryContent = content.split(boundaryStart);
            StringBuilder builder = new StringBuilder();
            for (String contentString : boundaryContent) {
                if (contentString.equals("")) {
                    continue;
                }
                String[] split = contentString.split("\r\n");
                builder.delete(0, builder.length());
                int count = 0;
                String filename = "";
                for (String line : split) {
                    if ("".equals(line)) {
                        continue;
                    }
                    count++;
                    if (line.contains("filename=")) {
                        String utfLine = new String(line.getBytes("ISO-8859-1"));
                        int beginIndex = utfLine.indexOf("filename=") + "filename".length() + 2;
                        int endIndex = utfLine.lastIndexOf(";") - 1;
                        if (endIndex > beginIndex) {
                            filename = utfLine.substring(beginIndex, endIndex);
                        } else {
                            filename = utfLine.substring(beginIndex, utfLine.length() -1);
                        }
                    }
                    if (count > 2) {
                        builder.append(line).append("\r\n");
                    }
                }
                dataMap.put(filename, builder.toString().getBytes("ISO-8859-1"));
            }
        }
        return dataMap;
    }

    private void saveFile(byte[] data, String name) throws IOException {
        try {
            File file = new File(savePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            String filename = savePath + "/" + name;
            File saveFile = new File(filename);
            OutputStream outputStream = new FileOutputStream(saveFile);
            outputStream.write(data);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
