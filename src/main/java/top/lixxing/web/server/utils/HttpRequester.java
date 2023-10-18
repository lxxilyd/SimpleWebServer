package top.lixxing.web.server.utils;


import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class HttpRequester {

	private static final String CHARSET = "UTF-8";

	private static final String METHOD_GET = "GET";
	private static final String METHOD_POST = "POST";
	private static final String METHOD_PUT = "PUT";
	private static final String METHOD_PATCH = "PATCH";
	private static final String METHOD_DELETE= "DELETE";
	private static final String METHOD_HEAD= "HEAD";
	private static final String METHOD_OPTIONS= "OPTIONS";

	private static final String CONTENT_TYPE= "Content-Type";
	private static final String CONTENT_LENGTH= "Content-Length";
	private static final String CONNECTION= "Connection";
	private static final String USER_AGENT= "User-Agent";
	private static final String ACCEPT= "*/*";
	private static final String KEEP_ALIVE= "keep-alive";
	private static final String ACCEPT_ENCODING= "gzip, deflate, br";
	private static final String USER_AGENT_VALUE= "HttpRequester/1.0";
	private static final String CONTENT_DISPONSION_WITH_FILE = "Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"\r\n";
	private static final String CONTENT_DISPONSION_WITH_KEY = "Content-Disposition: form-data; name=\"%s\"\r\n\r\n";
	private static final String CONTENT_TYPE_WITH_MINE = "Content-Type: %s\r\n\r\n";
	private static final String CONTENT_TYPE_JSON= "application/json";
	private static final String CONTENT_TYPE_JS= "application/javascript";
	private static final String CONTENT_TYPE_STREAM= "application/octet-stream";
	private static final String CONTENT_TYPE_UNKONW= "content/unknown";
	private static final String CONTENT_TYPE_FORM_URLDECODED= "application/x-www-form-urlencoded";
	private static final String CONTENT_TYPE_FORM_MULITPART= "multipart/form-data; boundary=";
	private static final String TLS= "TLS";
	private static final String CHAR_LIB= "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String BOUNDARY_PREFIX = "-----------------------------";
	private static final String BOUNDARY_SUFFIX = "--";
	private static final String NEW_LINE = "\r\n";
	private static final int BOUNDARY_LENGTH= 30;


	private final String url;
	private final Map<String, String> headers = new HashMap<>();
	private final Map<String, Object> params = new HashMap<>();
	private final Map<String, Object> forms = new HashMap<>();
	private final Map<String, Object> fileForms = new LinkedHashMap<>();
	private final AtomicBoolean ignoreSSLCheck = new AtomicBoolean(false);
	private final String method;
	private String requestBody;
	private byte[] requestData;
	private InputStream requestInputStream;

	private Consumer<Response> success;
	private Consumer<Response> error;

	private HttpRequester(String url, String method) {
		this.url = url;
		this.method = method;
		this.header(USER_AGENT, USER_AGENT_VALUE);
		this.header(CONNECTION, KEEP_ALIVE);
	}

	/**
	 * 设置请求方式(GET)与url
	 * @param url 请求url
	 * @return
	 */
	public static HttpRequester get(final String url) {
		return new HttpRequester(url, METHOD_GET);
	}

	/**
	 * 设置请求url
	 * @param url 请求url
	 * @return
	 */
	public static HttpRequester request(final String url, final String method) {
		return new HttpRequester(url, method);
	}

	/**
	 * 设置请求方式(POST)与url
	 * @param url 请求url
	 * @return
	 */
	public static HttpRequester post(final String url) {
		return new HttpRequester(url, METHOD_POST);
	}

	/**
	 * 设置请求头
	 * @param name 请求头名称
	 * @param value 请求头值
	 * @return
	 */
	public HttpRequester header(String name, String value) {
		this.headers.put(name, value);
		return this;
	}

	/**
	 * pil设置请求头
	 * @param headers 请求头
	 * @return
	 */
	public HttpRequester header(Map<String, String> headers) {
		this.headers.putAll(headers);
		return this;
	}

	/**
	 * 设置请求参数(url?${参数名1}=${参数值1}&${参数名2}=${参数值2}....)
	 * @param name 参数名称
	 * @param value 参数值
	 * @return
	 */
	public HttpRequester param(String name, Object value) {
		this.params.put(name, value);
		return this;
	}

	/**
	 * 批量设置请求参数(url?${参数名1}=${参数值1}&${参数名2}=${参数值2}....)
	 * @param params
	 * @return
	 */
	public HttpRequester param(Map<String, Object> params) {
		this.params.putAll(params);
		return this;
	}

	/**
	 * 设置普通form-data参数(application/x-www-form-urlencoded)
	 * @param name 参数名称
	 * @param value 参数值
	 * @return
	 */
	public HttpRequester form(String name, Object value) {
		Map<String, Object> form = new HashMap<>();
		form.put(name, value);
		return form(form);
	}

	/**
	 * 批量设置普通form-data(application/x-www-form-urlencoded)
	 * @param datas 普通form-data
	 * @return
	 */
	public HttpRequester form(Map<String, Object> datas) {
		this.forms.putAll(datas);
		return this;
	}

	/**
	 * 设置带文件的form-data(multipart/form-data; boundary=${boundaryValue})
	 * @param name 参数名称
	 * @param formFile 参数值 文件选择@FormFile
	 * @return
	 */
	public HttpRequester multipartForm(String name, Object formFile) {
		Map<String, Object> form = new HashMap<>();
		form.put(name, formFile);
		return multipartForm(form);
	}

	/**
	 * 批量设置带文件的form-data(multipart/form-data; boundary=${boundaryValue})
	 * @param datas 带文件的form-data
	 * @return
	 */
	public HttpRequester multipartForm(Map<String, Object> datas) {
		this.fileForms.putAll(datas);
		return this;
	}

	/**
	 * 设置json请求体
	 * @param json json请求体
	 * @return
	 */
	public HttpRequester requestBody(String json) {
		this.requestBody = json;
		header(CONTENT_TYPE, CONTENT_TYPE_JSON);
		return this;
	}

	/**
	 * 设置二进制请求体
	 * @param data 请求数据
	 * @return
	 */
	public HttpRequester requestBody(byte[] data) {
		this.requestData = data;
		return this;
	}

	/**
	 * 设置二进制请求体
	 * @param inputStream 请求数据
	 * @return
	 */
	public HttpRequester requestInputStream(InputStream inputStream) {
		this.requestInputStream = inputStream;
		return this;
	}

	/**
	 * 设置是否忽略SSL验证
	 * @param ignore 是否忽略
	 * @return
	 */
	public HttpRequester ignoreSSLCheck(boolean ignore) {
		this.ignoreSSLCheck.set(ignore);
		return this;
	}

	/**
	 * 执行请求，自动执行设置的成功回调与失败回调
	 */
	public void doRequest() {
		_doRequest(url, success, error);
	}

	/**
	 * 请求并返回请求结果
	 * @return 请求结果
	 */
	public String doRequestWithResponse() {
		return _doRequestWithResponse(url);
	}


	/**
	 * 下载文件
	 * @return byte[] 文件字节数组
	 */
	public byte[] download() {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		_doRequest(url, onSuccessOrError(outputStream), onSuccessOrError(outputStream));
		return outputStream.toByteArray();
	}

	/**
	 * 下载文件
	 * @return byte[] 文件字节数组
	 */
	public Response response() {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Response response = new Response();
		_doRequest(url, onSuccessOrError(outputStream, response), onSuccessOrError(outputStream, response));
		return response;
	}

	/**
	 * 下载文件并保存至指定路径，文件名称从url提取
	 * @param savePath 文件保存路径
	 * @throws IOException
	 */
	public void download(String savePath) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		_doRequest(url, onSuccessOrError(outputStream), onSuccessOrError(outputStream));
		String filename = fileNameWithUrl(url);
		File file = new File(savePath);
		if (!file.exists()) {
			file.mkdirs();
		}
		FileOutputStream fileOutputStream = new FileOutputStream(savePath + "\\" + filename);
		fileOutputStream.write(outputStream.toByteArray());
	}

	/**
	 * 设置请求成功回调
	 * @param success 成功回调方法
	 * @return
	 */
	public HttpRequester onSuccess(Consumer<Response> success) {
		this.success = success;
		return this;
	}

	/**
	 * 设置请求失败回调
	 * @param error 失败（异常）回调方法
	 * @return
	 */
	public HttpRequester onError(Consumer<Response> error) {
		this.error = error;
		return this;
	}

	private void prepareRequest(HttpURLConnection connection) throws ProtocolException {
		connection.setRequestMethod(method);
		doIgnoreSSL(connection);
		setHeaders(connection);
		formData(connection);
		multipartForm(connection);
		requestBody(connection);
		requestInputStream(connection);
	}

	private void multipartForm(HttpURLConnection connection) {
		if (!fileForms.isEmpty()) {
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			String boundary = generatorBoundary();
			connection.setRequestProperty(CONTENT_TYPE, CONTENT_TYPE_FORM_MULITPART + boundary.substring(2));
			try (OutputStream outputStream = connection.getOutputStream()) {
				for (Map.Entry<String, Object> fileEntry : fileForms.entrySet()) {
					String name = fileEntry.getKey();
					Object fileEntryValue = fileEntry.getValue();
					String start = boundary + NEW_LINE;
					outputStream.write(start.getBytes());
					if (fileEntryValue instanceof FormFile) {
						FormFile formFile = (FormFile) fileEntryValue;
						outputStream.write(String.format(CONTENT_DISPONSION_WITH_FILE, name, formFile.getFilename()).getBytes());
						outputStream.write(String.format(CONTENT_TYPE_WITH_MINE, formFile.getContentType()).getBytes());
						outputStream.write(formFile.getContent());
					} else {
						outputStream.write(String.format(CONTENT_DISPONSION_WITH_KEY, name).getBytes());
						outputStream.write(fileEntryValue.toString().getBytes());
					}
					outputStream.write(NEW_LINE.getBytes());
				}
				String end = boundary + BOUNDARY_SUFFIX + NEW_LINE;
				outputStream.write(end.getBytes());
				outputStream.flush();
			} catch (Exception e) {
				throw new HttpRequesterException(e);
			}
		}
	}

	private void formData(HttpURLConnection connection) {
		if (!forms.isEmpty()) {
			header(CONTENT_TYPE, CONTENT_TYPE_FORM_URLDECODED);
			connection.setDoOutput(true);
			try (OutputStream outputStream = connection.getOutputStream()) {
				String content = buildQuery(forms);
				outputStream.write(content.getBytes(CHARSET));
				outputStream.flush();
			} catch (Exception e) {
				throw new HttpRequesterException(e);
			}
		}
	}

	private void requestBody(HttpURLConnection connection) {
		if (requestBody != null || requestData != null) {
			if ("GET".equals(method)) {
				connection.setDoOutput(true);
				try (OutputStream outputStream = connection.getOutputStream()) {
					byte[] bytes = Optional.ofNullable(requestBody.getBytes(CHARSET)).orElse(requestData);
					outputStream.write(bytes);
					outputStream.flush();
				} catch (Exception e) {
					throw new HttpRequesterException(e);
				}
			}
		}
	}

	private void requestInputStream(HttpURLConnection connection) {
		if (requestInputStream != null) {
			if (!"GET".equals(method)) {
				connection.setDoOutput(true);
				try (OutputStream outputStream = connection.getOutputStream()) {
					IOUtils.writeFromStream(outputStream, requestInputStream);
					outputStream.flush();
				} catch (Exception e) {
					throw new HttpRequesterException(e);
				}
			}
		}
	}

	private void setHeaders(HttpURLConnection connection) {
		headers.forEach(connection::addRequestProperty);
	}

	private void doIgnoreSSL(HttpURLConnection connection) {
		// 忽略ssl验证
		if (ignoreSSLCheck.get()) {
			if (connection instanceof HttpsURLConnection) {
				try {
					HttpsURLConnection httpsURLConnection = (HttpsURLConnection) connection;
					SSLContext ctx = SSLContext.getInstance(TLS);
					ctx.init(null, new TrustManager[] { new TrustAllTrustManager() }, new SecureRandom());
					httpsURLConnection.setSSLSocketFactory(ctx.getSocketFactory());
					httpsURLConnection.setHostnameVerifier((x, y) -> true);
				} catch (Exception e) {
					throw new HttpRequesterException(e);
				}
			}
		}
	}

	private String _doRequestWithResponse(String url) {
		String content;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		_doRequest(url, onSuccessOrError(outputStream), onSuccessOrError(outputStream));
		content =outputStream.toString();
		return content;
	}

	private Consumer<Response> onSuccessOrError(OutputStream outputStream, Response res) {
		return response -> {
			try {
				int data;
				while ((data = response.inputStream.read()) != -1) {
					outputStream.write(data);
				}
				outputStream.flush();
				outputStream.close();
				res.setCode(response.code);
				res.setInputStream(response.inputStream);
				res.setHeaders(response.headers);
				res.setException(response.exception);
				if (outputStream instanceof ByteArrayOutputStream) {
					byte[] bytes = ((ByteArrayOutputStream) outputStream).toByteArray();
					res.setData(bytes);
				}
			} catch (Exception e) {
				throw new HttpRequesterException(e);
			}
		};
	}

	private Consumer<Response> onSuccessOrError(OutputStream outputStream) {
		return onSuccessOrError(outputStream, new Response());
	}

	private URL prepareUrl(String url) throws UnsupportedEncodingException, MalformedURLException {
		StringBuilder builder = new StringBuilder(url);
		if (!params.isEmpty()) {
			builder.append("?").append(buildQuery(params));
		}
		return new URL(builder.toString());
	}

	private void _doRequest(String url, Consumer<Response> success, Consumer<Response> error) {

		HttpURLConnection connection = null;
		Response response;
		Response.Builder builder = Response.builder();
		try {
			connection = (HttpURLConnection) prepareUrl(url).openConnection();
			prepareRequest(connection);
			response = builder.code(connection.getResponseCode())
					.headers(connection.getHeaderFields())
					.inputStream(connection.getInputStream())
					.build();
			if (success != null) {
				success.accept(response);
			}
		} catch (Exception e) {
			if (connection != null) {
				response = builder.inputStream(connection.getErrorStream())
						.exception(e)
						.build();
				if (error != null) {
					error.accept(response);
				}
				connection.disconnect();
			}
		}
	}

	private String generatorBoundary() {
		StringBuilder builder = new StringBuilder(BOUNDARY_PREFIX);
		for (int i = 0; i < BOUNDARY_LENGTH; i++) {
			builder.append(CHAR_LIB.charAt(new SecureRandom().nextInt(CHAR_LIB.length())));
		}
		return builder.toString();
	}

	private static String fileNameWithUrl(String url) {
		url = url.replace("\\", "/");
		return url.substring(url.lastIndexOf("/") + 1);
	}

	private String buildQuery(Map<String, Object> query) throws UnsupportedEncodingException {
		Iterator<Map.Entry<String, Object>> iterator = query.entrySet().iterator();
		StringBuilder builder = new StringBuilder();
		while (iterator.hasNext()) {
			Map.Entry<String, Object> next = iterator.next();
			builder.append(URLEncoder.encode(next.getKey(), CHARSET))
					.append("=")
					.append(URLEncoder.encode(next.getValue().toString(), CHARSET));
			if (iterator.hasNext()) {
				builder.append("&");
			}
		}
		return builder.toString();
	}

	/**
	 * multipart/for-data文件类型的封装
	 */
	public static class FormFile {

		private String filename;

		private String contentType;

		private byte[] content;

		private long length;

		public FormFile filename(String filename) {
			this.filename = filename;
			return this;
		}

		public FormFile contentType(String contentType) {
			this.contentType = contentType;
			return this;
		}

		public FormFile content(byte[] content) {
			this.content = content;
			return this;
		}

		public FormFile length(long length) {
			this.length = length;
			return this;
		}

		public String getFilename() {
			return filename;
		}

		public String getContentType() {
			if (contentType.equalsIgnoreCase(CONTENT_TYPE_UNKONW)) {
				return CONTENT_TYPE_STREAM;
			}
			return contentType;
		}

		public byte[] getContent() {
			return content;
		}

		public long getLength() {
			return length;
		}

		public static FormFile readFromDisk(String path) throws IOException {
			File file = new File(path);
			URLConnection urlConnection = file.toURL().openConnection();
			String contentType = urlConnection.getContentType();
			long contentLength= urlConnection.getContentLengthLong();
			InputStream inputStream = urlConnection.getInputStream();
			byte[] content = new byte[inputStream.available()];
			inputStream.read(content);
			String filename = HttpRequester.fileNameWithUrl(path);
			return new FormFile().filename(filename).contentType(contentType).content(content).length(contentLength);
		}
	}

	public static class Response {

		private int code;

		private Map<String, List<String>> headers;

		private InputStream inputStream;

		private Exception exception;

		private byte[] data;

		public int getCode() {
			return code;
		}

		public void setCode(int code) {
			this.code = code;
		}

		public Map<String, List<String>> getHeaders() {
			return headers;
		}

		public void setHeaders(Map<String, List<String>> headers) {
			this.headers = headers;
		}

		public InputStream getInputStream() {
			return inputStream;
		}

		public void setInputStream(InputStream inputStream) {
			this.inputStream = inputStream;
		}

		public Exception getException() {
			return exception;
		}

		public void setException(Exception exception) {
			this.exception = exception;
		}

		public byte[] getData() {
			return data;
		}

		public void setData(byte[] data) {
			this.data = data;
		}

		public static Builder builder() {
			return new Builder();
		}

		public static class Builder {

			private final Response response;

			Builder() {
				response = new Response();
			}

			public Builder code(int code) {
				response.setCode(code);
				return this;
			}

			public Builder headers(Map<String, List<String>> headers) {
				response.setHeaders(headers);
				return this;
			}

			public Builder inputStream(InputStream inputStream) {
				response.setInputStream(inputStream);
				return this;
			}

			public Builder exception(Exception e) {
				response.setException(e);
				return this;
			}

			public Response build() {
				return response;
			}
		}
	}

	private static class TrustAllTrustManager implements X509TrustManager {
		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}
	}

	private class HttpRequesterException extends RuntimeException {

		HttpRequesterException(String message) {
			super(message);
		}

		HttpRequesterException(Throwable throwable) {
			super(throwable);
		}

		HttpRequesterException(String message, Throwable throwable) {
			super(message, throwable);
		}
	}
}
