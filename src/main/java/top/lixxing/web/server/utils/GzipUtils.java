package top.lixxing.web.server.utils;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipUtils {

	private static final Logger logger = LoggerFactory.getLogger(GzipUtils.class);

	/**
	 * 对数据进行gzip压缩
	 * @param data
	 * @return
	 */
	public static byte[] gzip(byte[] data) {
		if (data == null || data.length == 0) {
			return data;
		}
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try(GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream)) {
			gzipOutputStream.write(data);
			gzipOutputStream.finish();
			gzipOutputStream.flush();
		} catch (Exception e) {
			logger.error(e.getMessage() ,e);
			return data;
		}
		return outputStream.toByteArray();
	}

	/**
	 * 对数据进行gzip解压缩
	 * @param data
	 * @return
	 */
	public static byte[] unGzip(byte[] data) {
		if (data == null || data.length == 0) {
			return data;
		}
		ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try (GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream)){
			byte[] gzipData = new byte[gzipInputStream.available()];
			gzipInputStream.read(gzipData);
			outputStream.write(gzipData);
			outputStream.flush();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return data;
		}
		return outputStream.toByteArray();
	}
}
