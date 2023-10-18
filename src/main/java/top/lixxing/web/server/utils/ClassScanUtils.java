package top.lixxing.web.server.utils;

import top.lixxing.web.server.logger.LoggerFactory;
import top.lixxing.web.server.utils.filter.ClassScanFilter;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

public class ClassScanUtils {

	private static final Logger logger = LoggerFactory.getLogger(ClassScanUtils.class);

	public static List<Class<?>> scanAllWithFilter(String packageName, ClassScanFilter filter) throws IOException {
		return filter.doFilter(scanAllClass(packageName));
	}

	public static List<Class<?>> scanAllClass(String packageName) throws IOException {
		List<Class<?>> classes = new ArrayList<>();

		Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(packageName);
		logger.fine("start scan Class...");
		while (resources.hasMoreElements()) {
			URL url = resources.nextElement();
			if (url.getProtocol().equals("file")) {
				String basePath = url.getPath().substring(1).replace("/", "\\").replace(packageName, "");
				classes.addAll(scanFileClass(basePath, basePath));
			}
			if (url.getProtocol().equals("jar")) {
				classes.addAll(scanJarClass(url));
			}

		}
		return classes;
	}

	private static List<Class<?>> scanFileClass(String path, String basePath) throws IOException {
		List<Class<?>> classes = new ArrayList<>();

		File base = new File(path);
		File[] files = base.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				List<Class<?>> classList = scanFileClass(file.getPath(), basePath);
				classes.addAll(classList);
			}
			logger.fine("scan file :" + file.getAbsolutePath());
			Class<?> aClass = nameToClass(file.getAbsolutePath().replace(basePath, ""));
			if (aClass != null) {
				classes.add(aClass);
			}
		}
		return classes;
	}

	private static List<Class<?>> scanJarClass(URL url) throws IOException {
		List<Class<?>> classes = new ArrayList<>();

		logger.fine("scanJarFile url: " + url);
		JarFile jarFile = null;
		URLConnection connection = url.openConnection();
		if (connection instanceof JarURLConnection) {
			JarURLConnection jarURLConnection = (JarURLConnection) connection;
			jarFile = jarURLConnection.getJarFile();
		}

		if (jarFile != null) {
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				JarEntry jarEntry = entries.nextElement();
				Class<?> aClass = nameToClass(jarEntry.getName());
				if (aClass != null) {
					classes.add(aClass);
				}
			}
		}

		return classes;
	}

	private static String pathToClassName(String path) {
		return path.replaceAll("\\$\\d", ".")
				.replace(".class", "")
				.replace("\\", ".")
				.replace("/", ".");
	}

	private static Class<?> nameToClass(String name) {
		if (name.endsWith("class") && !name.contains("$")) {
			String className = pathToClassName(name);
			try {
				return ClassLoader.getSystemClassLoader().loadClass(className);
			} catch (Exception e) {
				logger.severe(e.getMessage() + ": " + e.getLocalizedMessage());
			}
		}
		return null;
	}
}
