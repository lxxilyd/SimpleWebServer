package top.lixxing.web.server.utils;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPOutputStream;

public class GzipUtils {

	private static final Logger logger = LoggerFactory.getLogger(GzipUtils.class);

	public static byte[] gzip(byte[] data) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try(GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream)) {
			gzipOutputStream.write(data);
			gzipOutputStream.finish();
		} catch (Exception e) {
			logger.error(e.getMessage() + e);
		}
		return outputStream.toByteArray();
	}
}
