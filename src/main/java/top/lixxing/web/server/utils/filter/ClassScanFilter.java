package top.lixxing.web.server.utils.filter;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public interface ClassScanFilter {

	Class<?> target();

	List<Class<?>> doFilter(List<Class<?>> classes);

	default List<Class<?>> doAnnotationFilter(List<Class<?>> classes) {
		List<Class<?>> result = new ArrayList<>();
		for (Class<?> aClass : classes) {
			Class<? extends Annotation> target = (Class<? extends Annotation>) target();
			if (aClass.getAnnotation(target) != null) {
				result.add(aClass);
			}
		}
		return result;
	}

	default List<Class<?>> doAssignableFilter(List<Class<?>> classes) {
		List<Class<?>> result = new ArrayList<>();
		for (Class<?> aClass : classes) {
			if (target().isAssignableFrom(aClass)) {
				result.add(aClass);
			}
		}
		return result;
	}
}
