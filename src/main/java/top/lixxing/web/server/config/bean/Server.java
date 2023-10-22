package top.lixxing.web.server.config.bean;

import java.util.List;

public class Server {

	private Integer port;

	private Integer workThreads;

	private Boolean gzip;

	private Upload upload;

	private Auth auth;

	private List<Web> web;

	private List<Proxy> proxy;

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public Integer getWorkThreads() {
		return workThreads;
	}

	public void setWorkThreads(Integer workThreads) {
		this.workThreads = workThreads;
	}

	public Boolean getGzip() {
		return gzip;
	}

	public void setGzip(Boolean gzip) {
		this.gzip = gzip;
	}

	public Upload getUpload() {
		return upload;
	}

	public void setUpload(Upload upload) {
		this.upload = upload;
	}

	public Auth getAuth() {
		return auth;
	}

	public void setAuth(Auth auth) {
		this.auth = auth;
	}

	public List<Web> getWeb() {
		return web;
	}

	public void setWeb(List<Web> web) {
		this.web = web;
	}

	public List<Proxy> getProxy() {
		return proxy;
	}

	public void setProxy(List<Proxy> proxy) {
		this.proxy = proxy;
	}
}
