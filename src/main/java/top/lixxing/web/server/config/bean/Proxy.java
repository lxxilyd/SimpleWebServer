package top.lixxing.web.server.config.bean;

public class Proxy {

	private String url;

	private String target;

	private Boolean removeProxyUrl;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public Boolean getRemoveProxyUrl() {
		return removeProxyUrl;
	}

	public void setRemoveProxyUrl(Boolean removeProxyUrl) {
		this.removeProxyUrl = removeProxyUrl;
	}
}
