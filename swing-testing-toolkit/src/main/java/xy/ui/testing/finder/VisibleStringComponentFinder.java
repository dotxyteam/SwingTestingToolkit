package xy.ui.testing.finder;

import java.awt.Component;
import java.lang.reflect.Method;

import org.apache.commons.lang3.StringEscapeUtils;

public class VisibleStringComponentFinder extends ComponentFinder {

	protected String visibleString = "";

	public String getVisibleString() {
		return visibleString;
	}

	public void setVisibleString(String visibleString) {
		this.visibleString = visibleString;
	}

	@Override
	protected boolean initializeAllCriteriasExceptOccurrencesToskip(Component c) {
		visibleString = extractVisibleString(c);
		return visibleString != null;
	}

	@Override
	protected boolean matches(Component c) {
		return visibleString.equals(extractVisibleString(c));
	}

	public static String extractVisibleString(Component c) {
		String s;
		s = extractVisibleStringThroughMethod(c, "getTitle");
		if (s != null) {
			return s;
		}
		s = extractVisibleStringThroughMethod(c, "getText");
		if (s != null) {
			return s;
		}
		return null;
	}

	protected static String extractVisibleStringThroughMethod(Component c,
			String methodName) {
		try {
			Method method = c.getClass().getMethod(methodName);
			String result = (String) method.invoke(c);
			if (result == null) {
				return null;
			}
			if (result.trim().length() == 0) {
				return null;
			}
			return result;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public String toString() {
		return "Component n°" + (occurrencesToSkip + 1)
				+ " displaying the string \""
				+ StringEscapeUtils.escapeJava(visibleString) + "\"";
	}
}
