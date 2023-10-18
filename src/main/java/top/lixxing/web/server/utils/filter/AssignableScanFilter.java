package top.lixxing.web.server.utils.filter;

import java.util.List;

public class AssignableScanFilter implements ClassScanFilter {

	private final Class<?> targetClass;

	public AssignableScanFilter(Class<?> targetClass) {
		this.targetClass = targetClass;
	}

	@Override
	public Class<?> target() {
		return targetClass;
	}

	@Override
	public List<Class<?>> doFilter(List<Class<?>> classes) {
		return doAssignableFilter(classes);
	}

}
