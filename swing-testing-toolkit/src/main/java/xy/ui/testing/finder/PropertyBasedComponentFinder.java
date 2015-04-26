package xy.ui.testing.finder;

import java.awt.Component;
import java.text.MessageFormat;
import java.util.List;

import xy.ui.testing.action.component.property.CheckComponentPropertyAction;
import xy.ui.testing.util.TestingError;

public class PropertyBasedComponentFinder extends MatchingComponentFinder {
	private static final long serialVersionUID = 1L;
	CheckComponentPropertyAction checkPropertyAction = new CheckComponentPropertyAction();

	public String getPropertyName() {
		return checkPropertyAction.getPropertyName();
	}

	public void setPropertyName(String propertyyName) {
		checkPropertyAction.setPropertyName(propertyyName);
	}

	public String getComponentClassName() {
		return checkPropertyAction.getComponentClassName();
	}

	public void setComponentClassName(String componentClassName) {
		checkPropertyAction.setComponentClassName(componentClassName);
	}

	public List<String> getPropertyNameEnumeration() {
		return checkPropertyAction.getPropertyNameOptions();
	}

	public String getPropertyValue() {
		return checkPropertyAction.getPropertyValueExpected();
	}

	public void setPropertyValue(String value) {
		checkPropertyAction.setPropertyValueExpected(value);
	}

	@Override
	protected boolean matchesInContainingWindow(Component c) {
		try {
			Class<?> expectedClass;
			try {
				expectedClass = Class.forName(checkPropertyAction
						.getComponentClassName());
			} catch (ClassNotFoundException e) {
				throw new AssertionError(e);
			}
			if (!expectedClass.isInstance(c)) {
				return false;
			}
			checkPropertyAction.execute(c);
			return true;
		} catch (TestingError e) {
			return false;
		}
	}

	@Override
	protected boolean initializeSpecificCriterias(Component c) {
		return checkPropertyAction.initializeFrom(c, null);
	}

	@Override
	public String toString() {
		return MessageFormat.format(
				"\"{0}\" component n°{1} in the window n°{2}",
				checkPropertyAction.getValueDescription(),
				(occurrencesToSkip + 1), (windowIndex + 1));
	}

}
