package xy.ui.testing.finder;

import java.awt.Component;
import xy.ui.testing.Tester;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.MiscUtils;
import xy.ui.testing.util.ValidationError;

/**
 * The base class of component finders that use class information to find the
 * components.
 * 
 * @author olitank
 *
 */
public abstract class AbstractClassBasedComponentFinder extends MatchingComponentFinder {
	private static final long serialVersionUID = 1L;

	protected String componentClassName = "";

	public String getComponentClassName() {
		return componentClassName;
	}

	public void setComponentClassName(String componentClassName) {
		this.componentClassName = componentClassName;
	}

	@Override
	protected boolean matchesInContainingWindow(Component c, Tester tester) {
		Class<?> expectedClass;
		try {
			expectedClass = Class.forName(componentClassName);
		} catch (ClassNotFoundException e) {
			throw new AssertionError(e);
		}
		return expectedClass.isInstance(c);
	}

	@Override
	protected boolean initializeSpecificValues(Component c, TestEditor testEditor) {
		Class<?> componentClass = c.getClass();
		while (componentClass.isAnonymousClass()) {
			componentClass = componentClass.getSuperclass();
		}
		componentClassName = componentClass.getName();
		return true;
	}

	@Override
	public String toString() {
		String result = "";
		if (componentClassName == null) {
			result += "<unspecified class>";
		} else {
			try {
				Class<?> clazz = Class.forName(componentClassName);
				if (clazz.isAnonymousClass()) {
					result += "<" + clazz.getName() + ">";
				} else {
					result += clazz.getSimpleName();
				}
			} catch (ClassNotFoundException e) {
				result += "<" + componentClassName + ">";
			}
		}
		result = MiscUtils.formatOccurrence(result, occurrencesToSkip);
		return result;
	}

	@Override
	public void validate() throws ValidationError {
		if (componentClassName == null) {
			throw new ValidationError("Missing component class name");
		}
		try {
			Class<?> clazz = Class.forName(componentClassName);
			if (!Component.class.isAssignableFrom(clazz)) {
				throw new ValidationError(
						"The component class is not a sub-type of '" + Component.class.getName() + "'");
			}
		} catch (ClassNotFoundException e) {
			throw new ValidationError("Invalid class name: Class not found");
		}

	}

}
