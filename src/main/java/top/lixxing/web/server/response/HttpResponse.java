package top.lixxing.web.server.response;

public class HttpResponse {

	public static final String MSG_OK = "OK";
	public static final String MSG_NOT_FOUND = "404 Not Found";
	public static final String MSG_METHOD_NOTE_ALLOW = "405 Method Not Allowed";
	public static final String MSG_SERVER_ERROR = "500 Server Error";
	public static final String MSG_BAD_GATEWAY = "502 Bad Gateway";
	public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";

	public static final HttpResponse OK = new HttpResponse(200, MSG_OK.getBytes(), CONTENT_TYPE_TEXT_PLAIN);
	public static final HttpResponse NOT_FOUND = new HttpResponse(404, MSG_NOT_FOUND.getBytes(), CONTENT_TYPE_TEXT_PLAIN);
	public static final HttpResponse METHOD_NOT_ALLOWED = new HttpResponse(405, MSG_METHOD_NOTE_ALLOW.getBytes(), CONTENT_TYPE_TEXT_PLAIN);
	public static final HttpResponse SERVER_ERROR = new HttpResponse(500, MSG_SERVER_ERROR.getBytes(), CONTENT_TYPE_TEXT_PLAIN);
	public static final HttpResponse BAD_GATEWAY = new HttpResponse(502, MSG_BAD_GATEWAY.getBytes(), CONTENT_TYPE_TEXT_PLAIN);

	private int status; // 状态，参考http状态码

	private byte[] data; // 报文数据 message信息自行转换为二进制数据

	private String contentType; // Content-Type 参考http Content-Type

	public HttpResponse(int status, byte[] data, String contentType) {
		this.status = status;
		this.data = data;
		this.contentType = contentType;
	}

	public static HttpResponse ok(String message) {
		return ok(message.getBytes(), CONTENT_TYPE_TEXT_PLAIN);
	}

	public static HttpResponse ok(byte[] data) {
		return ok(data, CONTENT_TYPE_TEXT_PLAIN);
	}

	public static HttpResponse ok(byte[] data, String contentType) {
		return new HttpResponse(200, data, contentType);
	}

	public static HttpResponse methodNotAllowed(String message) {
		return methodNotAllowed(message.getBytes(), CONTENT_TYPE_TEXT_PLAIN);
	}

	public static HttpResponse methodNotAllowed(byte[] data) {
		return methodNotAllowed(data, CONTENT_TYPE_TEXT_PLAIN);
	}

	public static HttpResponse methodNotAllowed(byte[] data, String contentType) {
		return new HttpResponse(405, data, contentType);
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
}
