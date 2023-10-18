package top.lixxing.web.server;

public class Startup {

	public static void main(String[] args) {

		SimpleWebServer simpleWebServer = new SimpleWebServer();

		try {
			simpleWebServer.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
