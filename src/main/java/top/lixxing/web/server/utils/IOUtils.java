package top.lixxing.web.server.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {

	public static long writeFromStream(OutputStream outputStream, InputStream inputStream) throws IOException {
		int data;
		long length = 0;
		while ((data = inputStream.read()) != -1) {
			outputStream.write(data);
			length++;
		}
		return length;
	}

	public static byte[] readFromStream(InputStream inputStream) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		writeFromStream(byteArrayOutputStream, inputStream);
		return byteArrayOutputStream.toByteArray();
	}
}
