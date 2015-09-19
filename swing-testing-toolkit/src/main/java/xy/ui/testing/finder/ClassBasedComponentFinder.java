package xy.ui.testing.finder;

import java.awt.Component;
import java.text.MessageFormat;

import xy.reflect.ui.info.annotation.Validating;
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
	protected boolean matchesInContainingWindow(Component c) {
		return c.getClass().getName().equals(componentClassName);
	}

	@Override
	protected boolean initializeSpecificCriterias(Component c) {
		componentClassName = c.getClass().getName();
		return true;
	}

	@Override
	public String toString() {
		return MessageFormat.format(
				"<{0}> component n°{1} in the window n°{2}",
				componentClassName, (occurrencesToSkip + 1), (windowIndex+1));
	}

	@Override
	@Validating
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
			throw new ValidationError("Invalid class name: : Class not found");
		}

	}

}
