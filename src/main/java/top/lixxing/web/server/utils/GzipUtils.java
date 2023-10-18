package top.lixxing.web.server.utils;


import top.lixxing.web.server.logger.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

public class GzipUtils {

	private static final Logger logger = LoggerFactory.getLogger(GzipUtils.class);

	public static byte[] gzip(byte[] data) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try(GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream)) {
			gzipOutputStream.write(data);
			gzipOutputStream.finish();
		} catch (Exception e) {
			logger.severe(e.getMessage() + e);
		}
		return outputStream.toByteArray();
	}
}
