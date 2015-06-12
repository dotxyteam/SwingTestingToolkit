package xy.ui.testing.action.component.property;

import java.awt.Component;

import org.apache.commons.lang3.StringEscapeUtils;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.ui.testing.util.TestingError;

public class CheckComponentPropertyAction extends ComponentPropertyAction {
	private static final long serialVersionUID = 1L;

	protected String propertyValueExpected;

	public String getPropertyValueExpected() {
		return propertyValueExpected;
	}

	public void setPropertyValueExpected(String propertyValueExpected) {
		this.propertyValueExpected = propertyValueExpected;
	}

	@Override
	public void execute(final Component c) {
		IFieldInfo field = getPropertyFieldInfo();
		Object currentFieldValue = field.getValue(c);
		Object expectedFieldValue = propertyValueToFieldValue(propertyValueExpected);
		if (!ReflectionUIUtils.equalsOrBothNull(currentFieldValue,
				expectedFieldValue)) {
			throw new TestingError(
					"Component property checking failed: Unexpected property value: '"
							+ currentFieldValue + "'. Expected: '"
							+ expectedFieldValue + "'");
		}
	}

	@Override
	public String getValueDescription() {
		if (propertyValueExpected == null) {
			return propertyName + " = <null>";
		} else {
			String propertyValueString;
			if (String.class.getName().equals(
					getPropertyFieldInfo().getType().getName())) {
				propertyValueString = "\""
						+ StringEscapeUtils.escapeJava(propertyValueExpected)
						+ "\"";
			} else {
				propertyValueString = propertyValueExpected;
			}
			return propertyName + " = " + propertyValueString;
		}
	}

	@Override
	public String toString() {
		return "Check that \"" + getValueDescription() + "\" for the "
				+ getComponentFinder();
	}

}
