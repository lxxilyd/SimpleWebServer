package top.lixxing.web.server.utils.filter;

import java.lang.annotation.Annotation;
import java.util.List;

public class AnnotationScanFilter implements ClassScanFilter {

	private final Class<? extends Annotation> annotationClass;

	public AnnotationScanFilter(Class<? extends Annotation> annotationClass) {
		this.annotationClass = annotationClass;
	}

	@Override
	public Class<?> target() {
		return annotationClass;
	}

	@Override
	public List<Class<?>> doFilter(List<Class<?>> classes) {
		return doAnnotationFilter(classes);
	}
}
