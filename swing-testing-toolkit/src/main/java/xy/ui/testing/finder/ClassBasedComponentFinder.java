package xy.ui.testing.finder;

import java.awt.Component;
import java.text.MessageFormat;

import xy.ui.testing.Tester;
import xy.ui.testing.editor.TesterEditor;
import xy.ui.testing.util.ValidationError;

public class ClassBasedComponentFinder extends MatchingComponentFinder {
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
		Class<?> expecteClass;
		try {
			expecteClass = Class.forName(componentClassName);
		} catch (ClassNotFoundException e) {
			throw new AssertionError(e);
		}
		return expecteClass.isInstance(c);
	}

	@Override
	protected boolean initializeSpecificValues(Component c, TesterEditor testerEditor) {
		componentClassName = c.getClass().getName();
		return true;
	}

	@Override
	public String toString() {
		String componentClassString = componentClassName;
		if ((componentClassName == null) || (componentClassName.length() == 0)) {
			componentClassString = "?";
		}
		return MessageFormat.format("<{0}> n°{1} in the window n°{2}", componentClassString,
				(occurrencesToSkip + 1), (windowIndex + 1));
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
