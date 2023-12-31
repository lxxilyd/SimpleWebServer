package top.lixxing.web.server.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {

	/**
	 * 将输入流的数据写出到输出流
	 * @param outputStream
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	public static long writeFromStream(OutputStream outputStream, InputStream inputStream) throws IOException {
		int data;
		long length = 0;
		while ((data = inputStream.read()) != -1) {
			outputStream.write(data);
			length++;
		}
		return length;
	}

	/**
	 * 从输入流读取数据
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	public static byte[] readFromStream(InputStream inputStream) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		writeFromStream(byteArrayOutputStream, inputStream);
		return byteArrayOutputStream.toByteArray();
	}
}
